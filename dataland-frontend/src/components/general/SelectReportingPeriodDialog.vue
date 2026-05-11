<template>
  <div data-test="select-reporting-period-dialog">
    <h4 class="title">SELECT YEAR</h4>
    <div class="three-in-row" data-test="reporting-periods">
      <a
        v-for="(element, index) in dataTableContents"
        :class="element.isClickable ? 'link' : ''"
        :key="index"
        @click="element.isClickable ? $emit('selectedReportingPeriod', element) : () => {}"
      >
        {{ element.reportingPeriod }}</a
      >
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue';
import { type DataMetaInformation } from '@clients/backend';
import { compareReportingPeriods } from '@/utils/DataTableDisplay';
import { ReportingPeriodTableActions, type ReportingPeriodTableEntry } from '@/utils/PremadeDropdownDatasets';
import { type StoredDataRequest } from '@clients/communitymanager';

export default defineComponent({
  name: 'SelectReportingPeriodDialog',
  data() {
    return {
      dataTableContents: [] as ReportingPeriodTableEntry[],
    };
  },
  props: {
    mapOfReportingPeriodToActiveDataset: {
      type: Map as PropType<Map<string, DataMetaInformation>>,
    },
    answeredDataRequests: {
      type: Object as PropType<Pick<StoredDataRequest, 'reportingPeriod' | 'dataRequestId'>[]>,
    },
    actionOnClick: {
      type: String as PropType<ReportingPeriodTableActions>,
      required: true,
    },
  },
  emits: ['selectedReportingPeriod'],
  mounted() {
    this.setReportingPeriodDataTableContents();
  },
  methods: {
    /**
     * It extracts data from Dataset and builds a link to edit the report on its basis
     *
     */
    setReportingPeriodDataTableContents(): void {
      if (this.mapOfReportingPeriodToActiveDataset) {
        const sortedReportingPeriodMetaInfoPairs = Array.from(this.mapOfReportingPeriodToActiveDataset.entries()).sort(
          (firstElement, secondElement) => compareReportingPeriods(firstElement[0], secondElement[0])
        );
        for (const [key, value] of sortedReportingPeriodMetaInfoPairs) {
          const answeredDataRequestIds = this.answeredDataRequests
            ?.filter((answeredDataRequest) => {
              return answeredDataRequest.reportingPeriod == key;
            })
            .map((answeredDataRequest) => {
              return answeredDataRequest.dataRequestId;
            });
          let isClickable;
          if (this.actionOnClick == ReportingPeriodTableActions.EditDataset) {
            isClickable = true;
          } else {
            isClickable = answeredDataRequestIds && answeredDataRequestIds.length > 0;
          }

          this.dataTableContents.push({
            reportingPeriod: key,
            editUrl: `/companies/${value.companyId}/frameworks/${value.dataType}/upload?reportingPeriod=${key}`,
            dataRequestId: answeredDataRequestIds,
            actionOnClick: this.actionOnClick,
            isClickable: isClickable,
          } as ReportingPeriodTableEntry);
        }
      }
    },
  },
});
</script>
