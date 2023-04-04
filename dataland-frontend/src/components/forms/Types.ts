export interface ExtendedFile extends File {
  reportDate: string;
  convertedReportDate: string;
  documentId: string;
  [key: string]: unknown;
}
