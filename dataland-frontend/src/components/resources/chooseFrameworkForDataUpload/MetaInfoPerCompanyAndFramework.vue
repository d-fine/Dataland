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
          <p
            :class="[isFrontendViewPageExisting ? ['text-primary', 'cursor-pointer'] : '']"
            class="font-semibold"
            @click="redirectToViewPageIfEnabledInFrontend(dataMetaInfo)"
          >
            {{ getDynamicDatasetTitle(dataMetaInfo) }}
          </p>
          <p>{{ convertUnixTimeInMsToDateString(dataMetaInfo.uploadTime * 1000) }}</p>
        </div>
        <p class="mt-5">{{ dynamicButtonTitle }}</p>
        <PrimeButton
          class="uppercase p-button p-button-sm d-letters mt-3"
          :disabled="!isFrontendUploadFormExisting"
          label="Create Dataset"
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
import { defineComponent } from "vue";
import { convertUnixTimeInMsToDateString } from "@/utils/DateFormatUtils";
import PrimeButton from "primevue/button";
import { DataMetaInformation, DataTypeEnum } from "@clients/backend";

export default defineComponent({
  name: "MetaInfoPerComanyAndFramework",
  components: { PrimeButton },

  props: {
    title: {
      type: String,
      required: true,
    },
    isFrontendViewPageExisting: {
      type: Boolean,
      default: true,
    },
    isFrontendUploadFormExisting: {
      type: Boolean,
      default: true,
    },
    frameworkUrlPath: {
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

  data() {
    return {
      convertUnixTimeInMsToDateString: convertUnixTimeInMsToDateString,
    };
  },

  computed: {
    dynamicButtonTitle(): string {
      if (this.listOfFrameworkData.length === 0) {
        return "Be the first to create this dataset";
      } else {
        return `Create another dataset for ${this.title}`;
      }
    },
  },
  methods: {
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
     * Executes a router push to the page of the given data set if the framework it belongs to is enabled in the frontend
     *
     * @param dataMetaInfoOfClickedDataSet the meta information of the data set in question
     */
    async redirectToViewPageIfEnabledInFrontend(dataMetaInfoOfClickedDataSet: DataMetaInformation) {
      if (this.isFrontendViewPageExisting) {
        let urlExtension: string;
        if (dataMetaInfoOfClickedDataSet.dataType === DataTypeEnum.Lksg) {
          urlExtension = "";
        } else {
          urlExtension = `/${dataMetaInfoOfClickedDataSet.dataId}`;
        }
        await this.$router.push(`/companies/${this.companyId}/frameworks/${this.frameworkUrlPath}${urlExtension}`);
      }
    },
    /**
     * Executes a router push to the upload page of a given company and framework
     */
    async redirectToUploadForm() {
      await this.$router.push(`/companies/${this.companyId}/frameworks/${this.frameworkUrlPath}/upload`);
    },
  },
});
</script>
