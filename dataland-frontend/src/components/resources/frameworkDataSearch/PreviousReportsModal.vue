<template>
  <div class="dataland-dialog dataland-dialog-sm" data-test="previousReportsList">
    <div v-for="(referencedReportObject, indexOuter) in referencedReportsList" :key="indexOuter" class="row">
      <div v-if="indexOuter !== indexOfNewestReportingPeriod">
        <h4>{{ `Company Reports (${reportingPeriods[indexOuter]})` }}</h4>
        <div
          v-for="(report, nameInner, indexInner) in referencedReportObject"
          :key="indexInner"
          class="row mb-2"
          data-test="previousReportsList"
        >
          <a
            @click="openReportDataTableModal(report, nameInner as string)"
            class="link"
            :data-test="`report-link-${nameInner}`"
          >
            <span>{{ nameInner }}</span>
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import type { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import type { CompanyReport } from "@clients/backend";
import ReportDataTable from "@/components/general/ReportDataTable.vue";

export default defineComponent({
  inject: ["dialogRef"],
  name: "PreviousReportsModal",
  data() {
    return {
      reportingPeriods: [] as Array<string>,
      referencedReportsList: [] as Array<{ [p: string]: CompanyReport }>,
      indexOfNewestReportingPeriod: 9999 as number,
    };
  },
  methods: {
    /**
     * Opens a modal to display the details of the selected report.
     * @param report The report data.
     * @param reportName The name of the report.
     */
    openReportDataTableModal(report: CompanyReport, reportName: string) {
      const options = this.constructModalOptions(report, reportName);
      this.$dialog.open(ReportDataTable, {
        props: options.props,
        data: options.data,
      });
    },

    /**
     * Constructs the modal options for the ReportDataTable.
     * @param report The report data.
     * @param reportName The name of the report.
     * @returns The modal options.
     */
    /**
     * Constructs the modal options for the ReportDataTable.
     * @param report The report data.
     * @param reportName The name of the report.
     * @returns The modal options.
     */
    constructModalOptions(report: CompanyReport, reportName: string) {
      const reportWithName: CompanyReport = {
        ...report,
        fileName: report.fileName ? report.fileName : reportName,
      };

      return {
        props: {
          header: "Report Details",
          modal: true,
          dismissableMask: true,
        },
        data: {
          companyReport: reportWithName,
        },
      };
    },
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
