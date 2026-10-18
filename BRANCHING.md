# Branching strategy

Environment-based Git flow for **dynamic-crud-api**.

Promotion path: `feature/*` → `develop` → `qa` → `main`.

```text
feature/* ──PR──► develop ──PR──► qa ──PR──► main
                     │             │           │
                    Dev           QA        Production
```

## Long-lived branches

| Branch | Environment | Purpose |
|--------|-------------|---------|
| `develop` | Dev | Integration branch; default target for feature work |
| `qa` | QA | Release candidate for QA testing |
| `main` | Production | Production-ready code only |

## Short-lived branches

| Pattern | Branched from | Merges into |
|---------|---------------|-------------|
| `feature/<short-name>` | `develop` | `develop` |
| `bugfix/<short-name>` | usually `develop` | `develop` (then promote) |
| `hotfix/<short-name>` | `main` | `main`, then sync to `qa` and `develop` |

Naming: lowercase kebab-case — e.g. `feature/add-order-entity`, `bugfix/null-id-lookup`, `hotfix/cors-crash`.

## Promotion rules

1. All new work starts on `develop` via `feature/*` or `bugfix/*` PRs.
2. Promote `develop` → `qa` when a set of changes is ready for QA.
3. Promote `qa` → `main` only after QA approval.
4. Never commit features directly to `qa` or `main`.
5. Hotfix: branch from `main` → PR into `main` → merge or cherry-pick into `qa` and `develop`.

## PR rules

### Allowed targets

| From | To | Allowed |
|------|-----|---------|
| `feature/*`, `bugfix/*` | `develop` | Yes |
| `feature/*`, `bugfix/*` | `qa` or `main` | No |
| `develop` | `qa` | Yes (promotion) |
| `develop` | `main` | No (must go through `qa`) |
| `qa` | `main` | Yes (release) |
| `hotfix/*` | `main` | Yes, then sync down |

### Title format

- Feature: `feat: add order entity`
- Bugfix: `fix: handle null id on lookup`
- Hotfix: `hotfix: restore CORS on login`
- Promotion: `promote: develop to qa` / `release: qa to main`

### Before merge

1. Description includes what changed, why, how to test, and risk/rollback notes for `qa` → `main`.
2. Branch is up to date with the target.
3. Build passes (`mvn test` / `mvn package`).
4. No secrets in the diff.
5. One concern per PR.
6. Team may review; **only the owner merges**. For `main`, include QA sign-off in the PR.
7. Delete `feature/*`, `bugfix/*`, and `hotfix/*` after merge.

### Merge strategy

| PR | Strategy |
|----|----------|
| `feature/*` / `bugfix/*` → `develop` | Squash merge |
| `develop` → `qa` | Merge commit |
| `qa` → `main` | Merge commit |
| `hotfix/*` → `main` | Squash or merge commit, then sync to `qa` / `develop` |

### PR checklist

```markdown
## Summary
- 

## Test plan
- [ ] `mvn test` passes
- [ ] Manual API check for affected endpoints
- [ ] No secrets in diff

## Target
- [ ] Correct base branch (`develop` / `qa` / `main`)
```

## Access control

| Action | Who |
|--------|-----|
| Create new branches (`feature/*`, `bugfix/*`, `hotfix/*`, …) | Any collaborator |
| Open PRs into `develop` / `qa` / `main` | Any collaborator |
| Merge PRs into `develop`, `qa`, or `main` | Owner only — Ahmed Farrag |
| Direct push to `develop`, `qa`, `main` | Disabled (PR + owner merge only) |
| Delete / force-push protected branches | Owner only |

When a remote (e.g. GitHub) is configured, protect `main`, `develop`, and `qa`: require PRs, restrict merges to the owner, and allow all collaborators to create and push topic branches.

## Day-to-day workflow

```bash
git checkout develop
git pull
git checkout -b feature/add-order-entity
# work, commit…
# PR: feature/add-order-entity → develop  (squash)
# later PR: develop → qa                 (merge commit)
# later PR: qa → main                    (merge commit, after QA sign-off)
```
