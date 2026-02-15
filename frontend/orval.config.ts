import { defineConfig } from 'orval'

/**
 * Generate TypeScript API client from backend OpenAPI spec.
 * Uses openapi.yaml by default (committed; or run fetch-openapi with backend up then generate:api).
 * Override: INPUT_OPENAPI=http://localhost:8080/v3/api-docs npm run generate:api
 */
const inputTarget = process.env.INPUT_OPENAPI ?? './openapi.yaml'

export default defineConfig({
  api: {
    input: { target: inputTarget },
    output: {
      target: './src/api/generated/api.ts',
      client: 'react-query',
      override: {
        mutator: {
          path: './src/api/axios-instance.ts',
          name: 'customInstance',
        },
      },
    },
  },
})
