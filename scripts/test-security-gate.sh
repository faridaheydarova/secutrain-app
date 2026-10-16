#!/usr/bin/env bash
# security-gate.sh üçün lokal/CI self-test.
# İstifadə: bash scripts/test-security-gate.sh
# Fixture-lar müvəqqəti qovluqda generasiya olunur (repo-da saxta finding yoxdur).
set -uo pipefail

GATE="$(cd "$(dirname "$0")" && pwd)/security-gate.sh"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
PASS=0; FAIL=0

gl_empty()  { echo '{"version":"15.0.0","vulnerabilities":[]}'; }
gl_one()    { jq -n --arg s "$1" --arg n "$2" --arg i "$3" \
  '{version:"15.0.0",vulnerabilities:[{name:$n,severity:$s,identifiers:[{type:"id",name:$i,value:$i}],location:{file:"src/Main.java"}}]}'; }
tr_empty()  { echo '{"Results":[]}'; }
tr_one()    { jq -n --arg s "$1" \
  '{Results:[{Target:"gradle.lockfile",Vulnerabilities:[{VulnerabilityID:"CVE-2099-0001",PkgName:"demo-lib",Severity:$s}]}]}'; }

# yeni "təmiz" report qovluğu yaradır
mkdir_clean() {
  local d="$TMP/$1"; mkdir -p "$d"
  gl_empty > "$d/gl-sast-report.json"
  gl_empty > "$d/gl-iac-report.json"
  gl_empty > "$d/gl-container-scanning-report.json"
  gl_empty > "$d/gl-secret-detection-report.json"
  tr_empty > "$d/trivy-sca-lock.json"
  tr_empty > "$d/trivy-sca-jar.json"
  echo "$d"
}

# expect <ad> <profil> <gözlənən exit> <report-dir> [ALLOWLIST_FILE]
expect() {
  local name="$1" profile="$2" want="$3" dir="$4" allow="${5:-/nonexistent}"
  GATE_PROFILE="$profile" REPORT_DIR="$dir" GATE_ALLOWLIST_FILE="$allow" \
    GATE_SUMMARY_FILE="$dir/summary.md" bash "$GATE" >"$dir/out.log" 2>&1
  local got=$?
  if [ "$got" -eq "$want" ]; then
    PASS=$((PASS+1)); printf '  ok    %-52s [%s] exit=%s\n' "$name" "$profile" "$got"
  else
    FAIL=$((FAIL+1)); printf '  FAIL  %-52s [%s] gözlənən=%s, alınan=%s\n' "$name" "$profile" "$want" "$got"
    sed 's/^/        | /' "$dir/out.log"
  fi
}

echo "Security gate testləri"

d="$(mkdir_clean clean)"
expect "təmiz layihə"                         develop 0 "$d"
expect "təmiz layihə"                         main    0 "$d"

d="$(mkdir_clean sast-high)";  gl_one High "SQL Injection" "CWE-89" > "$d/gl-sast-report.json"
expect "SAST High"                            feature 0 "$d"
expect "SAST High"                            develop 1 "$d"
expect "SAST High"                            main    1 "$d"

d="$(mkdir_clean sast-critical)"; gl_one Critical "RCE" "CWE-94" > "$d/gl-sast-report.json"
expect "SAST Critical"                        develop 1 "$d"

d="$(mkdir_clean sast-medium)"; gl_one Medium "Weak hash" "CWE-328" > "$d/gl-sast-report.json"
expect "SAST yalnız Medium"                   develop 0 "$d"
expect "SAST yalnız Medium"                   main    0 "$d"

d="$(mkdir_clean iac-high)"; gl_one High "Dockerfile runs as root" "KICS-1" > "$d/gl-iac-report.json"
expect "IaC High (develop-da bloklamır)"      develop 0 "$d"
expect "IaC High"                             main    1 "$d"

d="$(mkdir_clean sca-high)"; tr_one HIGH > "$d/trivy-sca-lock.json"
expect "SCA High (develop-da bloklamır)"      develop 0 "$d"
expect "SCA High"                             main    1 "$d"

d="$(mkdir_clean sca-low)"; tr_one LOW > "$d/trivy-sca-jar.json"
expect "SCA yalnız Low"                       main    0 "$d"

d="$(mkdir_clean container-critical)"; gl_one Critical "openssl CVE" "CVE-2099-1111" > "$d/gl-container-scanning-report.json"
expect "Container Critical (develop-da yox)"  develop 0 "$d"
expect "Container Critical"                   main    1 "$d"

d="$(mkdir_clean secret)"; gl_one Critical "AWS access key" "aws-key" > "$d/gl-secret-detection-report.json"
expect "Secret aşkarlandı (develop-da yox)"   develop 0 "$d"
expect "Secret aşkarlandı"                    main    1 "$d"

d="$(mkdir_clean secret-low)"; gl_one Low "Generic token" "generic" > "$d/gl-secret-detection-report.json"
expect "Secret (Low severity də bloklayır)"   main    1 "$d"

d="$(mkdir_clean missing-sast)"; rm "$d/gl-sast-report.json"
expect "SAST report yoxdur (fail-closed)"     develop 1 "$d"
expect "SAST report yoxdur (feature: skip)"   feature 0 "$d"

d="$(mkdir_clean missing-container)"; rm "$d/gl-container-scanning-report.json"
expect "Container report yoxdur (develop OK)" develop 0 "$d"
expect "Container report yoxdur (fail-closed)" main   1 "$d"

d="$(mkdir_clean broken-json)"; echo "{ not json" > "$d/gl-sast-report.json"
expect "SAST report korlanıb (fail-closed)"   main    1 "$d"

d="$(mkdir_clean allowlist)"; gl_one High "False positive" "CWE-999" > "$d/gl-sast-report.json"
printf '# səbəb: false positive, MR !12\nCWE-999   # inline şərh\n' > "$d/allow.txt"
expect "Allowlist-də olan finding keçir"      develop 0 "$d" "$d/allow.txt"
expect "Allowlist olmadan bloklanır"          develop 1 "$d"

d="$(mkdir_clean allow-secret)"; gl_one High "Real secret" "aws-key" > "$d/gl-secret-detection-report.json"
printf 'aws-key\n' > "$d/allow.txt"
expect "Secret allowlist ilə SUSDURULA BİLMƏZ" main   1 "$d" "$d/allow.txt"

echo ""
echo "Nəticə: $PASS keçdi, $FAIL uğursuz"
[ "$FAIL" -eq 0 ]
