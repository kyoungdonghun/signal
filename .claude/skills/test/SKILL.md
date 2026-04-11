---
name: test
description: This skill should be used when running project test suites. It executes tests using the project's configured test framework (JUnit5/Gradle or Jest/Vitest) and reports results.
---

# Test

## Purpose

Execute project test suites and report results. Supports Java/JUnit5 (Phase 1) and JavaScript/Jest/Vitest (Phase 2) via auto-detection.

## When to Use

- After implementing new features
- Before committing code changes
- When `qa` agent needs test verification
- During CI/CD pipeline validation
- When `re` agent investigates failures

## Workflow

### 1. Detect Test Framework

Check for test configuration **in order**:

1. `build.gradle` or `src/test/java/` → **Java/JUnit5 + Gradle mode**
2. `vitest.config.ts` / `vite.config.ts` with test config → **Vitest**
3. `jest.config.js` / `jest.config.ts` → **Jest**
4. `package.json` scripts.test → **npm test**

### 2. Run Tests

#### Java / JUnit5 / Gradle (Phase 1 — Current)

```bash
# Windows — run all tests
gradlew.bat test

# Linux/Mac
./gradlew test

# Run specific test class
gradlew.bat test --tests "com.atstudio.atstudio.service.MusicServiceTest"

# Run with verbose output
gradlew.bat test --info

# Run and generate report
gradlew.bat test jacocoTestReport
```

Test reports generated at: `build/reports/tests/test/index.html`

#### JavaScript / npm (Phase 2 — React Frontend)

```bash
# Run all tests
npm test

# Verbose output
npm test -- --verbose

# Run specific test file
npm test -- src/utils/helpers.test.ts

# Run tests matching pattern
npm test -- --testNamePattern="should validate"
```

### 3. Output Format

Report results in structured format:

```
## Test Results

**Status**: ❌ Failed / ✅ Passed
**Mode**: Java/JUnit5 | JS/Jest | JS/Vitest
**Total**: X tests | **Passed**: Y | **Failed**: Z | **Skipped**: W

### Summary

| Suite | Tests | Passed | Failed | Duration |
|-------|-------|--------|--------|----------|
| MusicServiceTest | 5 | 5 | 0 | 0.8s |
| MusicControllerTest | 3 | 2 | 1 | 1.2s |

### Failed Tests

#### MusicControllerTest

**Test**: should return 404 when music not found
**Error**: Expected status 404 but was 200
**Location**: MusicControllerTest.java:45
```

## Framework-Specific Commands

| Framework | Run All | Specific Class | Verbose |
|-----------|---------|----------------|---------|
| Gradle/JUnit5 | `gradlew.bat test` | `gradlew.bat test --tests "ClassName"` | `gradlew.bat test --info` |
| Jest | `npx jest` | `npx jest helpers.test.ts` | `npx jest --verbose` |
| Vitest | `npx vitest run` | `npx vitest run helpers.test.ts` | `npx vitest run --reporter=verbose` |

## Integration

- Runs as part of `qa` agent quality workflow
- For detailed failure investigation, delegate to `re` agent
- Coverage analysis available via `/test-coverage` skill
