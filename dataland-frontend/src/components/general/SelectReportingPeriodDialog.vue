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
        const sortedReportingPeriodMetaInfoPairs = Array.from((this.mapOfReportingPeriodToActiveDataset as Map<string, DataMetaInformation>).entries())
            .sort((firstEl, secondEl) => (parseInt(firstEl[0]) - parseInt(secondEl[0])))
        for (const [key, value] of sortedReportingPeriodMetaInfoPairs) {
          this.dataTableContents.unshift({
            reportingPeriod: key,
            editUrl: `/companies/${value.companyId}/frameworks/${value.dataType}/upload?templateDataId=${value.dataId}`,
          });
        }
      }
    },
  },
});
</script>
