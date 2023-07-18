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
          (this.mapOfReportingPeriodToActiveDataset as Map<string, DataMetaInformation>).entries()
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
});
</script>
