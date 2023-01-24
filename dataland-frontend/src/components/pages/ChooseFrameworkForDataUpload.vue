<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <BackButton id="backButton" label="CHOOSE COMPANY" />
      <CompanyInformation :companyID="companyID" />
      <Card class="col-12">
        <template #title>
          New Dataset - Framework
          <hr />
        </template>
        <template #content>
          <div class="uploadFormWrapper">
            <div id="euTaxonomyContainer" class="grid">
              <div id="euTaxonomyLabel" class="col-3 text-left topicLabel">
                <h3>EU Taxonomy</h3>
                <p>Overiew of all existing and missing EU Taxonomy datasets for this company.</p>
              </div>
              <div class="col-6 text-left uploadForm">
                <div id="eutaxonomyDataSetsContainer" class="mt-6">
                  Eu Taxonomy Data Sets:
                  <div v-if="waitingForData" class="inline-loading meta-data-height text-center">
                    <p class="font-medium text-xl">Loading...</p>
                    <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
                  </div>
                  <div v-else v-for="(dataMetaInfo, index) in listOfEuTaxonomyMetaInfo" :key="index">
                    <span>{{
                      "- upload Time in Unix time: " + dataMetaInfo.uploadTime + ", dataId: " + dataMetaInfo.dataId
                    }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div id="sfdrContainer" class="grid mt-6">
              <div id="sfdrLabel" class="col-3 text-left topicLabel">
                <h3>SFDR</h3>
                <p>Overiew of all existing and missing SFDR datasets for this company.</p>
              </div>
              <div class="col-6 text-left uploadForm">
                <div id="sfdrDataSetsContainer" class="mt-6">
                  Sfdr Datasets:

                  <div v-if="waitingForData" class="inline-loading meta-data-height text-center">
                    <p class="font-medium text-xl">Loading...</p>
                    <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
                  </div>

                  <div v-for="(dataMetaInfo, index) in listOfSfdrMetaInfo" :key="index">
                    <span>{{
                      "- upload Time in Unix time: " + dataMetaInfo.uploadTime + ", dataId: " + dataMetaInfo.dataId
                    }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div id="lksgContainer" class="grid mt-6">
              <div id="lksgLabel" class="col-3 text-left topicLabel">
                <h3>LkSG</h3>
                <p>Overiew of all existing and missing LkSG datasets for this company.</p>
              </div>
              <div class="col-6 text-left uploadForm">
                <div id="lksgDataSetsContainer" class="mt-6">
                  LkSG Datasets:

                  <div v-if="waitingForData" class="inline-loading meta-data-height text-center">
                    <p class="font-medium text-xl">Loading...</p>
                    <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
                  </div>

                  <div v-for="(dataMetaInfo, index) in listOfLksgMetaInfo" :key="index">
                    <span>{{
                      "- upload Time in Unix time: " + dataMetaInfo.uploadTime + ", dataId: " + dataMetaInfo.dataId
                    }}</span>
                  </div>
                </div>
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
import { DataMetaInformation } from "@clients/backend";

export default defineComponent({
  name: "Choose Framework",
  components: {
    CompanyInformation,
    AuthenticationWrapper,
    TheHeader,
    BackButton,
    TheContent,
    Card,
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
      listOfEuTaxonomyMetaInfo: [] as Array<DataMetaInformation>,
      listOfSfdrMetaInfo: [] as Array<DataMetaInformation>,
      listOfLksgMetaInfo: [] as Array<DataMetaInformation>,
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
        this.listOfEuTaxonomyMetaInfo = listOfAllDataMetaInfo.filter((dataMetaInfo: DataMetaInformation) =>
          dataMetaInfo.dataType.startsWith("eutaxonomy-")
        );
        this.listOfSfdrMetaInfo = listOfAllDataMetaInfo.filter(
          (dataMetaInfo: DataMetaInformation) => dataMetaInfo.dataType === "sfdr"
        );
        this.listOfLksgMetaInfo = listOfAllDataMetaInfo.filter(
          (dataMetaInfo: DataMetaInformation) => dataMetaInfo.dataType === "lksg"
        );
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>
