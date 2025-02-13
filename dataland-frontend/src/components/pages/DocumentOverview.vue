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
        <DataTable
            v-if="documentsFiltered && documentsFiltered.length > 0"
            v-show="!waitingForData"
            ref="dataTable"
            data-test="requests-datatable"
            :value="documentsFiltered"
            :paginator="true"
            :lazy="true"
            :total-records="totalRecords"
            :rows="rowsPerPage"
            :first="firstRowIndex"
            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
            :alwaysShowPaginator="false"
            currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
            @page="onPage($event)"
            class="table-cursor"
            id="admin-request-overview-data"
            :rowHover="true"
            style="cursor: pointer"
        >
          <Column header="DOCUMENT NAME" field="documentName" :sortable="true">
            <template #body="slotProps">
              {{ slotProps.data.documentName }}
            </template>
          </Column>
        </DataTable>

      </div>
    </div>
  </TheContent>
  <TheFooter :is-light-version="true" :sections="footerContent" />
</template>

<script setup lang="ts">
import { Content, Page } from '@/types/ContentTypes.ts';
import contentData from '@/assets/content.json';
import { inject, onMounted, reactive, ref } from 'vue';
import { DocumentMetaInfo } from '@clients/documentmanager';
import { SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes.ts';
import { DataTablePageEvent } from 'primevue/datatable';
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import CompanyInfoSheet from "@/components/general/CompanyInfoSheet.vue";
import TheFooter from "@/components/generics/TheFooter.vue";
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';

const props = defineProps<{
  companyId: string;
}>();

const content: Content = contentData;
const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
const footerContent = footerPage?.sections;
const waitingForData = ref(true);
const useMobileView = inject<boolean>('useMobileView', false);
const documentsFiltered = reactive([]) as Array<DocumentMetaInfo>;
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
});
</script>

<style scoped lang="scss"></style>
