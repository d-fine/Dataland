import { computed, inject, ref, watch, type ComputedRef, type Ref } from 'vue';
import { QualityOptions } from '@clients/backend';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import { useGetDocumentMetaInfoByCompanyIdQuery } from '@/api-queries/document-manager/document/useGetDocumentMetaInfoQuery.ts';
import { isPageReferenceValid } from '@/utils/ValidationUtils.ts';
import type {
  ExtendedDataPointMetaInfoType,
  ExtendedDataPointType,
} from '@/components/resources/dataTable/conversion/Utils.ts';

export const qualityOptionsList = Object.values(QualityOptions).map((value) => ({ label: value, value }));

export interface ExtendedDataPointForm {
  companyId: string | undefined;
  chosenQuality: Ref<string | undefined>;
  selectedDocument: Ref<string | null>;
  insertedComment: Ref<string | undefined>;
  insertedPage: Ref<string | null>;
  availableDocuments: ComputedRef<{ label: string; value: string }[]>;
  selectedDocumentMetaInformation: ComputedRef<DocumentMetaInfoResponse | null>;
  isPageValid: ComputedRef<boolean>;
  getFormData: () => ExtendedDataPointMetaInfoType;
}

/**
 * Holds the state and logic shared by the stacked and the inline variant of the extended data point form field:
 * quality, data source (document and page) and comment, including the loading of the company's documents.
 * @param extendedDataPointObject the data point object used to prefill the form
 * @returns the form state and helpers
 */
export function useExtendedDataPointForm(extendedDataPointObject?: ExtendedDataPointType): ExtendedDataPointForm {
  const companyId = inject<string>('companyId');
  const chosenQuality = ref<string | undefined>(extendedDataPointObject?.quality ?? undefined);
  const selectedDocument = ref<string | null>(extendedDataPointObject?.dataSource?.fileReference ?? null);
  const insertedComment = ref<string | undefined>(extendedDataPointObject?.comment ?? undefined);
  const insertedPage = ref<string | null>(extendedDataPointObject?.dataSource?.page ?? null);

  const { data: documents, isFetched } = useGetDocumentMetaInfoByCompanyIdQuery(ref(companyId));
  const allDocuments = computed<DocumentMetaInfoResponse[]>(() => documents.value ?? []);
  const availableDocuments = computed(() =>
    allDocuments.value
      .filter((doc) => doc.documentName && doc.documentId)
      .map((doc) => ({ label: doc.documentName!, value: doc.documentId }))
  );
  const selectedDocumentMetaInformation = computed(
    () => allDocuments.value.find((doc) => doc.documentId === selectedDocument.value) ?? null
  );

  /**
   * Whether the currently entered page reference is valid. An empty value is considered valid, since the page field is optional.
   */
  const isPageValid = computed<boolean>(() => {
    const trimmedPage = insertedPage.value?.trim();
    return !trimmedPage || isPageReferenceValid(trimmedPage);
  });

  /**
   * Clears the selected document (and with it the page) if it is not one of the company's documents.
   */
  function dropSelectionIfDocumentUnknown(): void {
    if (!selectedDocumentMetaInformation.value) {
      selectedDocument.value = null;
      insertedPage.value = null;
    }
  }

  watch(selectedDocument, () => {
    if (!selectedDocumentMetaInformation.value) insertedPage.value = null;
  });
  watch(
    isFetched,
    (fetched) => {
      if (fetched) dropSelectionIfDocumentUnknown();
    },
    { immediate: true }
  );

  /**
   * Gathers the current form data into an object.
   * @returns An object containing the current form data.
   */
  function getFormData(): ExtendedDataPointMetaInfoType {
    return {
      quality: chosenQuality.value ?? undefined,
      comment: insertedComment.value ?? undefined,
      dataSource: {
        page: insertedPage.value?.trim() || undefined,
        fileReference: selectedDocument.value ?? undefined,
      },
    };
  }

  return {
    companyId,
    chosenQuality,
    selectedDocument,
    insertedComment,
    insertedPage,
    availableDocuments,
    selectedDocumentMetaInformation,
    isPageValid,
    getFormData,
  };
}
