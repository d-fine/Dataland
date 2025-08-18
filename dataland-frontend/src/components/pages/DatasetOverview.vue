<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="relative">
      <DatasetsTabMenu :initial-tab-index="1">
        <div>
          <PrimeButton
            v-if="hasUserUploaderRights"
            icon="pi pi-plus"
            label="NEW DATASET"
            data-test="newDatasetButton"
            @click="linkToNewDataSet()"
            :pt="{ root: { style: 'display: flex; margin:var(--spacing-sm)' } }"
          />
        </div>
        <DatasetOverviewTable
          data-test="datasetOverviewTable"
          :dataset-table-infos="datasetTableInfos"
          :class="datasetTableInfos.length > 0 ? '' : 'hidden'"
        />
        <div v-if="waitingForData" class="inline-loading text-center">
          <p class="font-medium text-xl">Loading datasets...</p>
          <DatalandProgressSpinner />
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
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import DatasetOverviewTable from '@/components/resources/datasetOverview/DatasetOverviewTable.vue';
import { type DatasetTableInfo, getMyDatasetTableInfos } from '@/components/resources/datasetOverview/DatasetTableInfo';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import { defineComponent, inject } from 'vue';
import router from '@/router';

export default defineComponent({
  name: 'DatasetOverview',
  components: {
    DatalandProgressSpinner,
    PrimeButton,
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
      hasUserUploaderRights: undefined as boolean | undefined,
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
     * Links the current instance or context to a new dataset, establishing a connection or association with it.
     */
    linkToNewDataSet() {
      void router.push('/companies/choose');
    },
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
