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
          <p class="cursor-pointer text-primary font-semibold" @click="redirectToViewPage(dataMetaInfo)">{{ title }}</p>
          <p>{{ convertUnixTimeInMsToDateString(dataMetaInfo.uploadTime * 1000) }}</p>
        </div>
        <p class="mt-5">{{ createButtonTitle }}</p>
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
    },
    isFrontendUploadFormExisting: {
      type: Boolean,
      default: true,
    },
    frameworkUrlPath: {
      type: String,
    },
    isWaitingForData: {
      type: Boolean,
      default: true,
    },
    companyId: {
      type: String,
    },
    listOfFrameworkData: {
      type: Array,
    },
  },

  data() {
    return {
      convertUnixTimeInMsToDateString: convertUnixTimeInMsToDateString,
    };
  },

  computed: {
    createButtonTitle() {
      if (this.listOfFrameworkData) {
        if (this.listOfFrameworkData.length === 0) {
          return "Be the first to create this dataset";
        } else {
          return `Create another dataset for ${this.title}`;
        }
      }
    },
  },
  methods: {
    redirectToViewPage(dataMetaInfoOfClickedDataSet: DataMetaInformation) {
      const frameworksWhichCanDisplayMultipleDatasetsAtOnceInFrontend = [DataTypeEnum.Lksg];
      let dataIdQueryParamToSet: string;
      if (frameworksWhichCanDisplayMultipleDatasetsAtOnceInFrontend.includes(dataMetaInfoOfClickedDataSet.dataType)) {
        dataIdQueryParamToSet = "";
      } else {
        dataIdQueryParamToSet = `?dataId=${dataMetaInfoOfClickedDataSet.dataId}`;
      }
      this.$router.push(`/companies/${this.companyId}/frameworks/${this.frameworkUrlPath}${dataIdQueryParamToSet}`);
    },

    redirectToUploadForm() {
      this.$router.push(`/companies/${this.companyId}/frameworks/${this.frameworkUrlPath}/upload`);
    },
  },
});
</script>
