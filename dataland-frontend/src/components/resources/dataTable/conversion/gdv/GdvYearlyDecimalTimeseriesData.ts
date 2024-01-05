export interface YearlyTimeseriesData<InnerObj> {
  currentYear: number;
  yearlyData: { [key: string]: InnerObj };
}

export type MappedOptionalDecimal<KeyList extends string> = { [K in KeyList]?: number | null };

export type GdvYearlyDecimalTimeseriesDataConfiguration<KeyList extends string> = {
  [K in KeyList]: {
    label: string;
    unitSuffix: string;
  };
};
