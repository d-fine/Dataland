<template>
  <div class="dialog-sm">
    <div v-for="(referencedReportObject, indexOuter) in referencedReportsList" :key="indexOuter" class="row">
      <h4>{{ `Company Reports (${reportingPeriods[indexOuter]})` }}</h4>

      <div
        v-for="(report, nameInner, indexInner) in referencedReportObject"
        :key="indexInner"
        class="row mb-2"
        data-test="previousReportsList"
      >
        <DocumentLink :download-name="nameInner" :reference="report.reference" show-icon />
      </div>

      <!-- <span v-if="indexOuter !== indexOfNewestReportingPeriod">
      <span class="mb-4" style="font-size: 16px" data-test="titleOfReportingperiodInModal">
        {{ `Company Reports (${reportingPeriods[indexOuter]})` }}
      </span>

    </span> -->
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import type { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";
import type { CompanyReport } from "@clients/backend";

export default defineComponent({
  components: { DocumentLink },
  inject: ["dialogRef"],
  name: "PreviousReportsModal",
  data() {
    return {
      reportingPeriods: [] as Array<string>,
      referencedReportsList: [] as Array<{ [p: string]: CompanyReport }>,
      indexOfNewestReportingPeriod: 999 as number,
    };
  },
  created() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      reportingPeriodsForTable: Array<string>;
      referencedReportsForModal: Array<{ [p: string]: CompanyReport }>;
      indexOfNewestReportingPeriodForModal: number;
    };
    this.reportingPeriods = dialogRefData.reportingPeriodsForTable;
    this.referencedReportsList = dialogRefData.referencedReportsForModal;
    this.indexOfNewestReportingPeriod = dialogRefData.indexOfNewestReportingPeriodForModal;
  },
});
</script>

<style scoped>
a:link {
  color: var(--yellow-700);
}
</style>
