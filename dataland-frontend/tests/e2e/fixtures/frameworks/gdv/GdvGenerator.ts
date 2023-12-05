import { Generator } from "@e2e/utils/FakeFixtureUtils";

interface YearlyTimeseriesData<InnerObj> {
  currentYear: number;
  yearlyData: { [key: string]: InnerObj };
}

type MappedOptionalDecimal<KeyList extends string> = { [K in KeyList]?: number | null };

export class GdvGenerator extends Generator {
  randomDecimalYearlyTimeseriesData<T extends string>(
    keys: T[],
  ): YearlyTimeseriesData<MappedOptionalDecimal<T>> | null {
    return this.valueOrNull(this.guaranteedDecimalYearlyTimeseriesData(keys));
  }
  guaranteedDecimalYearlyTimeseriesData<T extends string>(keys: T[]): YearlyTimeseriesData<MappedOptionalDecimal<T>> {
    const baseYear = 2023 + this.guaranteedInt(5);
    const yearlyData: { [key: string]: MappedOptionalDecimal<T> } = {};

    for (let year = baseYear - 2; year <= baseYear + 2; year++) {
      const value = this.valueOrNull(this.guaranteedDecimalDataObject(keys));
      if (value != null) {
        yearlyData[`${year}`] = value;
      }
    }

    return {
      currentYear: baseYear,
      yearlyData: yearlyData,
    };
  }
  guaranteedDecimalDataObject<T extends string>(keys: T[]): MappedOptionalDecimal<T> {
    const dataObject = {} as MappedOptionalDecimal<T>;
    for (const key of keys) {
      dataObject[key] = this.randomFloat();
    }
    return dataObject;
  }
}
