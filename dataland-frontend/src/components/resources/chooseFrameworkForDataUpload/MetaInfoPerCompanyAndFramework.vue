<template>
  <div>
    <div>
      <h4>{{ title + " Datasets:" }}</h4>
      <div v-if="isWaitingForData" class="inline-loading text-center">
        <p class="font-medium text-xl">Loading...</p>
        <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>

      <div v-else>
        <div v-for="(dataMetaInfo, index) in listOfFrameworkData" :key="index">
          <div>
            <router-link
              v-if="this.isFrontendViewPageExisting"
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
            <DatasetStatusBadge :dataset-status="getDatasetStatus(dataMetaInfo)" />
          </div>
        </div>
        <p class="mt-5">{{ dynamicButtonTitle }}</p>
        <PrimeButton
          v-if="!isPrivateFramework || isDataOwner"
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
import { defineComponent, inject } from "vue";
import { convertUnixTimeInMsToDateString } from "@/utils/DataFormatUtils";
import PrimeButton from "primevue/button";
import { type DataMetaInformation, type DataTypeEnum } from "@clients/backend";
import {
  ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM,
  ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE,
  PRIVATE_FRAMEWORKS,
} from "@/utils/Constants";
import { humanizeStringOrNumber } from "@/utils/StringFormatter";
import { getDatasetStatus } from "@/components/resources/datasetOverview/DatasetTableInfo";
import DatasetStatusBadge from "@/components/general/DatasetStatusBadge.vue";
import { isUserDataOwnerForCompany } from "@/utils/DataOwnerUtils";
import type Keycloak from "keycloak-js";

export default defineComponent({
  name: "MetaInfoPerCompanyAndFramework",
  components: { PrimeButton, DatasetStatusBadge },

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
      type: Array,
      required: true,
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      title: humanizeStringOrNumber(this.dataType),
      isFrontendViewPageExisting: null as null | boolean,
      isFrontendUploadFormExisting: null as null | boolean,
      isPrivateFramework: null as null | boolean,
      isDataOwner: false as boolean,
      convertUnixTimeInMsToDateString: convertUnixTimeInMsToDateString,
      getDatasetStatus,
    };
  },

  mounted() {
    this.isFrontendViewPageExisting = ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE.includes(this.dataType as DataTypeEnum);
    this.isFrontendUploadFormExisting = ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM.includes(this.dataType as DataTypeEnum);
    this.isPrivateFramework = PRIVATE_FRAMEWORKS.includes(this.dataType as DataTypeEnum);
  },
  created() {
    this.setDataOwnerRights();
  },
  computed: {
    dynamicButtonTitle(): string {
      if (this.listOfFrameworkData.length === 0) {
        if (this.isPrivateFramework && !this.isDataOwner) {
          return "Become data owner to create a dataset";
        } else {
          return "Be the first to create this dataset";
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
     * The methods determines the appropriate rights for dataowners and non dataowners
     * @returns the boolean if user has dataowner rights or not
     */
    async setDataOwnerRights(): Promise<void> {
      return isUserDataOwnerForCompany(this.companyId, this.getKeycloakPromise).then((isDataOwner) => {
        this.isDataOwner = isDataOwner;
      });
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
      await this.$router.push(`/companies/${this.companyId}/frameworks/${this.dataType}/upload`);
    },
  },
});
</script>
