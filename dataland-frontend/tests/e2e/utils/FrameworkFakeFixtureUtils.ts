import { type ReferencedDocuments, pickOneElement } from "@e2e/fixtures/FixtureUtils";
import { DataPointGenerator } from "@e2e/fixtures/common/DataPointFixtures";
import { type CurrencyDataPoint } from "@clients/backend";
import { generateCurrencyValue } from "@e2e/fixtures/common/NumberFixtures";
import { generateReferencedDocuments } from "@e2e/utils/DocumentReference";
import { generateCurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { type BaseDataPoint, type ExtendedDataPoint } from "@/utils/DataPoint";
import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";

export class FrameworkGenerator extends Generator {
  reports: ReferencedDocuments;
  documents: ReferencedDocuments;
  dataPointGenerator: DataPointGenerator;
  constructor(nullProbability = DEFAULT_PROBABILITY) {
    super(nullProbability);
    this.dataPointGenerator = new DataPointGenerator(nullProbability);
    this.reports = this.dataPointGenerator.generateReferencedReports();
    this.documents = generateReferencedDocuments();
  }
  randomBaseDataPoint<T>(input: T): BaseDataPoint<T> | null {
    const document = this.valueOrNull(pickOneElement(Object.values(this.documents)));
    return this.valueOrNull({ value: input, dataSource: document } as BaseDataPoint<T>);
  }
  randomExtendedDataPoint<T>(input: T): ExtendedDataPoint<T> | null {
    return this.valueOrNull(
      this.dataPointGenerator.generateDataPoint(this.valueOrNull(input), this.reports) as ExtendedDataPoint<T>,
    );
  }
  randomCurrencyDataPoint(input = generateCurrencyValue()): CurrencyDataPoint | null {
    const localCurrency = generateCurrencyCode();
    return this.valueOrNull(
      this.dataPointGenerator.generateDataPoint(this.valueOrNull(input), this.reports, localCurrency),
    );
  }
}
