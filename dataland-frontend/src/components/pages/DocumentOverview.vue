<template>
  <TheHeader v-if="!useMobileView" />

  <div ref="sheet">
    <CompanyInfoSheet :company-id="companyId" :show-single-data-request-button="false" />
    <div style="text-align: left; margin: 0 1rem">
      <ChangeFrameworkDropdown
        :data-meta-information="dataMetaInformation"
        data-type="Documents"
        :company-id="companyId"
      />
      <FrameworkDataSearchDropdownFilter
        :disabled="waitingForData"
        v-model="selectedDocumentType"
        ref="DocumentTypeFilter"
        :available-items="availableDocumentTypes"
        filter-name="Types"
        data-test="document-type-picker"
        filter-id="document-type-filter"
        filter-placeholder="Search by document type"
        class="ml-3"
        style="margin: 1rem"
      />
      <span class="tertiary-button" data-test="reset-filter" @click="resetFilter">RESET</span>
    </div>
  </div>

  <TheContent class="paper-section flex flex-col p-3">
    <DataTable
      v-if="documentsFiltered && documentsFiltered.length > 0"
      data-test="documents-overview-table"
      :value="documentsFiltered"
      :paginator="true"
      :lazy="true"
      paginator-position="bottom"
      :total-records="totalRecords"
      :rows="rowsPerPage"
      :first="firstRowIndex"
      @sort="onSort($event)"
      @page="onPage($event)"
      :alwaysShowPaginator="true"
    >
      <Column header="DOCUMENT NAME" field="documentName" :sortable="true" />
      <Column header="DOCUMENT TYPE" field="documentCategory" :sortable="true">
        <template #body="slotProps">
          {{ humanizeStringOrNumber(slotProps.data.documentCategory) }}
        </template>
      </Column>
      <Column header="PUBLICATION DATE" field="publicationDate" :sortable="true">
        <template #body="slotProps">
          <div>
            {{ dateStringFormatter(slotProps.data.publicationDate) }}
          </div>
        </template>
      </Column>
      <Column header="REPORTING PERIOD" field="reportingPeriod" :sortable="true" />
      <Column field="documentType" header="" class="d-bg-white w-1 d-datatable-column-right">
        <template #body="documentProps">
          <a class="tertiary-button" @click="openMetaInfoDialog(documentProps.data.documentId)">
            VIEW DETAILS <span class="material-icons">arrow_forward_ios</span>
          </a>
        </template>
      </Column>
      <Column field="documentType" header="" class="d-bg-white w-1 d-datatable-column-right">
        <template #body="document">
          <DocumentLink
            :download-name="truncatedDocumentName(document.data)"
            :label="truncatedDocumentName(document.data) + ' (' + document.data.publicationDate + ')'"
            :file-reference="document.data.documentId"
            show-icon
            data-test="download-button"
          />
        </template>
      </Column>
    </DataTable>
    <div v-else class="centered-element-wrapper">
      <div class="text-content-wrapper">
        <p class="font-medium text-xl">The company you searched for does not have any documents on Dataland yet.</p>
      </div>
    </div>
  </TheContent>
  <DocumentMetaDataDialog v-model:isOpen="isMetaInfoDialogOpen" :document-id="selectedDocumentId" />
  <TheFooter :is-light-version="true" :sections="footerContent" />
</template>

<script setup lang="ts">
import { type Content, type Page } from '@/types/ContentTypes.ts';
import contentData from '@/assets/content.json';
import { inject, onMounted, ref, watch } from 'vue';
import { type DocumentCategorySelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes.ts';
import DataTable, { type DataTablePageEvent, type DataTableSortEvent } from 'primevue/datatable';
import Column from 'primevue/column';
import TheHeader from '@/components/generics/TheHeader.vue';
import TheContent from '@/components/generics/TheContent.vue';
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import DocumentMetaDataDialog from '@/components/resources/documentPage/DocumentMetaDataDialog.vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import {
  DocumentMetaInfoDocumentCategoryEnum,
  type DocumentMetaInfoResponse,
  type SearchForDocumentMetaInformationDocumentCategoriesEnum,
} from '@clients/documentmanager';
import type Keycloak from 'keycloak-js';
import DocumentLink from '@/components/resources/frameworkDataSearch/DocumentLink.vue';
import { humanizeStringOrNumber, truncatedDocumentName } from '@/utils/StringFormatter.ts';
import { dateStringFormatter } from '@/utils/DataFormatUtils';
import ChangeFrameworkDropdown from '@/components/generics/ChangeFrameworkDropdown.vue';
import type { DataMetaInformation } from '@clients/backend';

const props = defineProps<{
  companyId: string;
}>();

const content: Content = contentData;
const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
const footerContent = footerPage?.sections;
const waitingForData = ref(true);
const useMobileView = inject<boolean>('useMobileView', false);
const documentsFiltered = ref<DocumentMetaInfoResponse[]>([]);
const selectedDocumentType = ref<Array<DocumentCategorySelectableItem>>();
const availableDocumentTypes = ref<Array<DocumentCategorySelectableItem>>(retrieveAvailableDocumentCategories());
const rowsPerPage = 100;
const totalRecords = ref(0);
const firstRowIndex = ref(0);
const currentPage = ref(0);
const isMetaInfoDialogOpen = ref(false);
const selectedDocumentId = ref<string>('');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const sortField = ref<keyof DocumentMetaInfoResponse>('publicationDate');
const sortOrder = ref(1);
const dataMetaInformation = ref<DataMetaInformation[]>([]);

