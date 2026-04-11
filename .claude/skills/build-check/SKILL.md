---
name: build-check
description: This skill should be used when verifying that the project builds successfully. It runs the build command and reports any compilation errors or warnings.
---

# Build Check

## Purpose

Verify that the project compiles and builds without errors. Supports Java/Gradle (Phase 1) and JavaScript/npm (Phase 2) projects via auto-detection.

## When to Use

- Before committing significant changes
- After dependency updates
- When `qa` agent needs build verification
- Before deployment or release
- After refactoring or restructuring

## Workflow

### 1. Detect Build System

Check for build configuration **in order**:

1. `build.gradle` or `gradlew.bat` → **Java/Gradle mode**
2. `package.json` with scripts.build → **npm/JS mode**
3. Both present → **Java/Gradle mode** (Java is primary)

### 2. Run Build

#### Java / Gradle (Phase 1 — Current)

```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build

# Build without tests (faster check)
gradlew.bat build -x test

# Clean build
gradlew.bat clean build
```

#### JavaScript / npm (Phase 2 — React Frontend)

```bash
# Standard npm build
npm run build

# With verbose output
npm run build -- --verbose

# Production build
NODE_ENV=production npm run build
```

### 3. Output Format

Report results in structured format:

```
## Build Results

**Status**: ❌ Failed / ✅ Success
**Mode**: Java/Gradle | JS/npm
**Duration**: X.Xs

### Errors (if any)

| File | Line | Error |
|------|------|-------|
| src/main/java/.../MusicService.java | 42 | cannot find symbol: variable title |

### Warnings

| Type | Count | Description |
|------|-------|-------------|
| Deprecation | 2 | Using deprecated API |
```

## Common Build Issues

### Java / Gradle

| Issue | Cause | Solution |
|-------|-------|----------|
| `cannot find symbol` | Missing import / wrong type | Check import statements |
| `package does not exist` | Dependency not declared | Add to `build.gradle` dependencies |
| `incompatible types` | Type mismatch | Fix type annotation |
| Gradle wrapper missing | Not committed | Run `gradle wrapper` to generate |

### JavaScript / npm

| Issue | Cause | Solution |
|-------|-------|----------|
| Module not found | Missing import/dependency | Check import path, install package |
| Type errors | TypeScript compilation | Fix type issues (see `/typecheck`) |
| Out of memory | Large build | Increase Node memory limit |

## Integration

- Final check in `qa` agent quality workflow
- Java: Runs after `/typecheck`, `/lint`, `/test`
- JS: Runs after `/typecheck`, `/eslint`, `/test`
- Success required before deployment
