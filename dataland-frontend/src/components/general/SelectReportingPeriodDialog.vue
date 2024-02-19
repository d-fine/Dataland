<template>
  <div data-test="select-reporting-period-dialog">
    <h4 class="title">SELECT YEAR</h4>
    <div class="three-in-row" data-test="reporting-periods">
      <a v-for="(el, index) in dataTableContents" :key="index" class="link" @click="onClick($event, el)"
        >{{ el.reportingPeriod }}
      </a>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { type DataMetaInformation } from "@clients/backend";
import { compareReportingPeriods } from "@/utils/DataTableDisplay";

interface ReportingPeriodTableEntry {
  reportingPeriod: string;
  editUrl: string;
}

export default defineComponent({
  name: "SelectReportingPeriodDialog",
  data() {
    return {
      dataTableContents: [] as ReportingPeriodTableEntry[],
    };
  },
  props: {
    mapOfReportingPeriodToActiveDataset: {
      type: Map,
    },
  },
  emits: ["selectedReportingPeriod"],
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
        const sortedReportingPeriodMetaInfoPairs = Array.from(
          (this.mapOfReportingPeriodToActiveDataset as Map<string, DataMetaInformation>).entries(),
        ).sort((firstEl, secondEl) => compareReportingPeriods(firstEl[0], secondEl[0]));
        for (const [key, value] of sortedReportingPeriodMetaInfoPairs) {
          this.dataTableContents.push({
            reportingPeriod: key,
            editUrl: `/companies/${value.companyId}/frameworks/${value.dataType}/upload?templateDataId=${value.dataId}`,
          });
        }
      }
    },
  },
  onClick(ev: Event, el: ReportingPeriodTableEntry): void {
    console.log("click event");
    console.log(ev);
    console.log("element");
    console.log(el);
  },
});
</script>
