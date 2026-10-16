# CI/CD və Secure SDLC sənədi

Layihə: Java (Gradle Kotlin DSL) · Platforma: on-prem GitLab **Free**

## 1. Məqsəd

Hər kod dəyişikliyi **Merge Request (MR)** vasitəsilə, avtomatik build, test və security yoxlamalarından keçərək `develop` və `main` branch-lərinə daxil olur. Security scanner-lər tapıntıları JSON report kimi yazır, **security gate** isə branch-ə görə pipeline-ı keçirir və ya bloklayır.

## 2. Branch strategiyası

```
feature/*  ──MR──▶  develop  ──MR──▶  main
```

| Branch | Məqsəd | Push | Merge |
|---|---|---|---|
| `feature/*` | Fərdi iş. MR-dan əvvəl pipeline yaranmır | Developer | – |
| `develop` | İnteqrasiya branch-i (protected) | **No one** | MR vasitəsilə (Developers + Maintainers) |
| `main` | Production-hazır kod, default branch (protected) | **No one** | MR vasitəsilə (yalnız Maintainers tövsiyə olunur) |

### GitLab ayarları (UI)

**Settings → Repository → Protected branches** (`develop`, `main`):
- Allowed to push and merge: **No one** (push), merge üçün Developers/Maintainers
- Allowed to force push: **söndürülmüş**

**Settings → Merge requests**:
- Merge checks: **Pipelines must succeed** = aktiv
- **Skipped pipelines are considered successful** = *söndürülmüş*
- **All threads must be resolved** = aktiv
- Squash commits və *Delete source branch* üçün komandanın seçimi

> `Pipelines must succeed` aktiv olduğu üçün `security-gate` job-u fail olan MR **merge edilə bilməz**.

## 3. Fayl strukturu

```
.gitlab-ci.yml                 stages + workflow + variables + include
ci/base.yml                    təkrar istifadə olunan gizli job (.gradle-base: image, cache)
ci/build.yml                   gradle-build
ci/test.yml                    unit-test, integration-test
ci/package.yml                 docker-build (registry-yə push)
ci/security.yml                SAST, IaC, Secret, SCA, Container scan
ci/gate.yml                    security-gate, gate-selftest
scripts/security-gate.sh       gate məntiqi
scripts/test-security-gate.sh  gate-in self-testi (26 ssenari)
security/gate-allowlist.txt    istisna siyahısı (false-positive)
Dockerfile, .dockerignore
```

## 4. Pipeline axını

```mermaid
flowchart LR
  subgraph build
    B[gradle-build]
  end
  subgraph test
    U[unit-test]
    I[integration-test]
  end
  subgraph package
    D[docker-build]
  end
  subgraph security
    S1[semgrep-sast]
    S2[kics-iac-sast]
    S3[secret_detection]
    S4[sca-trivy]
    S5[container_scanning]
  end
  subgraph gate
    G[security-gate]
  end
  B --> U & I
  B --> D --> S5
  B --> S4
  S1 & S2 & S3 & S4 & S5 --> G
```

SAST, IaC və Secret scan-lar `needs: []` ilə **build-i gözləmədən** paralel başlayır. SCA build artifact-ına, Container scan isə image-ə ehtiyac duyur.

### Pipeline nə vaxt yaranır (`workflow:rules`)

| Hadisə | Pipeline |
|---|---|
| MR açıq/yenilənib | **MR pipeline** |
| `develop` və ya `main`-ə push/merge | Branch pipeline |
| MR-ı olmayan `feature/*` push | Yaranmır |
| MR-ı olan `feature/*` push | Yalnız MR pipeline (dublikat yoxdur) |

MR pipeline-ında `CI_COMMIT_BRANCH` **boşdur**. Buna görə gate hədəf branch-i `CI_MERGE_REQUEST_TARGET_BRANCH_NAME`-dən oxuyur və GitLab template-lərinin `rules`-u `ci/security.yml`-də əvəz edilib.

## 5. Job-lar

