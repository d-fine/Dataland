<template>
  <div class="grid" v-if="referencedReportsList.length > 0">
    <div class="col-12 text-left">
      <h2 class="mb-0">Previous years reports</h2>
    </div>
    <span v-for="(referencedReportObject, indexOuter) in referencedReportsList" :key="indexOuter" class="row">
      <div v-if="indexOuter !== indexOfNewestReportingPeriod + 100">
        <h2 class="mb-0" style="font-size: 12px" data-test="titleOfReportingperiodInModal">
          {{ `${reportingPeriods[indexOuter]}:` }}
        </h2>
        <div v-for="(report, nameInner, indexInner) in referencedReportObject" :key="indexInner" class="row">
          <div>
            <DocumentLink :download-name="nameInner" :reference="report.reference" font-style="font-semibold" />
          </div>
        </div>
      </div>
    </span>
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
