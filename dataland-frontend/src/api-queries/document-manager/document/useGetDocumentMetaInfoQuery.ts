import { computed, type Ref } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { type DocumentMetaInfoResponse } from '@clients/documentmanager';
import { documentKeys } from '@/api-queries/document-manager/document/documentKeys';

/**
 * Fetch meta information of documents related to a company by its company id.
 *
 * @param {Ref<string | undefined>} companyId - Reactive ref with the company id.
 * @returns {UseQueryReturnType<DocumentMetaInfoResponse[], Error>} Vue Query result; `response.data` holds
 *   the backend response. The query is disabled when `companyId.value` is falsy.
 */
export function useGetDocumentMetaInfoByCompanyIdQuery(
  companyId: Ref<string | undefined>
): UseQueryReturnType<DocumentMetaInfoResponse[], Error> {
  const apiClientProvider = useApiClient();
  const queryKey = computed(() => documentKeys.listByCompanyId(companyId.value));
  const enabled = computed(() => !!companyId.value);
  return useQuery<DocumentMetaInfoResponse[], Error>({
    queryKey,
    enabled,
    queryFn: async () => {
      const { documentController } = apiClientProvider.apiClients;
      const id = companyId.value;
      if (!id) throw new Error('companyId is undefined');
      const { data } = await documentController.searchForDocumentMetaInformation(
        id,
        undefined,
        undefined,
        undefined,
        undefined
      );
      return data;
    },
  });
}
