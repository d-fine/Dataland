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
              <DataTable :value="resultData">
                <Column
                  bodyClass="headers-bg"
                  headerStyle="width: 30vw;"
                  headerClass="horizontal-headers-size"
                  field="kpiKey"
                  header="KPIs"
                >
                </Column>
              </DataTable>
            </div>
            <span>{{ this.metaInformation.dataType }}</span>
            <span>{{ this.metaInformation.reportingPeriod }}</span>
            <span>{{ this.metaInformation.uploadTime }}</span>
            <span>{{ this.companyInformation.companyName }}</span>
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
  methods: {
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
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Uses the dataland API to retrieve the companyId of the first teaser company and the dataId
     * of the eutaxonomy-non-financials framework of that company.
     */
    /*async getDataSet() {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const apiResponse = await metaDataControllerApi.getDataMetaInfo(this.dataId);
        const filteredData = apiResponse.data.dataType;
        if (filteredData === DataTypeEnum.EutaxonomyNonFinancials) {
          try {
            this.waitingForData = true;
            if (this.dataId != "loading") {
              const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
                assertDefined(this.getKeycloakPromise)()
              ).getEuTaxonomyDataForNonFinancialsControllerApi();
              const companyAssociatedData =
                await euTaxonomyDataForNonFinancialsControllerApi.getCompanyAssociatedEuTaxonomyDataForNonFinancials(
                  assertDefined(this.dataId)
                );
              this.dataSet = companyAssociatedData.data.data;
              this.waitingForData = false;
            }
          } catch (error) {
            console.error(error);
          }
        } else if (filteredData === DataTypeEnum.EutaxonomyFinancials) {
          try {
            this.waitingForData = true;
            if (this.dataId != "loading") {
              const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
                assertDefined(this.getKeycloakPromise)()
              ).getEuTaxonomyDataForFinancialsControllerApi();
              const companyAssociatedData =
                await euTaxonomyDataForFinancialsControllerApi.getCompanyAssociatedEuTaxonomyDataForFinancials(
                  assertDefined(this.dataId)
                );
              this.dataSet = companyAssociatedData.data.data;
              this.waitingForData = false;
            }
          } catch (error) {
            console.error(error);
          }
        } else if (filteredData === DataTypeEnum.Lksg) {
          try {
            this.waitingForData = true;
            const lksgDataControllerApi = await new ApiClientProvider(
              assertDefined(this.getKeycloakPromise)()
            ).getLksgDataControllerApi();
            const singleLksgData = (await lksgDataControllerApi.getCompanyAssociatedLksgData(this.dataId)).data
              .data as LksgData;

            this.waitingForData = false;
          } catch (error) {
            console.error(error);
          }
        } else if (filteredData === DataTypeEnum.Sfdr) {
          try {
            this.waitingForData = true;
            const sfdrDataControllerApi = await new ApiClientProvider(
              assertDefined(this.getKeycloakPromise)()
            ).getSfdrDataControllerApi();

            const singleSfdrData = (await sfdrDataControllerApi.getCompanyAssociatedSfdrData(this.dataId)).data
              .data as SfdrData;
            this.waitingForData = false;
          } catch (error) {
            console.error(error);
          }
        }
      } catch (error) {
        console.error(error);
      }
    },
  },
  */
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
