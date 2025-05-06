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
    <TheFooter :is-light-version="true" :sections="footerContent" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import TheContent from '@/components/generics/TheContent.vue';
import { defineComponent, inject } from 'vue';
import TheFooter from '@/components/generics/TheNewFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';
import DatasetOverviewTable from '@/components/resources/datasetOverview/DatasetOverviewTable.vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import { type DatasetTableInfo, getMyDatasetTableInfos } from '@/components/resources/datasetOverview/DatasetTableInfo';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import NewDatasetButton from '@/components/general/NewDatasetButton.vue';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';

export default defineComponent({
  name: 'DatasetOverview',
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
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    const footerContent = footerPage?.sections;
    return {
      datasetTableInfos: [] as DatasetTableInfo[],
      waitingForData: true,
      hasUserUploaderRights: undefined as boolean | undefined,
      footerContent,
    };
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
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
