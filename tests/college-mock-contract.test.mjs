import assert from 'node:assert/strict'
import test from 'node:test'

import { colCompanyReviewList } from '../src/mock/col.js'

test('college company review mock matches the page contract', () => {
  assert.ok(colCompanyReviewList.some(({ status }) => status === '待初审'))
  assert.ok(colCompanyReviewList.every(({ phone }) => typeof phone === 'string' && phone.length > 0))
})
