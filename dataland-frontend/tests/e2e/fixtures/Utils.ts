import { CompanyReport } from "../../../build/clients/backend";

export class JSONSet extends Set {
  toJSON() {
    return [...this];
  }
}

export type ReferencedReports = { [key: string]: CompanyReport };
