import { QueryClient } from '@tanstack/vue-query';

/**
 * Creates a QueryClient for a single component test mount.
 * Retries are disabled so that error states surface immediately and failing tests fail fast.
 * @returns a fresh QueryClient with an empty cache
 */
export function createTestQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
}
