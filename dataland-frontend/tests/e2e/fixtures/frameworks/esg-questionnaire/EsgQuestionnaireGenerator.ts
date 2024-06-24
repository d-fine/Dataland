import { DEFAULT_PROBABILITY, Generator } from '@e2e/utils/FakeFixtureUtils';
interface YearlyTimeseriesData<InnerObj> {
  currentYear: number;
  yearlyData: { [key: string]: InnerObj };
}

type MappedOptionalDecimal<KeyList extends string> = { [K in KeyList]?: number | null };

export class EsgQuestionnaireGenerator extends Generator {
  dataDate: string;

  constructor(nullProbability = DEFAULT_PROBABILITY) {
    super(nullProbability);
    this.dataDate = this.guaranteedFutureDate();
  }

  randomDecimalYearlyTimeseriesData<T extends string>(
    keys: T[],
    nYearsIntoPast: number,
    nYearsIntoFuture: number
  ): YearlyTimeseriesData<MappedOptionalDecimal<T>> | null {
    return this.valueOrNull(this.guaranteedDecimalYearlyTimeseriesData(keys, nYearsIntoPast, nYearsIntoFuture));
  }
  guaranteedDecimalYearlyTimeseriesData<T extends string>(
    keys: T[],
    nYearsIntoPast: number,
    nYearsIntoFuture: number
  ): YearlyTimeseriesData<MappedOptionalDecimal<T>> {
    const baseYear = parseInt(this.dataDate.substring(0, 4));
    const yearlyData: { [key: string]: MappedOptionalDecimal<T> } = {};

    for (let year = baseYear - nYearsIntoPast; year <= baseYear + nYearsIntoFuture; year++) {
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
