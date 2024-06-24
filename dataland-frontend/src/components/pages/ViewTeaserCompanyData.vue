<template>
  <div v-if="isMetaInfoFetched" class="mt-8">
    <h3>COMPANY DATA SAMPLE</h3>
    <h4>Try Dataland with other people to access all the data.</h4>
    <div class="col-4 col-offset-4">
      <JoinDatalandButton />
    </div>
    <ViewFrameworkData
      :view-in-preview-mode="true"
      :company-id="companyId"
      :data-type="dataType"
      :data-id="dataId"
      :reporting-period="reportingPeriod"
    />
  </div>
  <div v-if="!isAtLeastOneTeaserCompanyExisting || !isAtLeastOneDatasetExistingForTeaserCompany">
    <BackButton />
    <h3>No sample data published</h3>
    <h4>
      Currently there is no dataset published for preview by the Dataland administrators. Please come back later to see
      a preview dataset.
    </h4>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject } from 'vue';
import type Keycloak from 'keycloak-js';
import ViewFrameworkData from '@/components/pages/ViewFrameworkData.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import JoinDatalandButton from '@/components/general/JoinDatalandButton.vue';
import BackButton from '@/components/general/BackButton.vue';

export default defineComponent({
  name: 'ViewTeaserCompanyData',
  components: {
    ViewFrameworkData,
    JoinDatalandButton,
    BackButton,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  data: () => ({
    companyId: '',
    dataId: '',
    dataType: '',
    reportingPeriod: '',
    isMetaInfoFetched: false,
    isAtLeastOneTeaserCompanyExisting: true,
    isAtLeastOneDatasetExistingForTeaserCompany: true,
  }),
  created() {
    void this.queryCompany();
  },
  methods: {
    /**
     * Uses the Dataland API to retrieve all teaser companies, picks the first company and gets the data meta info of
     * the first dataset for that company to display it on the sample page.
     */
    async queryCompany() {
      try {
        const companyDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).backendClients
          .companyDataController;
        const companyResponse = await companyDataControllerApi.getTeaserCompanies();
        if (companyResponse.data.length > 0) {
          this.companyId = companyResponse.data[0];

          const backendClients = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).backendClients;
          const metaDataControllerApi = backendClients.metaDataController;
          const listOfMetaDataInfo = (await metaDataControllerApi.getListOfDataMetaInfo(this.companyId)).data;
          if (listOfMetaDataInfo.length > 0) {
            const dataMetaInfoForDisplay = listOfMetaDataInfo[0];
            this.dataId = dataMetaInfoForDisplay.dataId;
            this.dataType = dataMetaInfoForDisplay.dataType;
            this.reportingPeriod = dataMetaInfoForDisplay.reportingPeriod;
            this.isMetaInfoFetched = true;
          } else {
            this.isAtLeastOneDatasetExistingForTeaserCompany = false;
          }
        } else {
          this.isAtLeastOneTeaserCompanyExisting = false;
        }
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>
