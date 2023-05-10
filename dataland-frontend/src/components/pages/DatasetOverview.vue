<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TabView class="col-12" v-model:activeIndex="activeTabIndex" @tab-change="handleTabChange">
      <TabPanel header="AVAILABLE DATASETS"></TabPanel>
      <TabPanel header="MY DATASETS">
        <TheContent class="p-3 min-h-screen paper-section relative">
          <div class="col-12 flex flex-row justify-content-between align-items-end">
            <router-link
              v-if="hasUserUploaderRights"
              to="/companies/choose"
              class="no-underline"
              data-test="newDatasetButton"
            >
              <PrimeButton
                class="uppercase p-button p-button-sm d-letters mr-3"
                label="New Dataset"
                icon="pi pi-plus"
              />
            </router-link>
          </div>
          <DatasetOverviewTable
            data-test="datasetOverviewTable"
            :dataset-table-infos="datasetTableInfos"
            :class="datasetTableInfos.length > 0 ? '' : 'hidden'"
          />
          <div v-if="waitingForData" class="inline-loading text-center">
            <p class="font-medium text-xl">Loading datasets...</p>
            <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
          </div>
          <div v-else-if="datasetTableInfos.length === 0">
            <h1 class="mb-0" data-test="noDatasetUploadedText">No datasets uploaded</h1>
          </div>
        </TheContent>
      </TabPanel>
    </TabView>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import { defineComponent, inject } from "vue";
import TheFooter from "@/components/general/TheFooter.vue";
import PrimeButton from "primevue/button";
import DatasetOverviewTable from "@/components/resources/datasetOverview/DatasetOverviewTable.vue";
import Keycloak from "keycloak-js";
import { DatasetTableInfo, getMyDatasetTableInfos } from "@/components/resources/datasetOverview/DatasetTableInfo";
import TabView from "primevue/tabview";
import TabPanel from "primevue/tabpanel";
import { checkIfUserHasUploaderRights } from "@/utils/KeycloakUtils";

export default defineComponent({
  name: "DatasetOverview",
  components: {
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    TheFooter,
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
  created() {
    checkIfUserHasUploaderRights(this.getKeycloakPromise)
      .then((hasUserUploaderRights) => {
        this.hasUserUploaderRights = hasUserUploaderRights;
      })
      .catch((error) => console.log(error));
    this.requestDataMetaDataForCurrentUser().catch((error) => console.log(error));
  },
  methods: {
    /**
     * Finds the datasets the logged in user is responsible for and creates corresponding table entries
     */
    requestDataMetaDataForCurrentUser: async function (): Promise<void> {
      this.datasetTableInfos = await getMyDatasetTableInfos(this.getKeycloakPromise!);
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
