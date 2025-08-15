<template>
  <div
    v-if="displayWarning"
    data-test="datasetDisplayStatusContainer"
    class="flex w-full info-bar"
    style="min-height: 2rem"
  >
    <span class="flex-1 text-center">{{ warningMessage }}</span>
    <PrimeButton
      v-if="existsAcceptedVersion && link"
      :label="buttonLabel"
      icon="pi pi-stopwatch"
      @click="link()"
      data-test="datasetDisplayStatusButton"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue';
import { type DataMetaInformation, QaStatus } from '@clients/backend';
import PrimeButton from 'primevue/button';
import { assertDefined } from '@/utils/TypeScriptUtils';
import router from '@/router';
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
  methods: {
    /**
     * Link to the dataset based on its status.
     */
    link() {
      if (!this.linkURL) return;
      void router.push(this.linkURL);
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
        return 'VIEW ACTIVE';
      } else if (this.areMoreDatasetsViewableSimultaneously) {
        return 'VIEW ALL';
      } else {
        return 'ERROR';
      }
    },
    linkURL(): string | undefined {
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

<style scoped>
.info-bar {
  text-transform: uppercase;
  color: var(--main-color);
  border-color: var(--info-bar-bg);
  border-style: solid;
  border-width: var(--spacing-xxxs);
  font-weight: bold;
  margin: 0.5rem 0;
  align-items: center;
  button {
    margin: 0;
  }
}
</style>
