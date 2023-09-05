<template>
  <div style="display: flex">
    <h2 class="mb-0" style="font-size: 12px" data-test="frameworkNewDataTableTitle">
      {{ `Data extracted from the company report.Company Reports(${reportingPeriod}):` }}
    </h2>
    <span id="reportList">
      <span v-for="(report, name, index) in reports" :key="index">
        <DocumentLink :download-name="name" :reference="report.reference" font-style="font-semibold" />
        <span v-if="index < numberOfReports - 1"> | </span>
      </span>
      <span> </span>
    </span>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import AlignedActivitiesDataTable from "@/components/general/AlignedActivitiesDataTable.vue";
import NonAlignedActivitiesDataTable from "@/components/general/NonAlignedActivitiesDataTable.vue";

export default defineComponent({
  name: "ShowMultipleReportsBanner",
  components: { DocumentLink },
  props: {
    reports: { type: Object, required: true },
    reportingPeriod: { type: String, required: true },
  },
  computed: {
    numberOfReports(): number {
      return Object.keys(this.reports).length;
    },
  },
});
</script>

<style scoped>
a:link {
  color: var(--yellow-700);
}
</style>
