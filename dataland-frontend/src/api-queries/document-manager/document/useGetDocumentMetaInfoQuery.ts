import {computed, Ref} from "vue";
import {useQuery, UseQueryReturnType} from "@tanstack/vue-query";
import {useApiClient} from "@/utils/useApiClient.ts";
import {
    DocumentMetaInfoResponse,
} from "@clients/documentmanager";
import {documentKeys} from "@/api-queries/document-manager/document/documentKeys.ts";

export function useGetDocumentMetaInfoByCompanyIdQuery(companyId: Ref<string | undefined>): UseQueryReturnType<DocumentMetaInfoResponse[], Error> {
    const apiClientProvider = useApiClient();

    return useQuery({
        queryKey: computed(() => documentKeys.listByCompanyId(companyId.value)),
        queryFn: async () => {
            const id = companyId.value;
            if (!id) throw new Error('companyId is undefined');
            const apiController = apiClientProvider.apiClients.documentController;
            const response = await apiController.searchForDocumentMetaInformation(id, undefined, undefined, undefined, undefined);
            return response.data;
        },
        enabled: computed(() => !!companyId.value),
    });
}
