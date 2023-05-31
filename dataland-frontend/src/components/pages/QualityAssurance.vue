<template>
  <AuthenticationWrapper>
    <TheHeader />
    <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_REVIEWER">
      <TheContent class="paper-section flex">
        <div class="col-12 text-left pb-0">
          <BackButton />
          <h1>Quality Assurance</h1>
          <div v-if="!waitingForData">
            <div class="card">
              <DataTable :value="resultData" class="table-cursor" id="qa-data-result" :rowHover="true" @row-click="loadDatasetAndOpenModal" >
                <Column header="DATA ID" class="d-bg-white w-2">
                  <template #body="{ data }">
                    {{ data.dataId }}
                  </template>
                </Column>
                <Column header="COMPANY NAME" class="d-bg-white w-2">
                  <template #body="{ data }">
                    {{ data.companyInformation.companyName }}
                  </template>
                </Column>
                <Column header="FRAMEWORK" class="d-bg-white w-2">
                  <template #body="{ data }">
                    {{ humanizeString(data.metaInformation.dataType) }}
                  </template>
                </Column>
                <Column header="REPORTING PERIOD" class="d-bg-white w-2">
                  <template #body="{ data }">
                    {{ data.metaInformation.reportingPeriod }}
                  </template>
                </Column>
                <Column field="reviewDataset" header="" class="w-2 d-bg-white ">
                  <template #body="{ data }">
                    <router-link :to="loadDatasetAndOpenModal" class="text-primary no-underline font-bold">
                      <div class="text-right">
                        <span>REVIEW</span>
                        <span class="ml-3">></span>
                      </div>
                    </router-link>
                  </template>
                </Column>

              </DataTable>
            </div>
          </div>
              <div v-else-if="waitingForData" class="inline-loading text-center">
                  <p class="font-medium text-xl">Loading data to be reviewed...</p>
                  <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
              </div>
        </div>
        <MiddleCenterDiv class="col-12">
          <div>
            <PrimeButton @click="setQualityStatusToApproved" label="Accept Dataset" />
          </div>
          <div>
            <PrimeButton @click="setQualityStatusToRejected" label="Reject Dataset" />
          </div>
        </MiddleCenterDiv>
      </TheContent>
    </AuthorizationWrapper>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import BackButton from "@/components/general/BackButton.vue";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";
