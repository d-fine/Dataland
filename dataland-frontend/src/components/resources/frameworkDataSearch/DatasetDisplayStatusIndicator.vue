<template>
  <div
    v-if="displayWarning"
    data-test="datasetDisplayStatusContainer"
    class="flex w-full info-bar"
    style="min-height: 2rem"
  >
    <span class="flex-1">{{ warningMessage }}</span>
    <router-link
      v-if="existsAcceptedVersion && link"
      :to="link"
      class="no-underline"
      data-test="datasetDisplayStatusLink"
    >
      <PrimeButton :label="buttonLabel" icon="pi pi-stopwatch" />
    </router-link>
  </div>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue';
import { type DataMetaInformation, QaStatus } from '@clients/backend';
import PrimeButton from 'primevue/button';
import { assertDefined } from '@/utils/TypeScriptUtils';
export default defineComponent({
  name: 'DatasetDisplayStatusIndicator',
  components: { PrimeButton },
  props: {
    displayedDataset: {
      type: Object as PropType<DataMetaInformation | null>,
    },
    receivedMapOfReportingPeriodsToActiveDataMetaInfo: {
      type: Object as PropType<Map<string, DataMetaInformation>>,
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
        (this.displayedDataset?.qaStatus && this.displayedDataset.qaStatus !== QaStatus.Accepted) ||
        this.areMoreDatasetsViewableSimultaneously
      );
    },
    warningMessage(): string {
      if (this.displayedDataset?.qaStatus === QaStatus.Pending) return 'This dataset is currently pending review';
      else if (this.displayedDataset?.qaStatus === QaStatus.Rejected) return 'This dataset has been rejected';
      else if (this.displayedDataset?.currentlyActive === false) return 'This dataset is superseded';
      else if (this.areMoreDatasetsViewableSimultaneously) return 'You are only viewing a single available dataset';
      else return 'ERROR';
    },
    buttonLabel(): string {
      if (this.displayedDataset?.qaStatus === QaStatus.Pending || this.displayedDataset?.currentlyActive === false) {
        return 'View Active';
      } else if (this.areMoreDatasetsViewableSimultaneously) {
        return 'View All';
      } else {
        return 'ERROR';
      }
    },
    link(): string | undefined {
      if (
        (this.displayedDataset?.qaStatus && this.displayedDataset.qaStatus !== QaStatus.Accepted) ||
        this.displayedDataset?.currentlyActive === false
      ) {
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
    existsAcceptedVersion(): boolean {
      return this.receivedMapOfReportingPeriodsToActiveDataMetaInfo.has(this.displayedDataset?.reportingPeriod ?? '');
    },
    areMoreDatasetsViewableSimultaneously(): boolean {
      return (
        this.isMultiview &&
        this.displayedDataset != null &&
        this.receivedMapOfReportingPeriodsToActiveDataMetaInfo.size > 1
      );
    },
  },
});
</script>
