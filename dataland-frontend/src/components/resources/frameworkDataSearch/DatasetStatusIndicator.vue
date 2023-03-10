<template>
  <div v-if="displayWarning" data-test="datasetDisplayStatusContainer" class="flex w-full info-bar" style="min-height: 2rem">
    <span class="flex-1">{{ warningMessage }}</span>
    <router-link
      v-if="linkToActivePage"
      :to="linkToActivePage"
      class="no-underline"
      data-test="datasetStatusLinkToActive"
    >
      <PrimeButton :label="buttonLabel" icon="pi pi-stopwatch" />
    </router-link>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import { DataMetaInformation, QAStatus } from "@clients/backend";
import PrimeButton from "primevue/button";
export default defineComponent({
  name: "DatasetStatusIndicator",
  components: { PrimeButton },
  props: {
    displayedDataset: {
      type: Object as PropType<DataMetaInformation>,
    },
    linkToActivePage: {
      type: String,
    },
  },
  computed: {
    displayWarning(): boolean {
      return this.displayedDataset?.currentlyActive === false || this.displayedDataset?.qaStatus === QAStatus.Pending;
    },
    warningMessage(): string {
      if (this.displayedDataset?.qaStatus === QAStatus.Pending) return "This dataset is currently pending review";
      else if (this.displayedDataset?.currentlyActive === false) return "This dataset is outdated";
      else return "ERROR";
    },
    buttonLabel(): string {
      return "View Active";
    }
  },
});
</script>