watch(selectedDocumentType, () => {
  firstRowIndex.value = 0;
  getAllDocumentsForFilters().catch((error) => console.error(error));
});

/**
 * Get list of documents using the filter for document category
 */
async function getAllDocumentsForFilters(): Promise<void> {
  waitingForData.value = true;
  try {
    const documentControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
      .documentController;
    documentsFiltered.value = (
      await documentControllerApi.searchForDocumentMetaInformation(
        props.companyId,
        selectedDocumentType.value ? convertToEnumSet(selectedDocumentType) : undefined
      )
    ).data;
    const backendClients = new ApiClientProvider(assertDefined(getKeycloakPromise)()).backendClients;
    const metaDataControllerApi = backendClients.metaDataController;
    const apiResponse = await metaDataControllerApi.getListOfDataMetaInfo(props.companyId);
    dataMetaInformation.value = apiResponse.data;
  } catch (error) {
    console.error(error);
  } finally {
    waitingForData.value = false;
    updateCurrentDisplayedData();
  }
}

/**
 * Resets filter
 */
function resetFilter(): void {
  selectedDocumentType.value = undefined;
}

/**
 * Updates the displayedDocumentData
 */
function updateCurrentDisplayedData(): void {
  documentsFiltered.value.sort((a, b) => customSorting(a, b));
  totalRecords.value = documentsFiltered.value.length;
  documentsFiltered.value = documentsFiltered.value.slice(
    rowsPerPage * currentPage.value,
    rowsPerPage * (1 + currentPage.value)
  );
  window.scrollTo({
    top: 0,
    behavior: 'smooth',
  });
}

/**
 * Compares two extended stored data requests (sort field, request status, last modified, company name)
 * @param a ExtendedStoredDataRequest to sort
 * @param b ExtendedStoredDataRequest to sort
 * @returns result of the comparison
 */
function customSorting(a: DocumentMetaInfoResponse, b: DocumentMetaInfoResponse): number {
  const aValue = a[sortField.value] ?? '';
  const bValue = b[sortField.value] ?? '';

  if (sortField.value != ('requestStatus' as keyof DocumentMetaInfoResponse)) {
    if (aValue < bValue) return -1 * sortOrder.value;
    if (aValue > bValue) return sortOrder.value;
    return 0;
  } else {
    return sortOrder.value;
  }
}

/**
 * Updates the current Page
 * @param event DataTablePageEvent
 */
function onPage(event: DataTablePageEvent): void {
  currentPage.value = event.page;
  updateCurrentDisplayedData();
}

/**
 * Sorts the list of storedDataRequests
 * @param event contains column to sort and sortOrder
 */
function onSort(event: DataTableSortEvent): void {
  sortField.value = event.sortField as keyof DocumentMetaInfoResponse;
  sortOrder.value = event.sortOrder ?? 1;
  updateCurrentDisplayedData();
}

/**
 * Opens the Dialog box with the meta information
 * @param documentId Id of selected document
 */
function openMetaInfoDialog(documentId: string): void {
  selectedDocumentId.value = documentId;
  isMetaInfoDialogOpen.value = true;
}

/**
 * Gets list with all available document categories
 * @returns array of availableDocumentTypes
 */
function retrieveAvailableDocumentCategories(): Array<DocumentCategorySelectableItem> {
  return Object.entries(DocumentMetaInfoDocumentCategoryEnum).map(([, value]) => ({
    displayName: humanizeStringOrNumber(value),
    disabled: false,
    documentCategoryDataType: value,
  }));
}

/**
 * Converts to enumSet
 * @returns Set<SearchForDocumentMetaInformationDocumentCategoriesEnum>
 */
function convertToEnumSet(
  selectedTypeRef: typeof selectedDocumentType
): Set<SearchForDocumentMetaInformationDocumentCategoriesEnum> {
  if (!selectedTypeRef.value) {
    return new Set<SearchForDocumentMetaInformationDocumentCategoriesEnum>();
  }
  return new Set(selectedTypeRef.value.map((item) => item.documentCategoryDataType));
}

onMounted(() => {
  getAllDocumentsForFilters().catch((error) => console.error(error));
});
</script>

<style>
/** This is used to turn off the search bar in the FrameworkDataSearchDropdownFilter, because there are only 4 elements here. **/
.p-multiselect-header {
  display: none !important;
}
</style>

<style scoped lang="scss">
.sheet {
  width: 100%;
  background-color: var(--surface-0);
  box-shadow: 0 4px 4px 0 #00000005;
  padding: 0.5rem 1rem 1rem;
  display: flex;
  flex-direction: column;
  align-items: start;
}

.styled-box {
  padding: 0.5rem 0.75rem;
  border-width: 2px;
  border-style: solid;
  border-color: #5a4f36;
  border-radius: 8px;
  margin-left: 15px;
  margin-top: 5px;
  background: #5a4f36;
  color: white;
  width: 200px;
}

.text-content-wrapper {
  margin: 4rem;
  text-align: center;
  padding: 2rem;
  background-color: var(--surface-0);
  display: flex;
  min-height: 200px;
  justify-content: center;
  max-width: 400px;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
}

.centered-element-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-grow: 1;
}
</style>
