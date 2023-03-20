<template>
  <h4 class="title">SELECT YEAR</h4>
  <div class="three-in-row">
    <router-link v-for="(el, index) in dataTableContents" :key="index" class="link" :to="el.editUrl">{{
      el.reportingPeriod
    }}</router-link>
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
        let key: string;
        let value: DataMetaInformation;
        for ([key, value] of this.mapOfReportingPeriodToActiveDataset as Map<string, DataMetaInformation>) {
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
