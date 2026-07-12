# College Mock Duplicate Export Fix Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore production builds by keeping the single college-company mock dataset consumed by the current review page.

**Architecture:** Add a source-level regression contract around the college mock module, then remove only the obsolete duplicate export block. Preserve all current page-facing export names and business data.

**Tech Stack:** Node.js test runner, JavaScript ES modules, Vue 3, Vite.

---

## Chunk 1: Regression and minimal fix

### Task 1: Protect and repair the college mock module

**Files:**
- Create: `tests/college-mock-contract.test.mjs`
- Modify: `src/mock/col.js:1-16`

- [ ] **Step 1: Write the failing test**

Create a test that imports `src/mock/col.js`, asserts the module loads, asserts
`colCompanyReviewList` contains a `待初审` record, and asserts every record has a
`phone` field.

- [ ] **Step 2: Run the targeted test and verify RED**

Run: `node --test tests/college-mock-contract.test.mjs`

Expected: FAIL because parsing `src/mock/col.js` reports duplicated exports.

- [ ] **Step 3: Apply the minimal implementation**

Delete only the obsolete first `colCompanyReviewList` and
`colCompanyStatusMap` declarations at `src/mock/col.js:1-16`. Keep the later
`待初审`/`phone` dataset unchanged.

- [ ] **Step 4: Verify GREEN and full regression**

Run:

```text
node --test tests/college-mock-contract.test.mjs
npm test
npm run build
```

Expected: targeted test passes, all repository tests pass, and Vite build exits 0.

- [ ] **Step 5: Review diff**

Confirm no business flow, page component, or unrelated mock data changed.
