---
name: test-coverage
description: This skill should be used when analyzing test coverage metrics. For Java it uses JaCoCo/Gradle; for JavaScript it uses Jest/Vitest coverage.
---

# Test Coverage

## Purpose

Analyze test coverage to identify untested code paths. Supports Java/JaCoCo (Phase 1) and JavaScript/Jest/Vitest (Phase 2) via auto-detection.

## When to Use

- Before major releases to assess quality
- When adding tests to improve coverage
- During code review to verify test adequacy
- When `qa` agent needs coverage metrics

## Workflow

### 1. Detect Coverage Tool

Check **in order**:

1. `build.gradle` or `src/test/java/` → **Java/JaCoCo + Gradle mode**
2. Jest/Vitest config → **JavaScript coverage mode**

### 2. Run Coverage

#### Java / JaCoCo / Gradle (Phase 1 — Current)

```bash
# Windows — run tests + generate JaCoCo report
gradlew.bat test jacocoTestReport

# Linux/Mac
./gradlew test jacocoTestReport

# Run only if tests already passed
gradlew.bat jacocoTestReport

# Generate with XML (for CI integration)
gradlew.bat test jacocoTestReport jacocoTestCoverageVerification
```

**JaCoCo configuration** (in `build.gradle`):
```groovy
jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80  // 80% line coverage
            }
        }
    }
}
```

HTML report generated at: `build/reports/jacoco/test/html/index.html`

#### JavaScript / npm (Phase 2 — React Frontend)

```bash
# Jest
npx jest --coverage

# Vitest
npx vitest run --coverage

# Generate HTML report
npx jest --coverage --coverageReporters="html"
```

### 3. Output Format

Report results in structured format:

```
## Coverage Report

**Overall Coverage**: 82.3%
**Mode**: Java/JaCoCo | JS/Jest | JS/Vitest

| Metric | Coverage | Threshold | Status |
|--------|----------|-----------|--------|
| Lines | 82.3% | 80% | ✅ Passed |
| Branches | 71.5% | 70% | ✅ Passed |
| Methods | 85.0% | 80% | ✅ Passed |
| Instructions | 81.2% | 80% | ✅ Passed |

### Files with Low Coverage (<50%)

| File | Lines | Branches | Methods |
|------|-------|----------|---------|
| MusicService.java | 32% | 25% | 40% |

### Uncovered Methods

**MusicService.java**:
- `updateTrack()` — not covered
- `deleteTrack()` — not covered

### Recommendations

1. Add unit tests for `MusicService.updateTrack()`
2. Add exception path tests for `MusicService.deleteTrack()`
```

## Coverage Thresholds (ATStudio Standards)

Per `docs/standards/development-standards.md` Section 6.3:

| Metric | Threshold |
|--------|-----------|
| Lines | 80% |
| Branches | 70% |
| Methods/Functions | 80% |
| Statements/Instructions | 80% |

**100% coverage required for:** Security code, JWT/auth logic, business rule validators.

## Integration

- Runs as part of `qa` agent comprehensive check
- Supplements `/test` skill with coverage metrics
- Results inform `cr` agent review decisions
- Required before WI completion (per Evidence Pack standards)