| Job | Stage | Alət | Report |
|---|---|---|---|
| `gradle-build` | build | Gradle | `build/app.jar` (artifact, 1 həftə) |
| `unit-test` | test | Gradle + JUnit | JUnit report (MR-da görünür) |
| `integration-test` | test | Gradle `integrationTest` | JUnit report |
| `docker-build` | package | Docker | image: `$CI_REGISTRY_IMAGE:$CI_COMMIT_SHA` |
| `semgrep-sast` | security | GitLab SAST (Semgrep) | `gl-sast-report.json` |
| `kics-iac-sast` | security | GitLab IaC (KICS) | `gl-iac-report.json` |
| `secret_detection` | security | GitLab Secret Detection | `gl-secret-detection-report.json` |
| `sca-trivy` | security | Trivy | `trivy-sca-lock.json`, `trivy-sca-jar.json` |
| `container_scanning` | security | GitLab Container Scanning | `gl-container-scanning-report.json` |
| `security-gate` | gate | `scripts/security-gate.sh` | `gate-summary.md` |
| `gate-selftest` | gate | gate skriptinin testi | yalnız skript dəyişəndə işləyir |

## 6. Security gate qaydaları

Hədəf branch-ə görə profil seçilir (MR üçün MR-ın hədəfi, push üçün branch adı):

| Scan | `develop` | `main` | digər |
|---|---|---|---|
| SAST | **High/Critical → FAIL** | **High/Critical → FAIL** | xəbərdarlıq |
| IaC | xəbərdarlıq | **High/Critical → FAIL** | xəbərdarlıq |
| SCA (dependency) | xəbərdarlıq | **High/Critical → FAIL** | xəbərdarlıq |
| Container | xəbərdarlıq | **High/Critical → FAIL** | xəbərdarlıq |
| Secret | xəbərdarlıq | **istənilən secret → FAIL** | xəbərdarlıq |

Əlavə qaydalar:
- **Fail-closed:** bloklayıcı rejimdə report yoxdursa və ya JSON korlanıbsa gate **FAIL** olur (scanner səssizcə çökə bilməz).
- **Secret-lər severity-dən asılı olmayaraq** bloklayır və allowlist ilə **susdurula bilməz**: secret rotasiya olunmalıdır.
- Tövsiyə: `develop`-da da secret bloklansın. Bunun üçün `mode_for()` funksiyasına `develop:secret) echo block ;;` sətri əlavə et.
- Blok severity-ləri `GATE_BLOCK_SEVERITIES` (default `high,critical`) ilə dəyişir.

### Istisna (false-positive) idarəsi

`security/gate-allowlist.txt` faylına CVE/CWE/rule ID-si yazılır (hər sətirdə bir ID, `#` şərhdir). Hər əlavə **ayrıca MR** ilə, səbəb və vaxt limiti şərhdə göstərilərək edilməlidir:

```
CVE-2099-0001   # səbəb: təsir olunan funksiya istifadə olunmur, MR !57, yoxlanış: 2026-12-01
```

## 7. CI/CD Variables

Secret heç vaxt `.gitlab-ci.yml`-də və ya repo-da saxlanmır. **Settings → CI/CD → Variables**:

| Variable | Nə üçün | Visibility | Protected | Environment scope |
|---|---|---|---|---|
| `CI_REGISTRY_*`, `CI_JOB_TOKEN` | registry login | GitLab özü verir (qurulum lazım deyil) | – | – |
| Deploy/prod secret-lər (məs. `PROD_DB_PASSWORD`, `DEPLOY_SSH_KEY`) | deploy | **Masked and hidden** | **Bəli** | `production` / `develop` |
| Integration test dəyərləri (məs. test DB parolu) | MR pipeline-ında test | Masked | **Xeyr** | `*` |
| `TRIVY_USERNAME` / `TRIVY_PASSWORD` (yalnız private DB mirror üçün) | Trivy DB endirmə | Masked and hidden | **Xeyr** | `*` |

Vacib qaydalar:
- **Protected** dəyişənlər yalnız protected branch-lərdə (`develop`, `main`) görünür. `feature/*` MR pipeline-ı onları **görmür**; bu istənilən davranışdır (təsadüfən secret sızmasın). Buna görə feature MR-da lazım olan test dəyərləri ayrıca, **real olmayan** dəyərlərlə, protected olmadan saxlanır.
- **Masked and hidden** (GitLab 17.4+) dəyər yaradıldıqdan sonra UI-da bir daha görünmür. Köhnə versiyada *Masked* + *Protected* istifadə et. Masked üçün dəyər tək sətir və ən az 8 simvol olmalıdır.
- Secret-də `$` varsa *Expand variable reference* söndürülməlidir.
- **Settings → CI/CD → Variables → Minimum role to use pipeline variables** və **Job token permissions** məhdudlaşdırılsın.
- Secret dəyərini `echo`/`env` ilə log-a yazma.

## 8. Cache və artifacts

