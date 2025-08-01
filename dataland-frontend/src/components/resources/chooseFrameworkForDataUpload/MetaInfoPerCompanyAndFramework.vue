<template>
  <div>
    <div>
      <h4>{{ title + ' Datasets:' }}</h4>
      <div v-if="isWaitingForData" class="inline-loading text-center">
        <p class="font-medium text-xl">Loading...</p>
        <DatalandProgressSpinner />
      </div>

      <div v-else>
        <div v-for="(dataMetaInfo, index) in listOfFrameworkData" :key="index">
          <div>
            <router-link
              v-if="isFrontendViewPageExisting"
              :to="calculateDatasetLink(dataMetaInfo)"
              class="text-primary font-semibold underline"
            >
              {{ getDynamicDatasetTitle(dataMetaInfo) }}
            </router-link>
            <span v-else class="font-semibold underline">
              {{ getDynamicDatasetTitle(dataMetaInfo) }}
            </span>
          </div>
          <div>
            <span class="mr-3">{{ convertUnixTimeInMsToDateString(dataMetaInfo.uploadTime) }}</span>
            <DatalandTag :severity="getDatasetStatus(dataMetaInfo)" :value="getDatasetStatus(dataMetaInfo)" />
          </div>
        </div>
        <p class="mt-5">{{ dynamicButtonTitle }}</p>
        <PrimeButton
          v-if="!isPrivateFramework || isCompanyOwner"
          class="uppercase p-button p-button-sm d-letters mt-3"
          :disabled="!isFrontendUploadFormExisting"
          label="Create Dataset"
          data-test="createDatasetButton"
          icon="pi pi-plus"
          @click="redirectToUploadForm"
        />
        <div v-if="!isFrontendUploadFormExisting">
          <p>
            (Uploading data for this framework is currently not enabled on the Dataland frontend. You can use the
            Dataland API to do so.)
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import DatalandTag from '@/components/general/DatalandTag.vue';
import { getDatasetStatus } from '@/components/resources/datasetOverview/DatasetTableInfo';
import router from '@/router';
import { hasUserCompanyRoleForCompany } from '@/utils/CompanyRolesUtils';
import { FRAMEWORKS_WITH_UPLOAD_FORM, FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { isFrameworkPrivate } from '@/utils/Frameworks';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { type DataMetaInformation, type DataTypeEnum } from '@clients/backend';
import { CompanyRole } from '@clients/communitymanager';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import { defineComponent, inject, type PropType } from 'vue';

export default defineComponent({
  name: 'MetaInfoPerCompanyAndFramework',
  components: { DatalandProgressSpinner, PrimeButton, DatalandTag },

  props: {
    dataType: {
      type: String,
      required: true,
    },
    isWaitingForData: {
      type: Boolean,
      default: true,
    },
    companyId: {
      type: String,
      required: true,
    },
    listOfFrameworkData: {
      type: Array as PropType<DataMetaInformation[]>,
      required: true,
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  data() {
    return {
      title: humanizeStringOrNumber(this.dataType),
      isFrontendViewPageExisting: null as null | boolean,
      isFrontendUploadFormExisting: null as null | boolean,
      isPrivateFramework: null as null | boolean,
      isCompanyOwner: false as boolean,
      convertUnixTimeInMsToDateString: convertUnixTimeInMsToDateString,
      getDatasetStatus,
    };
  },

  mounted() {
    this.isFrontendViewPageExisting = FRAMEWORKS_WITH_VIEW_PAGE.includes(this.dataType as DataTypeEnum);
    this.isFrontendUploadFormExisting = FRAMEWORKS_WITH_UPLOAD_FORM.includes(this.dataType as DataTypeEnum);
    this.isPrivateFramework = isFrameworkPrivate(this.dataType as DataTypeEnum);
  },
  created() {
    void this.setCompanyOwnerRights();
  },
  computed: {
    dynamicButtonTitle(): string {
      if (this.listOfFrameworkData.length === 0) {
        if (this.isPrivateFramework && !this.isCompanyOwner) {
          return 'Become company owner to create a dataset';
        } else {
          return 'Be the first to create this dataset';
        }
      } else {
        return `Create another dataset for ${this.title}`;
      }
    },
  },
  methods: {
    /**
     * Calculates the link to the view page for the specified dataset
     * @param dataMetaInfo the dataset to generate the link for
     * @returns the link to the view page for the specified dataset
     */
    calculateDatasetLink(dataMetaInfo: DataMetaInformation): string {
      return `/companies/${this.companyId}/frameworks/${this.dataType}/${dataMetaInfo.dataId}`;
    },
    /**
     * Determines the appropriate rights for company owners and non company owners
     * @returns the boolean if user has company owner rights or not
     */
    async setCompanyOwnerRights(): Promise<void> {
      return hasUserCompanyRoleForCompany(CompanyRole.CompanyOwner, this.companyId, this.getKeycloakPromise).then(
        (isCompanyOwner) => {
          this.isCompanyOwner = isCompanyOwner;
        }
      );
    },

    /**
     * Method to construct a title for a data meta information object depending on whether it is currently active and
     * whether a corresponding frontend view page exists
     * @param dataMetaInfo The data meta information object for which the title is constructed
     * @returns the constrcted dataset title
     */
    getDynamicDatasetTitle(dataMetaInfo: DataMetaInformation): string {
      let resultingTitle = `${this.title} dataset for reporting period: ${dataMetaInfo.reportingPeriod}`;
      if (dataMetaInfo.currentlyActive) {
        resultingTitle = `${resultingTitle} - latest version`;
      }
      if (!this.isFrontendViewPageExisting) {
        resultingTitle = `${resultingTitle} (only viewable via API)`;
      }
      return resultingTitle;
    },

    /**
     * Executes a router push to the upload page of a given company and framework
     */
    async redirectToUploadForm() {
      await router.push(`/companies/${this.companyId}/frameworks/${this.dataType}/upload`);
    },
  },
});
</script>
<style scoped>
.d-letters {
  letter-spacing: 0.05em;
}

.text-primary {
  color: var(--main-color);
}
</style>
