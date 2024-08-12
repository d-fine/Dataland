<template>
  <div class="next-to-each-other my-4">
    <h4 class="m-0" data-test="frameworkNewDataTableTitle">
      Data extracted from the company report. Company Reports ({{ reportingPeriods[indexOfNewestReportingPeriod] }}):
    </h4>
    <div id="reportList" style="display: flex">
      <span v-for="(report, name, index) in reports[indexOfNewestReportingPeriod]" :key="index" class="link-in-list">
        <a @click="openReportDataTableModal(report, name as string)" class="link" :data-test="`report-link-${name}`">
          <span>{{ name ? name : 'Unnamed_File' }}</span>
        </a>
      </span>
    </div>
    <span
      v-if="doPreviousReportsExist(reports, indexOfNewestReportingPeriod)"
      class="link font-semibold underline mr-0 ml-auto"
      @click="openModalAndDisplayPreviousReportsInTable(reportingPeriods)"
      data-test="previousReportsLinkToModal"
    >
      Previous years reports
    </span>
  </div>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue';
import PreviousReportsModal from '@/components/resources/frameworkDataSearch/PreviousReportsModal.vue';
import type { CompanyReport } from '@clients/backend';
import { openReportDataTableModal } from '@/utils/ReferencedReportsUtil';

export default defineComponent({
  name: 'ShowMultipleReportsBanner',
  data() {
    return {
      indexOfNewestReportingPeriod: -1 as number,
    };
  },
  props: {
    reports: { type: Array as PropType<Record<string, CompanyReport>[]>, required: true },
    reportingPeriods: { type: Array as PropType<string[]>, required: true },
  },
  mounted() {
    this.indexOfNewestReportingPeriod = this.calculateIndexOfNewestReportingPeriod(this.reportingPeriods);
  },
  methods: {
    /**
     * Opens a modal to display a table containing detailed information about the report.
     * @param report the report
     * @param reportName the name of the report
     */
    openReportDataTableModal(report: CompanyReport, reportName: string) {
      openReportDataTableModal(this, report, reportName);
    },

    /**
     * Opens a modal to display a table containing previous referenced reports.
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
          header: 'Previous years reports',
          modal: true,
          dismissableMask: true,
        },
        data: passedData,
      });
    },

    /**
     * Returns the index of the with the newest reporting period in the array containing all reporting periods.
     * @param reportingPeriods Array containing all reporting periods.
     * @returns Index of the newest reporting period.
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
     * @param reports Array of all reports.
     * @param indexOfNewestReport Index of newest report in the reports array.
     * @returns Returns a boolean whether a report has been found.
     */
    doPreviousReportsExist(reports: Array<{ [p: string]: CompanyReport }>, indexOfNewestReport: number): boolean {
      if (!reports) {
        return false;
      }
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
