<template>
  <TheHeader v-if="!useMobileView" />
  <TheContent class="paper-section flex">
    <CompanyInfoSheet :company-id="companyId" :show-single-data-request-button="false" />
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
    <span class="flex align-items-center">
      <span
        data-test="reset-filter"
        style="margin: 15px"
        class="ml-3 cursor-pointer text-primary font-semibold d-letters"
        @click="resetFilter"
        >RESET</span
      >
    </span>

    <div class="col-12 text-left p-3">
      <div class="card">
        <DataTable v-if="documentsFiltered && documentsFiltered.length > 0"
          :value="documentsFiltered"
        >
          <Column header="DOCUMENT NAME" field="documentName" :sortable="true" > </Column>
          <Column header="DOCUMENT TYPE" field="documentType" :sortable="true" />
          <Column header="PUBLICATION DATE" field="publicationDate" :sortable="true" />
          <Column field="documentType" header="" class="d-bg-white w-1 d-datatable-column-right">
            <template #body>
              <span class="text-primary no-underline font-bold"><span> VIEW DETAILS</span> <span class="ml-3">></span> </span>
            </template>
          </Column>
          <Column field="documentType" header="" class="d-bg-white w-1 d-datatable-column-right">
            <template #body>
              <span class="text-primary no-underline font-bold"><span> DOWNLOAD </span> <i class="pi pi-download pl-1" data-test="download-icon" aria-hidden="true" style="font-size: 12px" /> </span>
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
  <TheFooter :is-light-version="true" :sections="footerContent" />
</template>

<script setup lang="ts">
import { Content, Page } from '@/types/ContentTypes.ts';
import contentData from '@/assets/content.json';
import { inject, onMounted, ref } from 'vue';
import { SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes.ts';
import DataTable, { DataTablePageEvent } from 'primevue/datatable';
import Column from 'primevue/column';
import TheHeader from '@/components/generics/TheHeader.vue';
import TheContent from '@/components/generics/TheContent.vue';
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';

const props = defineProps<{
  companyId: string;
}>();

const content: Content = contentData;
const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
const footerContent = footerPage?.sections;
const waitingForData = ref(true);
const useMobileView = inject<boolean>('useMobileView', false);
const documentsFiltered = ref<{ documentName: string; documentType: string; publicationDate: string }[]>([]);
const selectedDocumentType = [] as Array<SelectableItem>;
const availableDocumentTypes = [
  { displayName: 'Annual report', disabled: false },
  { displayName: 'sustainability report', disabled: false },
];
const totalRecords = ref(0);
const rowsPerPage = 100;
const firstRowIndex = ref(0);
const currentChunkIndex = ref(0);

/**
 * Get list of documents
 */
async function getAllDocumentsForFilters() {
  waitingForData.value = true;
  //todo set documentsFiltered
  documentsFiltered.value = [
    { documentName: 'policy123', documentType: 'policy', publicationDate: '202-01-01' },
    { documentName: 'annual_report_2024_edited_20250101', documentType: 'annualreport', publicationDate: '202-01-01' },
  ];
  waitingForData.value = false;
}

/**
 * Resets filter
 */
function resetFilter(): void {
  selectedDocumentType.length = 0;
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

onMounted(() => {
  getAllDocumentsForFilters();
  console.log(documentsFiltered.value)
});
</script>

<style scoped lang="scss"></style>
