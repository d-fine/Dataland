<template>
  <div data-test="select-reporting-period-dialog">
    <h4 class="title">SELECT YEAR</h4>
    <div class="three-in-row" data-test="reporting-periods">
      <router-link v-for="(el, index) in dataTableContents" :key="index" class="link" :to="el.editUrl">{{
        el.reportingPeriod
      }}</router-link>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { DataMetaInformation } from "@clients/backend";

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
  mounted() {
    this.reportingPeriodDataTableContents();
  },
  methods: {
    /**
     * It extracts data from Dataset and builds a link to edit the report on its basis
     *
     */
    reportingPeriodDataTableContents(): void {
      if (this.mapOfReportingPeriodToActiveDataset) {
        const sortedReportingPeriodMetaInfoPairs = Array.from(
          (this.mapOfReportingPeriodToActiveDataset as Map<string, DataMetaInformation>).entries()
        ).sort((firstEl, secondEl) => this.reportingPeriodComparator(firstEl[0], secondEl[0]));
        for (const [key, value] of sortedReportingPeriodMetaInfoPairs) {
          this.dataTableContents.unshift({
            reportingPeriod: key,
            editUrl: `/companies/${value.companyId}/frameworks/${value.dataType}/upload?templateDataId=${value.dataId}`,
          });
        }
      }
    },
    /**
     * Compares two reporting periods for sorting
     * @param firstReportingPeriod the first reporting period to compare
     * @param secondReportingPeriod the reporting period to compare with
     * @returns 1 if the first reporting period should be sorted after to the second one else -1
     */
    reportingPeriodComparator(firstReportingPeriod: string, secondReportingPeriod: string): number {
      if (!isNaN(Number(firstReportingPeriod)) && !isNaN(Number(secondReportingPeriod))) {
        if (Number(firstReportingPeriod) < Number(secondReportingPeriod)) {
          return 1;
        } else {
          return -1;
        }
      } else if(!isNaN(Number(firstReportingPeriod))) {
          return -1;
      } else if(!isNaN(Number(secondReportingPeriod))) {
          return 1;
      } else {
        if (firstReportingPeriod > secondReportingPeriod) {
          return 1;
        } else {
          return -1;
        }
      }
    },
  },
});
</script>