import TheFooter from "@/components/general/TheFooter.vue";
import PrimeButton from "primevue/button";
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { CompanyInformation, DataMetaInformation, DataTypeEnum } from "@clients/backend";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import AuthorizationWrapper from "@/components/wrapper/AuthorizationWrapper.vue";
import { KEYCLOAK_ROLE_REVIEWER } from "@/utils/KeycloakUtils";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import {humanizeString} from "@/utils/StringHumanizer";
import QADatasetModal from "@/components/general/QADatasetModal.vue";
import { AxiosError } from "axios";
export default defineComponent({
  name: "QualityAssurance",
  components: {
    AuthorizationWrapper,
    TheFooter,
    MiddleCenterDiv,
    BackButton,
    TheContent,
    TheHeader,
    AuthenticationWrapper,
    PrimeButton,
    DataTable,
    Column,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      dataIdList: [] as Array<string>,
      dataId: "",
      resultData: [] as QaDataObject[],
      waitingForData: true,
      dataSet: null | undefined,
      KEYCLOAK_ROLE_REVIEWER,
      metaInformation: null as DataMetaInformation,
      companyInformation: null as CompanyInformation | null,
    };
  },
  async mounted() {
    await this.getQaData();
  },
  props: {
    data: {
      type: Object,
      default: null,
    },
  },
  watch: {
    companyId() {
      this.dataId = "";
      void this.getDataSet;
    },
  },
  methods: {
    humanizeString,
    //TODO Discussion: Maybe only the first entry of the table should be clickable
    //TODO Buttons need to get functions, also should be disabled before a dataset is selected
    //TODO Add loading text / spinner to the page. Similar to the company result page
    //TODO Check that using non scoped style is fine
    //TODO Discussion: Should the Accept/Decline Button open a confirmation window asking if the user is sure to do the corresponding action
    //TODO Discussion What about reverting a decision?
    //TODO List of data Ids should be refreshed once a decision was made
    //TODO Include a button next to the My DataSet Button, only visible to a user with role Reviewer_Role
    //TODO Clean up code
    /**
     * Uses the dataland API to build the QaDataObject which is displayed on the quality assurance page
     */
    async getQaData() {
      try {
        const qaServiceControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getQaControllerApi();
        const response = await qaServiceControllerApi.getUnreviewedDatasets();
        this.dataIdList = response.data;
        for (const dataId of this.dataIdList) {
          await this.addDatasetAssociatedInformationToDisplayList(dataId);
        }
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Gathers meta and company information associated with a dataset and adds it to the list of displayed
     * datasets if the information can be retrieved
     * @param dataId the ID of the corresponding dataset
     */
    async addDatasetAssociatedInformationToDisplayList(dataId: string) {
      try {
        const metaDataInformationControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const metaDataResponse = await metaDataInformationControllerApi.getDataMetaInfo(dataId);
        this.metaInformation = metaDataResponse.data;
        const companyDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getCompanyDataControllerApi();
        const companyResponse = await companyDataControllerApi.getCompanyById(this.metaInformation.companyId);
        this.companyInformation = companyResponse.data.companyInformation;
        this.resultData.push({
          dataId: dataId,
          metaInformation: this.metaInformation,
          companyInformation: this.companyInformation,
        });
      } catch(error: AxiosError) {
        if(error.response.status !== 404) {
          throw error;
        }
      }
    },
    /**
     * Retrieves the dataset corresponding to the given dataId
     * @param data
     */
    async getDataSet(data: QaDataObject) {
      try {
        const filteredData = data.metaInformation.dataType;
        const dataId = data.dataId;
        if (filteredData === DataTypeEnum.EutaxonomyNonFinancials) {
          try {
            const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
              assertDefined(this.getKeycloakPromise)()
            ).getEuTaxonomyDataForNonFinancialsControllerApi();
            const companyAssociatedData =
              await euTaxonomyDataForNonFinancialsControllerApi.getCompanyAssociatedEuTaxonomyDataForNonFinancials(
                assertDefined(dataId)
              );
            this.dataSet = companyAssociatedData.data.data;
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
            this.dataSet = companyAssociatedData.data.data;
          } catch (error) {
            console.error(error);
          }
        } else if (filteredData === DataTypeEnum.Lksg) {
          try {
            const lksgDataControllerApi = await new ApiClientProvider(
              assertDefined(this.getKeycloakPromise)()
            ).getLksgDataControllerApi();
            const singleLksgData = await lksgDataControllerApi.getCompanyAssociatedLksgData(assertDefined(dataId));
            this.dataSet = singleLksgData.data.data;
          } catch (error) {
            console.error(error);
          }
        } else if (filteredData === DataTypeEnum.Sfdr) {
          try {
            const sfdrDataControllerApi = await new ApiClientProvider(
              assertDefined(this.getKeycloakPromise)()
            ).getSfdrDataControllerApi();

            const singleSfdrData = await sfdrDataControllerApi.getCompanyAssociatedSfdrData(dataId);
            this.dataSet = singleSfdrData.data.data;
          } catch (error) {
            console.error(error);
          }
        }
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Sets dataset to accepted
     */
    async setQualityStatusToApproved() {
      const qaServiceControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getQaControllerApi();
      await qaServiceControllerApi.assignQualityStatus(this.dataId, "Accepted");
    },
    /**
     * Sets dataset to rejected
     */
    async setQualityStatusToRejected() {
      const qaServiceControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getQaControllerApi();
      await qaServiceControllerApi.assignQualityStatus(this.dataId, "Rejected");
    },
    /**
     * Opens a modal to display a table with the provided list of production sites
     * @param event
     * @param event.data
     */
    loadDatasetAndOpenModal(event: { data: QaDataObject }) {
      this.getDataSet(event.data);
      this.$dialog.open(QADatasetModal, {
        props: {
          header: "Dataset to review",
          modal: true,
          dismissableMask: true,
        },
        data: {
          dataSetToReview: this.dataSet,
        },
      });
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
pre#dataset-container {
  background: white;
  padding: 20px;
  border: 1px solid black;
}

#qa-data-result tr:hover {
  cursor: pointer;
}
</style>
