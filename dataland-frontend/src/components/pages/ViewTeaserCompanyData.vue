<template>
  <TheHeader :showUserProfileDropdown="false">
    <span class="p-button p-button-rounded text-primary bg-white font-semibold border-0"> COMPANY DATA SAMPLE </span>
  </TheHeader>
  <TheContent>
    <MarginWrapper>
      <div class="grid">
        <div class="col-12 bg-green-500 p-0 mt-3">
          <p class="text-white font-semibold flex justify-content-center">
            <i class="material-icons pr-2 flex align-items-center" aria-hidden="true">check_circle</i>
            <span class="pr-2 flex align-items-center">Try Dataland with other people to access all the data.</span>
          </p>
          <JoinDatalandButton />
          <ViewFrameworkData
            v-if="isMetaInfoFetched"
            :view-in-preview-mode="true"
            :company-id="companyId"
            :data-type="dataType"
            :data-id="dataId"
            :reporting-period="reportingPeriod"
          />
        </div>
      </div>
    </MarginWrapper>
    <MarginWrapper class="text-left">
      <BackButton class="mt-3" />
    </MarginWrapper>
  </TheContent>
  <TheFooter />
</template>

<script lang="ts">
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import BackButton from "@/components/general/BackButton.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import { defineComponent, inject } from "vue";
import type Keycloak from "keycloak-js";
import TheFooter from "@/components/general/TheFooter.vue";
import ViewFrameworkData from "@/components/pages/ViewFrameworkData.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import JoinDatalandButton from "@/components/general/JoinDatalandButton.vue";

export default defineComponent({
  name: "ViewTeaserCompanyData",
  components: {
    TheContent,
    TheHeader,
    BackButton,
    MarginWrapper,
    TheFooter,
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
