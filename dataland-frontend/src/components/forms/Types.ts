import { CompanyReport } from "@clients/backend";

export interface ExtendedFile extends File {
  reportDate: string;
  reportDateAsDate: string | Date;
  documentId: string;
  [key: string]: unknown;
}

export interface ExtendedCompanyReport extends CompanyReport {
  name: string;
  reportDateAsDate: string | Date;
  [key: string]: unknown;
}
