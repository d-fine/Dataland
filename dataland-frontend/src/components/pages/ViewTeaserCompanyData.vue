<template>
  <div class="mt-8">
    <h3>COMPANY DATA SAMPLE</h3>
    <h4>Try Dataland with other people to access all the data.</h4>
    <div class="col-4 col-offset-4">
      <JoinDatalandButton />
    </div>
  </div>
  <ViewFrameworkData
    v-if="isMetaInfoFetched"
    :view-in-preview-mode="true"
    :company-id="companyId"
    :data-type="dataType"
    :data-id="dataId"
    :reporting-period="reportingPeriod"
  />
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import type Keycloak from "keycloak-js";
import ViewFrameworkData from "@/components/pages/ViewFrameworkData.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import JoinDatalandButton from "@/components/general/JoinDatalandButton.vue";

export default defineComponent({
  name: "ViewTeaserCompanyData",
  components: {
    ViewFrameworkData,
    JoinDatalandButton,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data: () => ({
    companyId: "",
    dataId: "",
    dataType: "",
    reportingPeriod: "",
    isMetaInfoFetched: false,
  }),
  created() {
    void this.queryCompany();
  },
  methods: {
    /**
     * Uses the dataland API to retrieve the companyId of the first teaser company and TODO explain further
     */
    async queryCompany() {
      try {
        const companyDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).getCompanyDataControllerApi();
        const companyResponse = await companyDataControllerApi.getTeaserCompanies();
        this.companyId = companyResponse.data[0];

        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).getMetaDataControllerApi();
        const listOfMetaDataInfo = (await metaDataControllerApi.getListOfDataMetaInfo(this.companyId)).data;

        if (listOfMetaDataInfo.length > 0) {
          const dataMetaInfoForDisplay = listOfMetaDataInfo[0];
          this.dataId = dataMetaInfoForDisplay.dataId;
          this.dataType = dataMetaInfoForDisplay.dataType;
          this.reportingPeriod = dataMetaInfoForDisplay.reportingPeriod;
        }
        this.isMetaInfoFetched = true;
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>
