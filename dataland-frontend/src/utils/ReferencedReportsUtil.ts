import ReportDataTable from '@/components/general/ReportDataTable.vue';
import type { CompanyReport } from '@clients/backend';

/**
 * Opens a modal to display the details of the selected report.
 * @param context For coupling the environment.
 * @param report The report data.
 * @param reportName The name of the report.
 */
export function openReportDataTableModal(context: ComponentContext, report: CompanyReport, reportName: string): void {
  const options = constructModalOptions(report, reportName);
  context.$dialog.open(ReportDataTable, {
    props: options.props,
    data: options.data,
  });
}

/**
 * Constructs the modal options for the ReportDataTable.
 * @param report The report data.
 * @param reportName The name of the report.
 * @returns The modal options.
 */
function constructModalOptions(report: CompanyReport, reportName: string): ModalOptions {
  const reportWithName: CompanyReport = {
    ...report,
    fileName: report.fileName ? report.fileName : reportName,
  };

  return {
    props: {
      header: 'Report Details',
      modal: true,
      dismissableMask: true,
    },
    data: {
      companyReport: reportWithName,
    },
  };
}

interface ModalOptions {
  props: {
    header: string;
    modal: boolean;
    dismissableMask: boolean;
  };
  data: {
    companyReport: CompanyReport;
  };
}

interface ComponentContext {
  $dialog: {
    //eslint-disable-next-line @typescript-eslint/no-explicit-any
    open(component: any, options: any): void;
  };
}
