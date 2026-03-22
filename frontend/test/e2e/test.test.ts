import { describe, expect, it } from 'vitest'
import { createPage, setup } from '@nuxt/test-utils/e2e'

describe('My e2e test', async () => {
  await setup({
    host: 'http://localhost:3000',
  })

  it('test', async () => {})
})
