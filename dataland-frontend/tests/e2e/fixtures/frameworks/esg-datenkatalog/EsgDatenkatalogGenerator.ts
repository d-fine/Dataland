import { DEFAULT_PROBABILITY, Generator } from '@e2e/utils/FakeFixtureUtils';

export class EsgDatenkatalogGenerator extends Generator {
  dataDate: string;

  constructor(nullProbability = DEFAULT_PROBABILITY) {
    super(nullProbability);
    this.dataDate = this.guaranteedFutureDate();
  }
}
