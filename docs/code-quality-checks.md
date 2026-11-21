# Code Quality Checks

This document explains how to run the code-formatting and code-quality
checks for the backend and Angular frontend.

The goal is to keep formatting consistent across the team and catch
common issues before code is merged.

## Overview

  -----------------------------------------------------------------------
  Area                    Tool                    Purpose
  ----------------------- ----------------------- -----------------------
  Backend                 Spotless (Gradle)       Format and verify
                                                  backend code

  Frontend                Prettier                Format and verify
                                                  frontend files

  Frontend                Angular ESLint          Detect common
                                                  TypeScript and Angular
                                                  template issues

  CI                      GitHub Actions          Run the checks
                                                  automatically on pull
                                                  requests and pushes
  -----------------------------------------------------------------------

The formatter and linter configuration files are committed to the
repository so that everyone uses the same rules.

------------------------------------------------------------------------

## 1. Backend: Spotless

Run these commands from the repository root (the directory containing
`gradlew`).

### Apply formatting

``` bash
./gradlew spotlessApply
```

This applies the configured Spotless formatting rules and may modify
files. Review the changes and commit them.

### Check formatting

``` bash
./gradlew spotlessCheck
```

This checks whether files comply with the Spotless rules without
modifying them. The command fails if formatting violations are found.

### Typical workflow

``` bash
cd setora-backend
./gradlew spotlessApply
./gradlew spotlessCheck
```

------------------------------------------------------------------------

## 2. Frontend: Prettier

Run these commands from the frontend directory (the directory containing
`package.json`).

### Apply formatting

``` bash
npm run format
```

This runs Prettier in write mode and may modify files. Review and commit
the changes.

### Check formatting

``` bash
npm run format:check
```

This checks formatting without changing files. It exits with an error if
files need formatting.

### Configuration

Prettier's shared rules are stored in the frontend's `.prettierrc` file.
Files and directories that should not be formatted are listed in
`.prettierignore`.

------------------------------------------------------------------------

## 3. Frontend: Angular ESLint

Run this from the frontend directory:

``` bash
npm run lint
```

This runs the Angular project's configured ESLint target. It checks the
TypeScript and Angular template files covered by the project's ESLint
configuration.

The shared rules are defined in `eslint.config.js` (or the ESLint
configuration file used by this project).

If linting reports an issue, fix the code and run the command again. Do
not disable a rule just to make the check pass unless the team agrees
that the rule is inappropriate.

------------------------------------------------------------------------

## 4. Run all frontend checks

From the frontend directory:

``` bash
npm run quality
```

This runs the formatting check and linting in sequence:

``` bash
npm run format:check
npm run lint
```

`npm run quality` is a verification command: it does not automatically
reformat files. If the formatting check fails, run `npm run format`,
review the resulting changes, and then rerun `npm run quality`.

------------------------------------------------------------------------

## 5. GitHub Actions

The workflow is stored under:

``` text
.github/workflows/frontend-quality.yml
```

It installs the frontend dependencies with `npm ci`, then runs the
Prettier check and Angular ESLint.

The workflow reports each step separately in the GitHub Actions run. A
failed formatting or linting step causes the job to fail.

> GitHub Actions provides automated feedback, but it does not format
> files or fix lint issues for you. Make the fixes locally and push the
> updated changes.


------------------------------------------------------------------------

## 6. Suggested checks before opening a pull request

From the repository root, run the backend checks:

``` bash
./gradlew spotlessApply
./gradlew spotlessCheck
```

Then run the frontend checks:

``` bash
cd frontend
npm ci
npm run quality
```