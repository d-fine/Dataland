import { CompanyReport } from "@clients/backend";

export interface ExtendedFile extends File {
  reportDate: string;
  convertedReportDate: string | Date;
  documentId: string;
  [key: string]: unknown;
}

export interface ExtendedCompanyReport extends CompanyReport {
  name: string;
  convertedReportDate: string | Date;
  [key: string]: unknown;
}

export type WhichSetOfFiles = "uploadFiles" | "filesToUpload";
