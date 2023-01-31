<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <BackButton id="backButton" label="CHOOSE COMPANY" />
      <CompanyInformation :companyID="companyID" />
      <Card class="col-12 text-left page-wrapper-card">
        <template #title> New Dataset - Framework </template>
        <template #content>
          <div class="uploadFormWrapper grid">
            <div id="euTaxonomyContainer" class="col-9 flex">
              <div id="euTaxonomyLabel" class="col-3 p-3">
                <h3>EU Taxonomy</h3>
                <p>Overview of all existing and missing EU Taxonomy datasets for this company.</p>
              </div>
              <div class="col-9 d-card">
                <div id="eutaxonomyDataSetsContainer">
                  <h4 class="bottom-border-section-dots">Eu Taxonomy Data Sets:</h4>

                  <MetaInfoPerCompanyAndFramework
                    title="Non-Financials"
                    :isFrontendUploadFormExisting="false"
                    :framework-url-path="DataTypeEnum.EutaxonomyNonFinancials"
                    description="Overview of all existing and missing SFDR datasets for this company"
                    :companyId="companyID"
                    :isWaitingForData="waitingForData"
                    :listOfFrameworkData="listOfEuTaxonomyNonFinancialsMetaInfo"
                    class="bottom-border-section-dots"
                  />

                  <MetaInfoPerCompanyAndFramework
                    title="Financials"
                    :isFrontendUploadFormExisting="false"
                    :framework-url-path="DataTypeEnum.EutaxonomyFinancials"
                    description="Overview of all existing and missing SFDR datasets for this company"
                    :companyId="companyID"
                    :isWaitingForData="waitingForData"
                    :listOfFrameworkData="listOfEuTaxonomyFinancialsMetaInfo"
                  />
                </div>
              </div>
            </div>

            <div id="sfdrContainer" class="col-9 flex top-border-section">
              <div id="sfdrLabel" class="col-3 p-3">
                <h3>SFDR</h3>
                <p>Overview of all existing and missing SFDR datasets for this company.</p>
              </div>
              <div class="col-9 d-card">
                <MetaInfoPerCompanyAndFramework
                  title="SFDR"
                  :isFrontendViewPageExisting="false"
                  :isFrontendUploadFormExisting="false"
                  :framework-url-path="DataTypeEnum.Sfdr"
                  description="Overview of all existing and missing SFDR datasets for this company"
                  :companyId="companyID"
                  :isWaitingForData="waitingForData"
                  :listOfFrameworkData="listOfSfdrMetaInfo"
                />
              </div>
            </div>

            <div id="lksgContainer" class="col-9 flex top-border-section">
              <div id="lksgLabel" class="col-3">
                <h3>LkSG</h3>
                <p>Overview of all existing and missing LkSG datasets for this company.</p>
              </div>
              <div class="col-9 d-card">
                <MetaInfoPerCompanyAndFramework
                  title="LkSG"
                  :framework-url-path="DataTypeEnum.Lksg"
                  :companyId="companyID"
                  :isWaitingForData="waitingForData"
                  :listOfFrameworkData="listOfLksgMetaInfo"
                />
              </div>
            </div>
          </div>
        </template>
      </Card>
    </TheContent>
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import BackButton from "@/components/general/BackButton.vue";
import Card from "primevue/card";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import { DataMetaInformation, DataTypeEnum } from "@clients/backend";
import MetaInfoPerCompanyAndFramework from "@/components/resources/chooseFrameworkForDataUpload/MetaInfoPerCompanyAndFramework.vue";

export default defineComponent({
  name: "Choose Framework",
  components: {
    CompanyInformation,
    AuthenticationWrapper,
    TheHeader,
    BackButton,
    TheContent,
    Card,
    MetaInfoPerCompanyAndFramework,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },

  mounted() {
    this.getMetaInfoAboutAllDataSetsForCurrentCompany();
  },

  data() {
    return {
      waitingForData: true,
      listOfEuTaxonomyNonFinancialsMetaInfo: [] as Array<DataMetaInformation>,
      listOfEuTaxonomyFinancialsMetaInfo: [] as Array<DataMetaInformation>,
      listOfSfdrMetaInfo: [] as Array<DataMetaInformation>,
      listOfLksgMetaInfo: [] as Array<DataMetaInformation>,
      DataTypeEnum,
    };
  },
  props: {
    companyID: {
      type: String,
    },
  },

  watch: {},
  methods: {
    async getMetaInfoAboutAllDataSetsForCurrentCompany() {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const response = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID);
        const listOfAllDataMetaInfo = response.data;
        this.listOfEuTaxonomyNonFinancialsMetaInfo = listOfAllDataMetaInfo.filter(
          (dataMetaInfo: DataMetaInformation) => dataMetaInfo.dataType === DataTypeEnum.EutaxonomyNonFinancials
        );
        this.listOfEuTaxonomyFinancialsMetaInfo = listOfAllDataMetaInfo.filter(
          (dataMetaInfo: DataMetaInformation) => dataMetaInfo.dataType === DataTypeEnum.EutaxonomyFinancials
        );
        this.listOfSfdrMetaInfo = listOfAllDataMetaInfo.filter(
          (dataMetaInfo: DataMetaInformation) => dataMetaInfo.dataType === DataTypeEnum.Sfdr
        );
        this.listOfLksgMetaInfo = listOfAllDataMetaInfo.filter(
          (dataMetaInfo: DataMetaInformation) => dataMetaInfo.dataType === DataTypeEnum.Lksg
        );
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>
