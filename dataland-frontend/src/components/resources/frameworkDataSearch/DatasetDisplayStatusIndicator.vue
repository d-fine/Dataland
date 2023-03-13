<template>
  <div
    v-if="displayWarning"
    data-test="datasetDisplayStatusContainer"
    class="flex w-full info-bar"
    style="min-height: 2rem"
  >
    <span class="flex-1">{{ warningMessage }}</span>
    <router-link v-if="displayedDataset.qaStatus !== QAStatus.Pending" :to="link" class="no-underline" data-test="datasetDisplayStatusLink">
      <PrimeButton :label="buttonLabel" icon="pi pi-stopwatch" />
    </router-link>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import { DataMetaInformation, QAStatus } from "@clients/backend";
import PrimeButton from "primevue/button";
import { assertDefined } from "@/utils/TypeScriptUtils";
export default defineComponent({
  name: "DatasetDisplayStatusIndicator",
  components: { PrimeButton },
  data() {
    return {
      QAStatus,
    };
  },
  props: {
    displayedDataset: {
      type: Object as PropType<DataMetaInformation | null>,
      required: true,
    },
    receivedMapOfReportingPeriodsToActiveDataMetaInfo: {
      type: Object,
      required: true,
    },
    isMultiview: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    displayWarning(): boolean {
      return (
        this.displayedDataset?.currentlyActive === false ||
        this.displayedDataset?.qaStatus === QAStatus.Pending ||
        this.areMoreDatasetsViewableSimultaneously
      );
    },
    warningMessage(): string {
      if (this.displayedDataset?.qaStatus === QAStatus.Pending) return "This dataset is currently pending review";
      else if (this.displayedDataset?.currentlyActive === false) return "This dataset is outdated";
      else if (this.areMoreDatasetsViewableSimultaneously) return "You are only viewing a single available dataset";
      else return "ERROR";
    },
    buttonLabel(): string {
      if (this.displayedDataset?.qaStatus === QAStatus.Pending || this.displayedDataset?.currentlyActive === false) {
        return "View Active";
      } else if (this.areMoreDatasetsViewableSimultaneously) {
        return "View All";
      } else {
        return "ERROR";
      }
    },
    link(): string | undefined {
      if (this.displayedDataset?.qaStatus === QAStatus.Pending || this.displayedDataset?.currentlyActive === false) {
        return (
          `/companies/${this.displayedDataset.companyId}` +
          `/frameworks/${this.displayedDataset.dataType}/reportingPeriods/${this.displayedDataset.reportingPeriod}`
        );
      } else if (this.areMoreDatasetsViewableSimultaneously) {
        return (
          `/companies/${assertDefined(this.displayedDataset?.companyId)}/` +
          `frameworks/${assertDefined(this.displayedDataset?.dataType)}`
        );
      } else {
        return undefined;
      }
    },
    areMoreDatasetsViewableSimultaneously(): boolean {
      return (
        this.isMultiview &&
        this.displayedDataset != null &&
        (this.receivedMapOfReportingPeriodsToActiveDataMetaInfo as Map<string, DataMetaInformation>).size > 1
      );
    },
  },
});
</script>
