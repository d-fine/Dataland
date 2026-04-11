import { inject, type ComputedRef } from 'vue';
import type { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';

/**
 * Helper that retrieves the global ApiClientProvider injected into the
 * current Vue app context from App.vue.
 * @returns {ApiClientProvider} The global ApiClientProvider instance.
 * @throws {Error} If the `apiClientProvider` injection is not present or undefined.
 */
export function useApiClient(): ApiClientProvider {
  const apiClientProviderRef = inject<ComputedRef<ApiClientProvider | undefined>>('apiClientProvider');
  const providerRef = assertDefined(apiClientProviderRef);
  return assertDefined(providerRef.value);
}
