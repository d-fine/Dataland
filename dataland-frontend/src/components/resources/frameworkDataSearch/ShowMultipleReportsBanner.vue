<template>
  <div class="next-to-each-other my-4">
    <h4 class="m-0" data-test="frameworkNewDataTableTitle">
      {{ `Data extracted from the company report. Company Reports(${reportingPeriods[indexOfNewestReportingPeriod]}):` }}
    </h4>
    <span id="reportList">
      <span v-for="(report, name, index) in reports[indexOfNewestReportingPeriod]" :key="index" class="link-in-list">
        <DocumentLink
          data-test="documentLinkTest"
          :download-name="name"
          :reference="report.reference"
          font-style="font-semibold"
        />
      </span>
    </span>
    <span
      v-if="doPreviousReportsExist(reports, indexOfNewestReportingPeriod)"
      class="link font-semibold underline mr-0 ml-auto"
      @click="openModalAndDisplayPreviousReportsInTable(reportingPeriods)"
      data-test="previousReportsLinkToModal"
      >Previous years reports
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
      const passedData = {
        reportingPeriodsForTable: reportingPeriods,
        referencedReportsForModal: this.reports,
        indexOfNewestReportingPeriodForModal: this.indexOfNewestReportingPeriod,
      };
      this.$dialog.open(PreviousReportsModal, {
        props: {
          header: "Previous years reports",
          modal: true,
          dismissableMask: true,
        },
        data: passedData,
      });
    },

    /**
     * Returns the index of the with the newest reporting period in the array containing all reporting periods.
     * @param reportingPeriods array containing all reporting periods.
     * @returns index of the newest reporting period
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
     * @returns returns a boolean wheter a report has been found
     */
    doPreviousReportsExist(reports: Array<{ [p: string]: CompanyReport }>, indexOfNewestReport: number): boolean {
      if (!reports) return false;
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
