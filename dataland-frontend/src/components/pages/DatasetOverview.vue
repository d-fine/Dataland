<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TabView class="col-12" v-model:activeIndex="activeTabIndex" @tab-change="handleTabChange">
      <TabPanel header="AVAILABLE DATASETS"> </TabPanel>
      <TabPanel header="MY DATASETS">
        <TheContent class="p-3 min-h-screen paper-section relative">
          <div class="col-12 flex flex-row justify-content-between align-items-end">
            <PrimeButton
              v-if="hasUserUploaderRights"
              class="uppercase p-button p-button-sm d-letters mr-3"
              label="New Dataset"
              icon="pi pi-plus"
              @click="void this.$router.push('/companies/choose')"
            />
          </div>
          <DatasetOverviewTable
            :dataset-table-infos="datasetTableInfos"
            :class="datasetTableInfos.length > 0 ? '' : 'hidden'"
          />
          <div v-if="waitingForData" class="inline-loading text-center">
            <p class="font-medium text-xl">Loading datasets...</p>
            <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
          </div>
        </TheContent>
      </TabPanel>
    </TabView>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import { defineComponent, inject } from "vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import PrimeButton from "primevue/button";
import DatasetOverviewTable from "@/components/resources/datasetOverview/DatasetOverviewTable.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Keycloak from "keycloak-js";
import { DataMetaInformation, StoredCompany } from "@clients/backend";
import { DatasetTableInfo } from "@/components/resources/datasetOverview/DatasetTableInfo";
import { ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS } from "@/utils/Constants";
import TabView from "primevue/tabview";
import TabPanel from "primevue/tabpanel";
import { checkIfUserHasUploaderRights } from "@/utils/KeycloakUtils";

export default defineComponent({
  name: "DatasetOverview",
  components: {
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    DatalandFooter,
    PrimeButton,
    DatasetOverviewTable,
    TabView,
    TabPanel,
  },
  data() {
    return {
      datasetTableInfos: [] as DatasetTableInfo[],
      waitingForData: true,
      activeTabIndex: 1,
      hasUserUploaderRights: null as boolean | null,
    };
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  async created() {
    await this.checkIfUserHasUploaderRightsAndSetFlag(this.getKeycloakPromise);
    await this.requestDataMetaDataForCurrentUser();
  },
  methods: {
    async checkIfUserHasUploaderRightsAndSetFlag() {
      this.hasUserUploaderRights = await checkIfUserHasUploaderRights(this.getKeycloakPromise);
    },

    /**
     * Finds the datasets the logged in user is responsible for and creates corresponding table entries
     */
    requestDataMetaDataForCurrentUser: async function (): Promise<void> {
      let userId: string | undefined;
      const companyDataControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getCompanyDataControllerApi();
      const companiesMetaInfos = (
        await companyDataControllerApi.getCompanies(
          undefined,
          new Set(ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS),
          undefined,
          undefined,
          undefined,
          true
        )
      ).data;
      const resolvedKeycloakPromise = await assertDefined(this.getKeycloakPromise)();
      if (resolvedKeycloakPromise.idTokenParsed) {
        userId = resolvedKeycloakPromise.idTokenParsed.sub;
      }
      this.datasetTableInfos = companiesMetaInfos.flatMap((company: StoredCompany) =>
        company.dataRegisteredByDataland
          .filter(
            (dataMetaInfo: DataMetaInformation) =>
              dataMetaInfo.uploaderUserId == userId &&
              ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS.includes(dataMetaInfo.dataType)
          )
          .map(
            (dataMetaInfo: DataMetaInformation) =>
              new DatasetTableInfo(
                company.companyInformation.companyName,
                dataMetaInfo.dataType,
                dataMetaInfo.uploadTime * 1000,
                company.companyId,
                dataMetaInfo.dataId
              )
          )
      );
      this.waitingForData = false;
    },
    /**
     * Routes to companies page when AVAILABLE DATASET tab is clicked
     */
    handleTabChange(): void {
      if (this.activeTabIndex == 0) {
        void this.$router.push("/companies");
      }
    },
  },
});
</script>
