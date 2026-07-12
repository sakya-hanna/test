# STU-PAR-01 Parent Notice Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver a testable parent-notice receipt page in Vue and a matching editable Figma Frame.

**Architecture:** Keep the existing student shell and route. Store demonstration content in the student mock module, keep page-only interaction state in `ParentNotice.vue`, verify the source contract and production build, then capture and normalize the rendered page in the existing Figma file.

**Tech Stack:** Vue 3, Vue Router, Tailwind CSS 4, Node test runner, Vite, Figma MCP.

---

## Chunk 1: Vue page

### Task 1: Add a failing page contract

**Files:**
- Create: `tests/parent-notice-contract.test.mjs`
- Modify: `package.json`

- [ ] Assert the page declares `STU-PAR-01`, contains the status summary, notice actions, receipt record, file input, and no placeholder copy.
- [ ] Update the test command to execute all `tests/*.test.mjs` files.
- [ ] Run `npm test` and confirm the new contract fails.

### Task 2: Add mock data and implement the page

**Files:**
- Modify: `src/mock/stu.js`
- Modify: `src/views/stu/ParentNotice.vue`

- [ ] Add focused `parentNoticeData` for internship, parent, notice, and receipt state.
- [ ] Build the status cards, two-column information/notice area, and receipt card.
- [ ] Add preview toggle, download mock, and file-selection feedback.
- [ ] Run `npm test` and confirm all contracts pass.
- [ ] Run `npm run build` and confirm the production bundle succeeds.

## Chunk 2: Figma delivery

### Task 3: Capture and verify the browser page

**Files:**
- Read: `src/views/stu/ParentNotice.vue`
- Read: `src/layouts/LayoutBackend.vue`

- [ ] Start Vite and visually verify `/stu/parent-notice` at desktop width.
- [ ] Use Figma MCP to capture the rendered route into file `vyOniy0QCWKZ5XunXuyzMk`.
- [ ] Rename the final editable frame to `STU-PAR-01：家长知情` and keep it clear of existing frames.
- [ ] Inspect the final screenshot for clipping, overlap, typography, and state visibility.
- [ ] Record the resulting Figma Node ID in project tracking documents.