| Nə | Necə |
|---|---|
| Gradle cache | `.gradle/caches`, `.gradle/wrapper`. Açar: `build.gradle.kts` + `gradle-wrapper.properties` hash-i. `gradle-build` **pull-push**, qalan job-lar **pull** |
| Trivy DB cache | `.trivy-cache/`, açar `trivy-db` |
| Build artifact | `build/libs/`, `build/app.jar`, `expire_in: 1 week` |
| Test report | `when: always` (test fail olsa da report qalır) |
| Scan report | `when: always` (template), 30 gün, gate-ə `needs:artifacts` ilə ötürülür |
| Gate summary | `gate-summary.md`, `when: always`, 30 gün |

Cache sürət üçündür, artifact isə job-lar arası **etibarlı** ötürmədir. Pipeline cache olmadan da işləməlidir.

## 9. Runner tələbləri

- Docker executor-lu on-prem runner (Settings → CI/CD → Runners).
- `docker-build` üçün runner **privileged** olmalıdır (Docker-in-Docker). Əgər olmazsa, docker socket binding və ya Kaniko/Buildah istifadə et.
- GitLab **Container Registry** aktiv olmalıdır (Settings → General → Visibility → Container registry).
- Runner şəbəkəsindən çıxış: Docker Hub, Trivy DB (`ghcr.io`, `mirror.gcr.io`), `registry.gitlab.com` (scanner image-ləri), Alpine paket mirror-u, `services.gradle.org`, Maven Central. Məhdud şəbəkədə bunları daxili mirror-a yönləndir (`GATE_IMAGE`, `TRIVY_IMAGE`, `JAVA_IMAGE`, `TRIVY_DB_REPOSITORY`).
- Runner tag-ları istifadə olunursa `.gitlab-ci.yml`-də `default: tags:` bloku aktiv edilməlidir.

## 10. İlk quraşdırma (layihəyə birdəfəlik əlavələr)

### a) `.gitignore`
```
.gradle/
.trivy-cache/
build/
gate-summary.md
```

### b) SCA üçün dependency locking (`build.gradle.kts`)
Trivy Gradle layihəsində dependency-ləri `gradle.lockfile`-dan oxuyur:
```kotlin
dependencyLocking {
    lockAllConfigurations()
}
```
```bash
./gradlew dependencies --write-locks
git add gradle.lockfile
```
Dependency dəyişəndə lockfile yenidən yaradılıb commit edilməlidir.

### c) Integration test (`build.gradle.kts`)
Layihəyə uyğunlaşdır (Spring Boot-da əlavə dependency lazım ola bilər):
```kotlin
testing {
    suites {
        val integrationTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(project())
            }
        }
    }
}
tasks.named("check") { dependsOn(testing.suites.named("integrationTest")) }
```
Testlər `src/integrationTest/java` qovluğunda olur. Task hazır olandan sonra `.gitlab-ci.yml`-də `REQUIRE_INTEGRATION_TESTS: "true"` et ki, task silinərsə job səssizcə keçməsin.

### d) Mövcud tarixçədə secret yoxlaması (bir dəfə)
**Build → Pipelines → Run pipeline** → `develop` branch-i, variable `SECRET_DETECTION_HISTORIC_SCAN = true`. Tapılan hər secret rotasiya olunmalıdır (git-dən silmək kifayət deyil).

## 11. Nümunə pipeline-lar (təhvil üçün)

Bloklanan nümunələr üçün **ayrıca demo branch** istifadə olunur. Merge edilmir, screenshot götürüldükdən sonra MR bağlanır. Hədəf branch-ə görə hansı qaydanın işlədiyi MR-ın *target branch*-indən asılıdır.

### ✅ Uğurlu pipeline
1. `feature/ci-cd-setup` → `develop` MR aç. Bütün job-lar yaşıl, `security-gate` = *PASSED*.
2. Merge et. `develop` branch pipeline-ı yaşıl olmalıdır.
3. `develop` → `main` MR aç, merge et.

Screenshot: pipeline qrafı (hamısı yaşıl) + `security-gate` log-u.

### ❌ Bloklanan 1: SAST High (hədəf: `develop`)
```bash
git switch develop && git pull
git switch -c feature/demo-sast-blocked
```
Layihəyə (məs. `src/main/java/.../DemoRepository.java`) zəif kod əlavə et:
```java
public ResultSet find(Connection c, String name) throws SQLException {
    Statement st = c.createStatement();
    return st.executeQuery("SELECT * FROM users WHERE name = '" + name + "'");
}
```
`feature/demo-sast-blocked` → **`develop`** MR aç. Gözlənən: `security-gate` FAIL, `sast | block | ≥1 | FAIL`.

