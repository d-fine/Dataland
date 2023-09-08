<template>
  <div style="display: flex">
    <h2 class="mb-3" style="font-size: 16px" data-test="frameworkNewDataTableTitle">
      {{ `Data extracted from the company report.Company Reports(${reportingPeriods[indexOfNewestReportingPeriod]}):` }}
    </h2>
    <span id="reportList">
      <span v-for="(report, name, index) in reports[indexOfNewestReportingPeriod]" :key="index" style="font-size: 16px">
        <DocumentLink :download-name="name" :reference="report.reference" font-style="font-semibold" />
        <span v-if="index < reports[indexOfNewestReportingPeriod].length - 1"> | </span>
      </span>
      <span
        v-if="doPreviousReportsExist(reports, indexOfNewestReportingPeriod)"
        class="link"
        style="text-align: right"
        @click="openModalAndDisplayPreviousReportsInTable(reportingPeriods)"
        >Previous years reports
      </span>
    </span>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";
import PreviousReportsModal from "@/components/resources/frameworkDataSearch/PreviousReportsModal.vue";
import type { CompanyReport } from "@clients/backend";

export default defineComponent({
  name: "ShowMultipleReportsBanner",
  components: { DocumentLink },
  data() {
    return {
      indexOfNewestReportingPeriod: -1 as number,
    };
  },
  props: {
    reports: { type: Array<{ [p: string]: CompanyReport }>, required: true },
    reportingPeriods: { type: Array<string>, required: true },
  },
  mounted() {
    this.indexOfNewestReportingPeriod = this.calculateIndexOfNewestReportingPeriod(this.reportingPeriods);
  },
  methods: {
    /**
     * Opens a modal to display a table containing previous referenced reports
     * @param reportingPeriods States the origin year of the report.
     */
    openModalAndDisplayPreviousReportsInTable(reportingPeriods: Array<string>) {
      this.indexOfNewestReportingPeriod = this.calculateIndexOfNewestReportingPeriod(this.reportingPeriods);
      const passedData = {
        reportingPeriodsForTable: reportingPeriods,
        referencedReportsForModal: this.reports,
        indexOfNewestReportingPeriodForModal: this.indexOfNewestReportingPeriod,
      };
      this.$dialog.open(PreviousReportsModal, {
        props: {
          modal: true,
          dismissableMask: true,
        },
        data: passedData,
      });
    },

    /**
     * Returns the index of the with the newest reporting period in the array containing all reporting periods.
     * @param reportingPeriods array containing all reporting periods.
     */
    calculateIndexOfNewestReportingPeriod(reportingPeriods: Array<string>): number {
      let indexOfHighestReportingPeriod = 0;
      let tempHighestReportingPeriodNumber = 0;
      for (let i = 0; i < reportingPeriods.length; i++) {
        if (Number(reportingPeriods[i]) > tempHighestReportingPeriodNumber) {
          tempHighestReportingPeriodNumber = Number(reportingPeriods[i]);
          indexOfHighestReportingPeriod = i;
        }
      }
      return indexOfHighestReportingPeriod;
    },

    /**
     * Checks whether a report of the previous year exists.
     * @param reports array of all reports
     * @param indexOfNewestReport index of newest report in the reports array
     */
    doPreviousReportsExist(reports: Array<{ [p: string]: CompanyReport }>, indexOfNewestReport: number): boolean {
      if (!indexOfNewestReport) return false;
      let reportsFound = false;
      reports.forEach((report, index) => {
        if (index != indexOfNewestReport) {
          if (report) {
            reportsFound = true;
          }
        }
      });
      return reportsFound;
    },
  },
});
</script>

<style scoped>
a:link {
  color: var(--yellow-700);
}
</style>
