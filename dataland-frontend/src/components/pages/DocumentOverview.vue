<template>
  <TheHeader v-if="!useMobileView" />
  <TheContent class="paper-section flex">
    <div ref="sheet">
      <CompanyInfoSheet :company-id="companyId" :show-single-data-request-button="false" />

      <div class="styled-box">
        <span>Documents</span>
      </div>

      <span class="flex align-items-center">
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
          style="margin: 15px"
        />

        <span
          data-test="reset-filter"
          style="margin: 15px"
          class="ml-3 cursor-pointer text-primary font-semibold d-letters"
          @click="resetFilter"
          >RESET</span
        >
      </span>
    </div>
    <div class="col-12 text-left p-3">
      <div class="card">
        <DataTable v-if="documentsFiltered && documentsFiltered.length > 0" :value="documentsFiltered">
          <Column header="DOCUMENT NAME" field="documentName" :sortable="true" />
          <Column header="DOCUMENT TYPE" field="documentCategory" :sortable="true" />
          <Column header="PUBLICATION DATE" field="publicationDate" :sortable="true" />
          <Column header="REPORTING PERIOD" field="reportingPeriod" :sortable="true" />
          <Column field="documentType" header="" class="d-bg-white w-1 d-datatable-column-right">
            <template #body="documentProps">
              <span
                class="text-primary no-underline font-bold cursor-pointer"
                @click="openMetaInfoDialog(documentProps.data.documentId)"
                ><span> VIEW DETAILS</span> <span class="ml-3">></span>
              </span>
            </template>
          </Column>
          <Column field="documentType" header="" class="d-bg-white w-1 d-datatable-column-right">
            <template #body="document">
              <DocumentLink
                  :download-name=" 'DOWNLOAD'"
                  :file-reference="document.data.documentId"
                  show-icon
              />
            </template>
          </Column>
        </DataTable>
        <div class="d-center-div text-center px-7 py-4" v-else data-test="DataSearchNoResultsText">
          <p class="font-medium text-xl">We're sorry, but your search did not return any results.</p>
          <p class="font-medium text-xl">Please double-check the spelling and filter settings!</p>
          <p class="font-medium text-xl">
            It might be possible that the company you searched for does not have any documents on Dataland yet.
          </p>
        </div>
      </div>
    </div>
  </TheContent>
  <DocumentMetaDataDialog :dialog-visible.sync="isMetaInfoDialogOpen" :document-id="selectedDocumentId" />
  <TheFooter :is-light-version="true" :sections="footerContent" />
</template>

<script setup lang="ts">
import { Content, Page } from '@/types/ContentTypes.ts';
import contentData from '@/assets/content.json';
import { inject, onMounted, ref, watch } from 'vue';
import {DocumentCategorySelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes.ts';
import DataTable, { DataTablePageEvent } from 'primevue/datatable';
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
  DocumentMetaInfoResponse,
} from '@clients/documentmanager';
import type Keycloak from 'keycloak-js';
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";
import {humanizeStringOrNumber} from "@/utils/StringFormatter.ts";

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
const availableDocumentTypes = ref<Array<DocumentCategorySelectableItem>>((retrieveAvailableDocumentCategories()));
const rowsPerPage = 100;
const firstRowIndex = ref(0);
const currentChunkIndex = ref(0);
const isMetaInfoDialogOpen = ref(false);
const selectedDocumentId = ref<string>('');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

watch(selectedDocumentType, (newSelected) => {
  firstRowIndex.value = 0;
  currentChunkIndex.value = 0;
  console.log('Neu:', newSelected);
});
/**
 * Get list of documents
 */
async function getAllDocumentsForFilters(): Promise<void> {
  waitingForData.value = true;
  try {
    if (getKeycloakPromise) {
      const documentControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
        .documentController;
      if (selectedDocumentType.value) {
        const responses: DocumentMetaInfoResponse[][] = await Promise.all(
          selectedDocumentType.value.map(async (item) => {
            return (await documentControllerApi.searchForDocumentMetaInformation(props.companyId, item.documentCategoryDataType)).data;
          }))
        documentsFiltered.value= responses.flat();
      } else{
        documentsFiltered.value = (await documentControllerApi.searchForDocumentMetaInformation(props.companyId)).data;
      }
    }
  } catch (error) {
    console.error(error);
  }
  waitingForData.value = false;
}

/**
 * Resets filter
 */
function resetFilter(): void {
  selectedDocumentType.value = undefined;
}

/**
 * Updates the current Page
 * @param event DataTablePageEvent
 */
function onPage(event: DataTablePageEvent) {
  window.scrollTo(0, 0);
  if (event.page != currentChunkIndex.value) {
    currentChunkIndex.value = event.page;
    firstRowIndex.value = currentChunkIndex.value * rowsPerPage;
  }
}

function openMetaInfoDialog(documentId: string) {
  selectedDocumentId.value = documentId;
  isMetaInfoDialogOpen.value = true;
}

/**
 * Gets list with all available document categories
 * @returns array of availableDocumentTypes
 */
export function retrieveAvailableDocumentCategories(): Array<DocumentCategorySelectableItem> {
  return  Object.entries(DocumentMetaInfoDocumentCategoryEnum).map(
      ([, value]) => ({
        displayName: humanizeStringOrNumber(value),
        disabled: false,
        documentCategoryDataType:value
      })
  );
}

onMounted(() => {
  getAllDocumentsForFilters();
});
</script>

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
</style>
