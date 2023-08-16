<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section relative">
      <DatasetsTabMenu :initial-tab-index="1">
        <div class="col-12 flex flex-row justify-content-between align-items-end">
          <NewDatasetButton v-if="hasUserUploaderRights" />
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
      </DatasetsTabMenu>
    </TheContent>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import { defineComponent, inject } from "vue";
import TheFooter from "@/components/general/TheFooter.vue";
import DatasetOverviewTable from "@/components/resources/datasetOverview/DatasetOverviewTable.vue";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Keycloak from "keycloak-js";
import { DatasetTableInfo, getMyDatasetTableInfos } from "@/components/resources/datasetOverview/DatasetTableInfo";
import { checkIfUserHasRole, KEYCLOAK_ROLE_UPLOADER } from "@/utils/KeycloakUtils";
import DatasetsTabMenu from "@/components/general/DatasetsTabMenu.vue";
import NewDatasetButton from "@/components/general/NewDatasetButton.vue";

export default defineComponent({
  name: "DatasetOverview",
  components: {
    NewDatasetButton,
    DatasetsTabMenu,
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    TheFooter,
    DatasetOverviewTable,
  },
  data() {
    return {
      datasetTableInfos: [] as DatasetTableInfo[],
      waitingForData: true,
      hasUserUploaderRights: undefined,
    };
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, this.getKeycloakPromise)
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
      this.datasetTableInfos = await getMyDatasetTableInfos(assertDefined(this.getKeycloakPromise));
      this.waitingForData = false;
    },
  },
});
</script>
