export interface KpiDataObject {
  subcategoryKey: string;
  subcategoryLabel: string;
  kpiKey: string;
  kpiLabel: string;
  kpiDescription: string;

  [index: string]: string;
}

export type KpiValue = number | string | string[] | object | null;
