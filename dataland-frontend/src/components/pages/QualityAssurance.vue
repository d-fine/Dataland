<template>
  <AuthenticationWrapper>
    <TheHeader />
    <DatasetsTabMenu :initial-tab-index="2">
      <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_REVIEWER">
        <TheContent class="paper-section flex">
          <div class="col-12 text-left pb-0">
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
                @row-click="loadDatasetAndOpenModal($event)"
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
        </TheContent>
      </AuthorizationWrapper>
    </DatasetsTabMenu>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheFooter from "@/components/general/TheFooter.vue";
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import {
  CompanyDataControllerApiInterface,
  CompanyInformation,
  DataMetaInformation,
  DataTypeEnum,
  MetaDataControllerApiInterface,
} from "@clients/backend";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import AuthorizationWrapper from "@/components/wrapper/AuthorizationWrapper.vue";
import { KEYCLOAK_ROLE_REVIEWER } from "@/utils/KeycloakUtils";
import DataTable, { DataTablePageEvent, DataTableRowClickEvent } from "primevue/datatable";
import Column from "primevue/column";
import { humanizeString } from "@/utils/StringHumanizer";
import QADatasetModal from "@/components/general/QaDatasetModal.vue";
import { AxiosError } from "axios";
import DatasetsTabMenu from "@/components/general/DatasetsTabMenu.vue";
import { convertUnixTimeInMsToDateString } from "@/utils/DataFormatUtils";
import { QaControllerApi } from "@clients/qaservice";

export default defineComponent({
  name: "QualityAssurance",
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
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      dataIdList: [] as Array<string>,
      dataId: "",
      displayDataOfPage: [] as QaDataObject[],
      waitingForData: true,
      dataSet: null as unknown as object,
      KEYCLOAK_ROLE_REVIEWER,
      metaInformation: null as DataMetaInformation,
      companyInformation: null as CompanyInformation | null,
      qaServiceControllerApi: undefined as undefined | QaControllerApi,
      metaDataInformationControllerApi: undefined as undefined | MetaDataControllerApiInterface,
      companyDataControllerApi: undefined as undefined | CompanyDataControllerApiInterface,
      currentPage: 0,
    };
  },
  mounted() {
    this.getQaDataForCurrentPage().catch((error) => console.log(error));
  },
  methods: {
    convertUnixTimeInMsToDateString,
    humanizeString,
    /**
     * Uses the dataland API to build the QaDataObject which is displayed on the quality assurance page
     */
    async getQaDataForCurrentPage() {
      try {
        this.waitingForData = true;
        this.displayDataOfPage = [];
        const dataOfPage = [] as QaDataObject[];
        await this.gatherControllerApis();
        const response = await (this.qaServiceControllerApi as QaControllerApi).getUnreviewedDatasetsIds();
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
     * Gathers the controller APIs
     */
    async gatherControllerApis() {
      this.qaServiceControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getQaControllerApi();
      this.metaDataInformationControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getMetaDataControllerApi();
      this.companyDataControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getCompanyDataControllerApi();
    },
    /**
     * Gathers meta and company information associated with a dataset if the information can be retrieved
     * @param dataId the ID of the corresponding dataset
     * @returns a promise on the fetched data object
     */
    async addDatasetAssociatedInformationToDisplayList(dataId: string): Promise<QaDataObject> {
      try {
        const metaDataResponse = await (
          this.metaDataInformationControllerApi as MetaDataControllerApiInterface
        ).getDataMetaInfo(dataId);
        this.metaInformation = metaDataResponse.data;
        const companyResponse = await (
          this.companyDataControllerApi as CompanyDataControllerApiInterface
        ).getCompanyById(this.metaInformation.companyId);
        this.companyInformation = companyResponse.data.companyInformation;
        return {
          dataId: dataId,
          metaInformation: this.metaInformation,
          companyInformation: this.companyInformation,
        } as QaDataObject;
      } catch (error) {
        if (error instanceof AxiosError && error.response.status !== 404) {
          throw error;
        }
      }
    },
    /**
     * Retrieves the dataset corresponding to the given dataId
     * @param data is the quality assurance data object used to retrieve the actual dataset to be reviewed
     */
    async getDataSet(data: QaDataObject) {
      try {
        const filteredData = data.metaInformation.dataType;
        const dataId = data.dataId;
        this.dataId = dataId;
        if (filteredData === DataTypeEnum.EutaxonomyNonFinancials) {
          try {
            const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
              assertDefined(this.getKeycloakPromise)()
            ).getEuTaxonomyDataForNonFinancialsControllerApi();
            const companyAssociatedData =
              await euTaxonomyDataForNonFinancialsControllerApi.getCompanyAssociatedEuTaxonomyDataForNonFinancials(
                assertDefined(dataId)
              );
            this.dataSet = assertDefined(companyAssociatedData.data.data);
          } catch (error) {
            console.error(error);
          }
        } else if (filteredData === DataTypeEnum.EutaxonomyFinancials) {
          try {
            const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
              assertDefined(this.getKeycloakPromise)()
            ).getEuTaxonomyDataForFinancialsControllerApi();
            const companyAssociatedData =
              await euTaxonomyDataForFinancialsControllerApi.getCompanyAssociatedEuTaxonomyDataForFinancials(
                assertDefined(dataId)
              );
            this.dataSet = assertDefined(companyAssociatedData.data.data);
          } catch (error) {
            console.error(error);
          }
        } else if (filteredData === DataTypeEnum.Lksg) {
          try {
            const lksgDataControllerApi = await new ApiClientProvider(
              assertDefined(this.getKeycloakPromise)()
            ).getLksgDataControllerApi();
            const singleLksgData = await lksgDataControllerApi.getCompanyAssociatedLksgData(assertDefined(dataId));
            this.dataSet = assertDefined(singleLksgData.data.data);
          } catch (error) {
            console.error(error);
          }
        } else if (filteredData === DataTypeEnum.Sfdr) {
          try {
            const sfdrDataControllerApi = await new ApiClientProvider(
              assertDefined(this.getKeycloakPromise)()
            ).getSfdrDataControllerApi();

            const singleSfdrData = await sfdrDataControllerApi.getCompanyAssociatedSfdrData(dataId);
            this.dataSet = assertDefined(singleSfdrData.data.data);
          } catch (error) {
            console.error(error);
          }
        }
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Opens a modal to display a table with the provided list of production sites
     * @param event the event which triggers the method
     */
    async loadDatasetAndOpenModal(event: DataTableRowClickEvent) {
      const qaDataObject = event.data as QaDataObject;
      await this.getDataSet(qaDataObject);
      this.$dialog.open(QADatasetModal, {
        props: {
          header:
            "Reviewing " +
            qaDataObject.metaInformation.dataType +
            " data for " +
            qaDataObject.companyInformation.companyName +
            " for the reporting period " +
            qaDataObject.metaInformation.reportingPeriod,
          modal: true,
          dismissableMask: true,
        },
        data: {
          dataSetToReview: this.dataSet,
          dataId: this.dataId,
        },
        onClose: () => {
          this.getQaDataForCurrentPage().catch((error) => console.log(error));
        },
      });
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
