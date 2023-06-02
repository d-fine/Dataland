export interface KpiDataObject {
  subcategoryKey: string;
  subcategoryLabel: string;
  kpiKey: string;
  kpiLabel: string;
  kpiDescription: string;
  kpiFormFieldComponent: string;
  content: Kpi;
}

export type KpiValue = number | string | string[] | object | null;

export type Kpi = {
  [dataId: string]: KpiValue;
};
