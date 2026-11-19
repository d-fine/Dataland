import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { documentKeys } from '@/api-queries/document-manager/document/documentKeys';
import {
  type DocumentMetaInfoResponse,
  type SearchForDocumentMetaInformationDocumentCategoriesEnum,
} from '@clients/documentmanager';

/**
 * Vue Query hook that searches the meta information of documents (GET /documents/).
 * The query is disabled while `companyId`, `documentCategories` and `reportingPeriod` are all empty, because an
 * unfiltered request lists all documents on Dataland.
 *
 * @param params - Endpoint parameters. Undefined filters are not applied.
 * @param params.companyId - Only return documents associated with this company.
 * @param params.documentCategories - Only return documents of these categories.
 * @param params.reportingPeriod - Only return documents of this reporting period.
 * @param params.chunkSize - Maximum number of returned documents.
 * @param params.chunkIndex - Index of the returned chunk.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the matching {@link DocumentMetaInfoResponse}s.
 */
export function useSearchForDocumentMetaInformationQuery(
  params: {
    companyId?: MaybeRefOrGetter<string | undefined>;
    documentCategories?: MaybeRefOrGetter<SearchForDocumentMetaInformationDocumentCategoriesEnum[] | undefined>;
    reportingPeriod?: MaybeRefOrGetter<string | undefined>;
    chunkSize?: MaybeRefOrGetter<number | undefined>;
    chunkIndex?: MaybeRefOrGetter<number | undefined>;
  },
  options: Omit<UseQueryOptions<DocumentMetaInfoResponse[], Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<DocumentMetaInfoResponse[], Error> {
  const apiClientProvider = useApiClient();

  return useQuery<DocumentMetaInfoResponse[], Error>({
    queryKey: computed(() =>
      documentKeys.search({
        companyId: toValue(params.companyId),
        documentCategories: toValue(params.documentCategories),
        reportingPeriod: toValue(params.reportingPeriod),
        chunkSize: toValue(params.chunkSize),
        chunkIndex: toValue(params.chunkIndex),
      })
    ),
    queryFn: async () => {
      const documentCategories = toValue(params.documentCategories);
      const response = await apiClientProvider.apiClients.documentController.searchForDocumentMetaInformation(
        toValue(params.companyId),
        documentCategories ? new Set(documentCategories) : undefined,
        toValue(params.reportingPeriod),
        toValue(params.chunkSize),
        toValue(params.chunkIndex)
      );
      return response.data;
    },
    enabled: computed(
      () =>
        !!toValue(params.companyId) ||
        (toValue(params.documentCategories)?.length ?? 0) > 0 ||
        !!toValue(params.reportingPeriod)
    ),
    ...options,
  });
}