> Semgrep qaydasının severity-si versiyadan asılı olaraq Medium/High ola bilər. `gl-sast-report.json` artifact-ında `severity` sahəsinə bax. Medium-dursa gate bloklamaz və başqa nümunə (məs. `Runtime.getRuntime().exec(userInput)`) sına.

### ❌ Bloklanan 2: Secret (hədəf: `main`)
```bash
git switch develop && git pull
git switch -c feature/demo-secret-blocked
# təsadüfi, saxta AWS formatlı açar yarat (real açar deyil):
echo "aws.key=AKIA$(head -c 10 /dev/urandom | base32)" > src/main/resources/demo-secret.properties
git add . && git commit -m "demo: secret" && git push -u origin HEAD
```
MR-ın hədəfini **`main`** seç. Gözlənən: `secret | block | ≥1 | FAIL`.

> `AKIAIOSFODNN7EXAMPLE` kimi sənəd nümunələrini istifadə etmə: scanner-lər onları bilərəkdən ignore edir. Demo-dan sonra branch-i sil.

### ❌ Bloklanan 3: SCA, köhnə log4j (hədəf: `main`)
`build.gradle.kts`-ə `implementation("org.apache.logging.log4j:log4j-core:2.14.1")` əlavə et, sonra:
```bash
./gradlew dependencies --write-locks && git add -A && git commit -m "demo: vulnerable dependency"
```
MR → **`main`**. Gözlənən: `sca | block | ≥1 | FAIL` (CVE-2021-44228, Critical).

### ❌ Bloklanan 4: Container (hədəf: `main`)
`.gitlab-ci.yml`-də `RUNTIME_IMAGE` dəyərini High CVE-ləri olan köhnə base image-ə dəyiş (məs. köhnə `eclipse-temurin:8-jre-focal`), MR → **`main`**. Gözlənən: `container | block | ≥1 | FAIL`.

### Təhvil üçün screenshot siyahısı
1. Protected branches ayarları
2. Variables siyahısı (dəyərlər gizli)
3. Uğurlu pipeline qrafı və `security-gate` log-u
4. Hər bloklanan MR: pipeline qrafı, `security-gate` log-u, bloklanmış *Merge* düyməsi
5. Artifacts səhifəsi (`gl-*.json`, `gate-summary.md`)

## 12. Lokal test

```bash
bash scripts/test-security-gate.sh     # tələb: bash, jq
```
26 ssenari yoxlanır: hər branch profili, missing/korlanmış report (fail-closed), allowlist və secret-in susdurula bilməməsi.

## 13. GitLab Free məhdudiyyətləri

- **Vulnerability Report, Security Dashboard və MR-da security widget yoxdur** (Ultimate). Tapıntılar artifact JSON-larında və gate log-unda görünür.
- **Security/Merge approval policy və scan execution policy yoxdur.** Məcburiyyət `workflow`, `Pipelines must succeed` və gate job-u ilə təmin olunur.
- Required approvals və Protected environments Premium funksiyalarıdır. Review qaydası komanda konvensiyası ilə saxlanılmalıdır.
- GitLab Dependency Scanning əvəzinə **Trivy** istifadə olunur.
- Template job adları on-prem versiyadan asılıdır. `CI/CD → Editor → Validate` ilə yoxla.

## 14. Problemlərin həlli

| Simptom | Səbəb / Həll |
|---|---|
| Pipeline yaranmır | Feature branch-də MR yoxdur. MR aç (bu dizayndır) |
| `jobs:semgrep-sast config contains unknown keys` və ya "no script" | Template-də job adı fərqlidir. `Jobs/SAST.gitlab-ci.yml`-ə baxıb `ci/security.yml`-də adı düzəlt |
| `security-gate`: `Report tapılmadı -> fail-closed` | Scan job-u report yaratmayıb. Həmin job-un log-una bax (image pull, şəbəkə) |
| `sca-trivy` boş nəticə | `gradle.lockfile` yoxdur (bölmə 10b) |
| `docker-build` "Cannot connect to the Docker daemon" | Runner privileged deyil (bölmə 9) |
| `integration-test` heç nə işlətmədi | `integrationTest` task-ı yoxdur (bölmə 10c) |
| main-də container scan həmişə FAIL | Base image-də High CVE var. Image-i yenilə; yeniləmə mümkün deyilsə allowlist (MR ilə) |
| Variable pipeline-da boşdur | Variable *Protected*-dir, branch isə protected deyil |
