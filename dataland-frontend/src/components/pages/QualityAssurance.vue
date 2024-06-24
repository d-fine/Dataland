<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section flex">
      <DatasetsTabMenu :initial-tab-index="2">
        <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_REVIEWER">
          <div class="col-12 text-left p-3">
            <h1>Quality Assurance</h1>
            <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
              <p class="font-medium text-xl">Loading data to be reviewed...</p>
              <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
            </div>
            <div class="card">
              <DataTable
                :value="displayDataOfPage"
                class="table-cursor"
                id="qa-data-result"
                :rowHover="true"
                data-test="qa-review-section"
                @row-click="goToQaViewPage($event)"
                paginator
                paginator-position="top"
                :rows="datasetsPerPage"
                lazy
                :total-records="dataIdList.length"
                @page="onPage($event)"
              >
                <Column header="DATA ID" class="d-bg-white w-2 qa-review-id">
                  <template #body="slotProps">
                    {{ slotProps.data.dataId }}
                  </template>
                </Column>
                <Column header="COMPANY NAME" class="d-bg-white w-2 qa-review-company-name">
                  <template #body="slotProps">
                    {{ slotProps.data.companyInformation.companyName }}
                  </template>
                </Column>
                <Column header="FRAMEWORK" class="d-bg-white w-2 qa-review-framework">
                  <template #body="slotProps">
                    {{ humanizeString(slotProps.data.metaInformation.dataType) }}
                  </template>
                </Column>
                <Column header="REPORTING PERIOD" class="d-bg-white w-2 qa-review-reporting-period">
                  <template #body="slotProps">
                    {{ slotProps.data.metaInformation.reportingPeriod }}
                  </template>
                </Column>
                <Column header="SUBMISSION DATE" class="d-bg-white w-2 qa-review-submission-date">
                  <template #body="slotProps">
                    {{ convertUnixTimeInMsToDateString(slotProps.data.metaInformation.uploadTime) }}
                  </template>
                </Column>
                <Column field="reviewDataset" header="" class="w-2 d-bg-white qa-review-button">
                  <template #body>
                    <div class="text-right text-primary no-underline font-bold">
                      <span>REVIEW</span>
                      <span class="ml-3">></span>
                    </div>
                  </template>
                </Column>
              </DataTable>
            </div>
          </div>
        </AuthorizationWrapper>
      </DatasetsTabMenu>
    </TheContent>
    <TheFooter :is-light-version="true" :sections="footerContent" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheFooter from '@/components/generics/TheNewFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';
import TheContent from '@/components/generics/TheContent.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { defineComponent, inject } from 'vue';
import { type CompanyInformation, type DataMetaInformation } from '@clients/backend';
import { type ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import AuthorizationWrapper from '@/components/wrapper/AuthorizationWrapper.vue';
import { KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakUtils';
import DataTable, { type DataTablePageEvent, type DataTableRowClickEvent } from 'primevue/datatable';
import Column from 'primevue/column';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';

export default defineComponent({
  name: 'QualityAssurance',
  components: {
    DatasetsTabMenu,
    AuthorizationWrapper,
    TheFooter,
    TheContent,
    TheHeader,
    AuthenticationWrapper,
    DataTable,
    Column,
  },
  setup() {
    return {
      datasetsPerPage: 10,
      apiClientProvider: inject<ApiClientProvider>('apiClientProvider'),
    };
  },
  data() {
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    const footerContent = footerPage?.sections;
    return {
      dataIdList: [] as Array<string>,
      displayDataOfPage: [] as QaDataObject[],
      waitingForData: true,
      KEYCLOAK_ROLE_REVIEWER,
      metaInformation: null as DataMetaInformation | null,
      companyInformation: null as CompanyInformation | null,
      currentPage: 0,
      footerContent,
    };
  },
  mounted() {
    this.getQaDataForCurrentPage().catch((error) => console.log(error));
  },
  methods: {
    convertUnixTimeInMsToDateString,
    humanizeString: humanizeStringOrNumber,
    /**
     * Uses the dataland API to build the QaDataObject which is displayed on the quality assurance page
     */
    async getQaDataForCurrentPage() {
      try {
        this.waitingForData = true;
        this.displayDataOfPage = [];
        const dataOfPage = [] as QaDataObject[];
        const response = await assertDefined(this.apiClientProvider).apiClients.qaController.getUnreviewedDatasetsIds();
        this.dataIdList = response.data;
        const firstDatasetOnPageIndex = this.currentPage * this.datasetsPerPage;
        const dataIdsOnPage = this.dataIdList.slice(
          firstDatasetOnPageIndex,
          firstDatasetOnPageIndex + this.datasetsPerPage
        );
        for (const dataId of dataIdsOnPage) {
          dataOfPage.push(await this.addDatasetAssociatedInformationToDisplayList(dataId));
        }
        this.displayDataOfPage = dataOfPage;
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Gathers meta and company information associated with a dataset if the information can be retrieved
     * @param dataId the ID of the corresponding dataset
     * @returns a promise on the fetched data object
     */
    async addDatasetAssociatedInformationToDisplayList(dataId: string): Promise<QaDataObject> {
      const metaDataResponse = await assertDefined(
        this.apiClientProvider
      ).backendClients.metaDataController.getDataMetaInfo(dataId);
      this.metaInformation = metaDataResponse.data;
      const companyResponse = await assertDefined(
        this.apiClientProvider
      ).backendClients.companyDataController.getCompanyById(this.metaInformation.companyId);
      this.companyInformation = companyResponse.data.companyInformation;
      return {
        dataId: dataId,
        metaInformation: this.metaInformation,
        companyInformation: this.companyInformation,
      };
    },
    /**
     * Navigates to the view framework data page on a click on the row of the company
     * @param event the row click event
     * @returns the promise of the router push action
     */
    goToQaViewPage(event: DataTableRowClickEvent) {
      const qaDataObject = event.data as QaDataObject;
      const qaUri = `/companies/${qaDataObject.metaInformation.companyId}/frameworks/${qaDataObject.metaInformation.dataType}/${qaDataObject.dataId}`;
      return this.$router.push(qaUri);
    },
    /**
     * Updates the data for the current page
     * @param event event containing the new page
     */
    async onPage(event: DataTablePageEvent) {
      this.currentPage = event.page;
      await this.getQaDataForCurrentPage();
    },
  },
});
interface QaDataObject {
  dataId: string;
  metaInformation: DataMetaInformation;
  companyInformation: CompanyInformation;
}
</script>

<style>
#qa-data-result tr:hover {
  cursor: pointer;
}
</style>
