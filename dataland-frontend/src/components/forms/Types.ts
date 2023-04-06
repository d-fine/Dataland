import { CompanyReport } from "@clients/backend";

export interface ExtendedFile extends File {
  reportDate: string;
  convertedReportDate: string;
  documentId: string;
  [key: string]: unknown;
}

export interface ExtendedCompanyReport extends CompanyReport {
  name: string;
  convertedReportDate: Date;
  [key: string]: unknown;
}

export type WhichSetOfFiles = "uploadFiles" | "filesToUpload";
