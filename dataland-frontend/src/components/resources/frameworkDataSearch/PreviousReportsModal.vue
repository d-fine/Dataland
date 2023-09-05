<template>
  <div class="grid">
    <div class="col-12 text-left">
      <h2 class="mb-0">Previous years reports</h2>
    </div>
    <div v-for="(report, name, index) in referencedReports" :key="index" class="row">
      <div>
        <DocumentLink :download-name="name" :reference="report.reference" font-style="font-semibold" />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import type { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";

export default defineComponent({
  components: { DocumentLink },
  inject: ["dialogRef"],
  name: "PreviousReportsModal",
  data() {
    return {
      reportingPeriod: "" as string,
      referencedReports: {} as object,
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      reportingPeriodForTable: string;
      referencedReports: object;
    };
    this.reportingPeriod = dialogRefData.reportingPeriodForTable;
    this.referencedReports = dialogRefData.referencedReports;
  },
});
</script>

<style scoped>
a:link {
  color: var(--yellow-700);
}
</style>
