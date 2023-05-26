<template>
  <AuthenticationWrapper>
    <TheHeader />
    <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_REVIEWER">
      <TheContent class="paper-section flex">
        <div class="col-12 text-left pb-0">
          <BackButton />
          <h1>"Quality Assurance"</h1>
          <div v-if="!waitingForData">
            <div class="card">
              <DataTable :value="resultData" class="table-cursor" id="qa-data-result" @row-click="getDataSet">
                <Column field="companyInformation.sector" header="DATA ID" :sortable="true" class="d-bg-white w-2">
                  <template #body="{ data }">
                    {{ data.dataId }}
                  </template>
                </Column>
                <Column field="companyInformation.sector" header="COMPANY NAME" :sortable="true" class="d-bg-white w-2">
                  <template #body="{ data }">
                    {{ data.companyInformation.companyName }}
                  </template>
                </Column>
                <Column field="companyInformation.sector" header="FRAMEWORK" :sortable="true" class="d-bg-white w-2">
                  <template #body="{ data }">
                    {{ data.metaInformation.dataType }}
                  </template>
                </Column>
                <Column
                  field="companyInformation.headquarters"
                  header="REPORTING PERIOD"
                  :sortable="true"
                  class="d-bg-white w-2"
                >
                  <template #body="{ data }">
                    {{ data.metaInformation.reportingPeriod }}
                  </template>
                </Column>
              </DataTable>
            </div>
          </div>
          <div v-else-if="waitingForData">
            <span>loading</span>
          </div>
          <div>
            <pre id="dataset-container">{{ datasetAsJson }}</pre>
          </div>
        </div>
        <MiddleCenterDiv class="col-12">
          <div>
            <PrimeButton label="Accept Dataset" />
          </div>
          <div>
            <PrimeButton label="Reject Dataset" />
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
import { CompanyInformation, DataMetaInformation, DataTypeEnum, LksgData, SfdrData } from "@clients/backend";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import AuthorizationWrapper from "@/components/wrapper/AuthorizationWrapper.vue";
import { KEYCLOAK_ROLE_REVIEWER } from "@/utils/KeycloakUtils";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
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
  computed: {
    datasetAsJson(): string {
      return JSON.stringify(this.dataSet, null, 2);
    },
  },
  props: {
    data: {
      type: Object,
      default: null,
    },
  },
  methods: {
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
        }

        console.log(this.resultData);
        console.log(this.resultData[0]);
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Retrieves the dataset corresponding to the given dataId
     * @param event
     * @param event.data
     */
    async getDataSet(event: { data: QaDataObject }) {
      try {
        const filteredData = event.data.metaInformation.dataType;
        const dataId = event.data.dataId;
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
            const singleLksgData = (await lksgDataControllerApi.getCompanyAssociatedLksgData(dataId).data
              .data) as LksgData;
          } catch (error) {
            console.error(error);
          }
        } else if (filteredData === DataTypeEnum.Sfdr) {
          try {
            const sfdrDataControllerApi = await new ApiClientProvider(
              assertDefined(this.getKeycloakPromise)()
            ).getSfdrDataControllerApi();

            const singleSfdrData = (await sfdrDataControllerApi.getCompanyAssociatedSfdrData(dataId)).data
              .data as SfdrData;
          } catch (error) {
            console.error(error);
          }
        }
      } catch (error) {
        console.error(error);
      }
    },
  },
});
interface QaDataObject {
  dataId: string;
  metaInformation: DataMetaInformation;
  companyInformation: CompanyInformation;
}
</script>

<style scoped>
pre#dataset-container {
  background: white;
  padding: 20px;
  border: 1px solid black;
}
</style>
