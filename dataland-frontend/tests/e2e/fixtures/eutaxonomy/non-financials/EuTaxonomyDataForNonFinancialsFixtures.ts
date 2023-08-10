import {
  Activity,
  EuTaxonomyActivity, EuTaxonomyAlignedActivity,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDetailsPerCashFlowType,
  FinancialShare, YesNo
} from "@clients/backend";
import {
  generateArray,
  getRandomNumberOfDistinctElementsFromArray,
  ReferencedDocuments
} from "@e2e/fixtures/FixtureUtils";
import { generateDatapoint, generateDatapointAbsoluteAndPercentage } from "@e2e/fixtures/common/DataPointFixtures";
import { generateEuTaxonomyBaseFields } from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import { randomEuroValue, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import {faker} from "@faker-js/faker";
import {generateIso4217CurrencyCode} from "@e2e/fixtures/common/CurrencyFixtures";
import {generateListOfNaceCodes} from "@e2e/fixtures/common/NaceCodeFixtures";
import {EnvironmentalObjective} from "@/api-models/EnvironmentalObjective";

function generatePercentage(): number {
  return faker.number.float({ min: 0, max: 100 })
}

export function generateFinancialShare(): FinancialShare {
  return {
    percentage: generatePercentage(),
    absoluteShare: faker.number.float(),
    currency: generateIso4217CurrencyCode(),
  };
}

function generateActivity(): EuTaxonomyActivity {
  return {
    activityName: faker.helpers.arrayElement(Object.values(Activity)),
    naceCodes: valueOrUndefined(generateListOfNaceCodes()),
    share: valueOrUndefined(generateFinancialShare()),
  }
}

function generateMap<K, V>(possibleKeys: Array<K>, valueGenerator: () => V): Map<K, V> {
  const keys = getRandomNumberOfDistinctElementsFromArray(Array.from(possibleKeys))
  return new Map<K,V>(keys.map((key) => [key, valueGenerator()]))
}

function generateObject<V>(keyProvider: object, valueGenerator: () => V): { [p: string]: V } {
  return Object.fromEntries(
      generateMap(
          Object.values(EnvironmentalObjective),
          () => valueGenerator(),
      )
  )
}

function generateAlignedActivity(): EuTaxonomyAlignedActivity {
  return {
    ...generateActivity(),
    substantialContributionCriteria: generateObject(EnvironmentalObjective, generatePercentage),
    dnshCriteria: generateObject(EnvironmentalObjective, () => faker.helpers.arrayElement(Object.values(YesNo))),
    minimumSafeguards: valueOrUndefined(faker.helpers.arrayElement(Object.values(YesNo)))
  }
}

/**
 * Generates fake data for a single cash-flow type for the eutaxonomy-non-financials framework
 * @param reports a list of reports that can be referenced
 * @returns the generated data
 */
export function generateEuTaxonomyPerCashflowType(reports: ReferencedDocuments): EuTaxonomyDetailsPerCashFlowType {
  return {
    totalAmount: valueOrUndefined(generateDatapoint(valueOrUndefined(randomEuroValue()), reports)),
    totalNonEligibleShare: valueOrUndefined(generateFinancialShare()),
    totalEligibleShare: valueOrUndefined(generateFinancialShare()),
    totalEligibleNonAlignedShare: valueOrUndefined(generateFinancialShare()),
    totalAlignedShare: valueOrUndefined(generateFinancialShare()),
    eligibleNotAlignedActivities: valueOrUndefined(generateArray(generateActivity)),
    alignedActivities: valueOrUndefined(generateArray(generateAlignedActivity)),
  };
}

/**
 * Generates a single fixture for the eutaxonomy-non-financials framework
 * @returns the generated fixture
 */
export function generateEuTaxonomyDataForNonFinancials(): EuTaxonomyDataForNonFinancials {
  const data: EuTaxonomyDataForNonFinancials = {};
  data.general = generateEuTaxonomyBaseFields();
  data.opex = generateEuTaxonomyPerCashflowType(assertDefined(data.general.referencedReports));
  data.capex = generateEuTaxonomyPerCashflowType(assertDefined(data.general.referencedReports));
  data.revenue = generateEuTaxonomyPerCashflowType(assertDefined(data.general.referencedReports));
  return data;
}
