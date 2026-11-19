import { useMutation, type UseMutationReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { type CompanyIdentifierValidationResult } from '@clients/backend';

export interface PostCompanyValidationVariables {
  identifiers: string[];
}

/**
 * Vue Query mutation hook that resolves company identifiers to the matching companies
 * (POST /api/companies/validation).
 * The endpoint does not change server state, so nothing is invalidated. It is a mutation because it is triggered by an
 * explicit user action and its result is not shared via the cache.
 *
 * @returns Mutation result resolving to one {@link CompanyIdentifierValidationResult} per identifier.
 */
export function usePostCompanyValidationMutation(): UseMutationReturnType<
  CompanyIdentifierValidationResult[],
  Error,
  PostCompanyValidationVariables,
  unknown
> {
  const apiClientProvider = useApiClient();

  return useMutation({
    mutationFn: async ({ identifiers }: PostCompanyValidationVariables) => {
      const response = await apiClientProvider.backendClients.companyDataController.postCompanyValidation(identifiers);
      return response.data;
    },
  });
}
