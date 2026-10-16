#!/usr/bin/env bash

set -uo pipefail

REPORT_DIR="${REPORT_DIR:-.}"
BLOCK_SEVERITIES="${GATE_BLOCK_SEVERITIES:-high,critical}"
ALLOWLIST_FILE="${GATE_ALLOWLIST_FILE:-security/gate-allowlist.txt}"
SUMMARY_FILE="${GATE_SUMMARY_FILE:-gate-summary.md}"
DEFAULT_BRANCH="${CI_DEFAULT_BRANCH:-main}"

command -v jq >/dev/null 2>&1 || { echo "ERROR: jq tapılmadı"; exit 2; }

TARGET="${CI_MERGE_REQUEST_TARGET_BRANCH_NAME:-${CI_COMMIT_BRANCH:-}}"
PROFILE="${GATE_PROFILE:-}"
if [ -z "$PROFILE" ]; then
  if   [ "$TARGET" = "$DEFAULT_BRANCH" ]; then PROFILE="main"
  elif [ "$TARGET" = "develop" ];         then PROFILE="develop"
  else PROFILE="feature"
  fi
fi


mode_for() {
  case "$PROFILE:$1" in
    develop:sast) echo block ;;
    main:*)       echo block ;;
    *)            echo warn  ;;
  esac
}

ALLOW_JSON="$(sed 's/#.*//; s/[[:space:]]//g' "$ALLOWLIST_FILE" 2>/dev/null | grep -v '^$' | jq -R . | jq -s . 2>/dev/null)"
[ -n "$ALLOW_JSON" ] || ALLOW_JSON='[]'


NORM='
def norm:
  [ (.vulnerabilities[]? | {
        severity: (.severity // "Unknown"),
        name:     (.name // "unnamed"),
        where:    (.location.file // .location.image // "-"),
        ids:      ([.identifiers[]?.value] + [.cve // empty])
     }),
    (.Results[]? | (.Target // "-") as $t | .Vulnerabilities[]? | {
        severity: (.Severity // "UNKNOWN"),
        name:     ((.VulnerabilityID // "?") + " " + (.PkgName // "")),
        where:    $t,
        ids:      [.VulnerabilityID // empty]
     })
  ];
'

BLOCKING_FILTER="$NORM"'
  ($sev | split(",") | map(ascii_downcase)) as $b
  | norm
  | map(select($kind == "all" or ((.severity | ascii_downcase) as $s | ($b | index($s)) != null)))
  | map(select(((.ids | map(. as $i | ($allow | index($i)) != null) | any)) | not))
'
TOTAL_FILTER="$NORM"' norm | length'

FAILED=0
SUMMARY_ROWS=""

scan() {
  local name="$1" type="$2"; shift 2
  local mode kind allow present=0 blocking=0 total=0 f path out n t status note=""
  mode="$(mode_for "$name")"
  if [ "$type" = "secret" ]; then kind="all"; allow='[]'; else kind="sev"; allow="$ALLOW_JSON"; fi

  echo ""
  echo "== $name  (rejim: $mode) =="

  for f in "$@"; do
    path="$REPORT_DIR/$f"
    [ -f "$path" ] || continue
    if ! jq empty "$path" >/dev/null 2>&1; then
      echo "  ! $f oxunmadı (yanlış JSON)"
      note="yanlış JSON"
      continue
    fi
    present=$((present + 1))
    out="$(jq -c --argjson allow "$allow" --arg sev "$BLOCK_SEVERITIES" --arg kind "$kind" "$BLOCKING_FILTER" "$path")"
    n="$(jq 'length' <<<"$out")"
    t="$(jq "$TOTAL_FILTER" "$path")"
    blocking=$((blocking + n))
    total=$((total + t))
    echo "  $f: ümumi=$t, bloklayıcı=$n"
    if [ "$n" -gt 0 ]; then
      jq -r '.[] | "    - [\(.severity)] \(.name)  (\(.where))"' <<<"$out" | head -n 15
      [ "$n" -gt 15 ] && echo "    ... və daha $((n - 15)) tapıntı"
    fi
  done

  if [ "$present" -eq 0 ]; then
    if [ "$mode" = "block" ]; then
      echo "  ! Report tapılmadı/oxunmadı -> fail-closed"
      status="FAIL (report yoxdur)"; FAILED=1
    else
      echo "  Report tapılmadı (xəbərdarlıq)"
      status="SKIP (report yoxdur)"
    fi
    SUMMARY_ROWS="${SUMMARY_ROWS}| $name | $mode | - | $status |"$'\n'
    return
  fi

  if [ "$blocking" -gt 0 ] && [ "$mode" = "block" ]; then
    status="FAIL"; FAILED=1
  elif [ "$blocking" -gt 0 ]; then
    status="WARN"
  else
    status="OK"
  fi
  [ -n "$note" ] && status="$status ($note)"
  SUMMARY_ROWS="${SUMMARY_ROWS}| $name | $mode | $blocking | $status |"$'\n'
}

echo "Security Gate | profil=$PROFILE | hədəf='${TARGET:-n/a}' | bloklayıcı severity=$BLOCK_SEVERITIES"
echo "Allowlist girişləri: $(jq 'length' <<<"$ALLOW_JSON")"

scan sast      vuln   gl-sast-report.json
scan iac       vuln   gl-iac-report.json
scan sca       vuln   trivy-sca-lock.json trivy-sca-jar.json
scan container vuln   gl-container-scanning-report.json
scan secret    secret gl-secret-detection-report.json

{
  echo "### Security Gate nəticəsi"
  echo ""
  echo "Profil: **$PROFILE** (hədəf: \`${TARGET:-n/a}\`)"
  echo ""
  echo "| Scan | Rejim | Bloklayıcı tapıntı | Nəticə |"
  echo "|---|---|---|---|"
  printf '%s' "$SUMMARY_ROWS"
} > "$SUMMARY_FILE"

echo ""
cat "$SUMMARY_FILE"
echo ""
if [ "$FAILED" -ne 0 ]; then
  echo "SECURITY GATE: FAILED (pipeline bloklandı)"
  exit 1
fi
echo "SECURITY GATE: PASSED"
exit 0
