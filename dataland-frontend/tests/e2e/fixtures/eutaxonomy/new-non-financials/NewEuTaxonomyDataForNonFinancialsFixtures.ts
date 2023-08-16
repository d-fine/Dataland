import {
  Activity,
  EuTaxonomyActivity,
  EuTaxonomyAlignedActivity,
  FinancialShare, MoneyAmount,
  NewEuTaxonomyDataForNonFinancials,
  NewEuTaxonomyDetailsPerCashFlowType,
  YesNo,
} from "@clients/backend";
import {
  generateArray,
  getRandomNumberOfDistinctElementsFromArray,
  ReferencedDocuments,
} from "@e2e/fixtures/FixtureUtils";
import { generateDatapoint } from "@e2e/fixtures/common/DataPointFixtures";
import { generateEuTaxonomyWithBaseFields } from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import { randomEuroValue } from "@e2e/fixtures/common/NumberFixtures";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { faker } from "@faker-js/faker";
import { generateListOfNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateIso4217CurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { EnvironmentalObjective } from "@/api-models/EnvironmentalObjective";

/**
 * Generates a random percentage between 0 and 100
 * @returns a reandom percentage
 */
function generatePercentage(): number {
  return faker.number.float({ min: 0, max: 100 });
}

/**
 * Generates a random amount of money
 * @returns an amount of money
 */
export function generateMoneyAmount(): MoneyAmount {
  return {
    amount: valueOrUndefined(randomEuroValue()),
    currency: valueOrUndefined(generateIso4217CurrencyCode()),
  };
}

/**
 * Generates a random financial share
 * @returns a financial share
 */
export function generateFinancialShare(): FinancialShare {
  return {
    percentage: valueOrUndefined(generatePercentage()),
    absoluteShare: valueOrUndefined(generateMoneyAmount()),
  };
}

/**
 * Generates a random activity
 * @returns a random activity
 */
function generateActivity(): EuTaxonomyActivity {
  return {
    activityName: faker.helpers.arrayElement(Object.values(Activity)),
    naceCodes: valueOrUndefined(generateListOfNaceCodes()),
    share: valueOrUndefined(generateFinancialShare()),
  };
}

/**
 * Generates a map with keys drawn from the provided array and generated values
 * @param possibleKeys the keys that can occur in the resulting map
 * @param valueGenerator the generator function for the values
 * @returns the generated map
 */
function generateMap<K, V>(possibleKeys: Array<K>, valueGenerator: () => V): Map<K, V> {
  const keys = getRandomNumberOfDistinctElementsFromArray(Array.from(possibleKeys));
  return new Map<K, V>(keys.map((key) => [key, valueGenerator()]));
}

/**
 * Generates an object with keys drawn from the provided objects values and generated values
 * @param possibleKeys the keys that can occur in the resulting map
 * @param valueGenerator the generator function for the values
 * @returns the generated map
 */
function generateObject<V>(possibleKeys: Array<string>, valueGenerator: () => V): { [p: string]: V } {
  return Object.fromEntries(generateMap(possibleKeys, () => valueGenerator()));
}

/**
 * Generates a random aligned activity
 * @returns a random aligned activity
 */
function generateAlignedActivity(): EuTaxonomyAlignedActivity {
  return {
    ...generateActivity(),
    substantialContributionCriteria: generateObject(Object.values(EnvironmentalObjective), generatePercentage),
    dnshCriteria: generateObject(Object.values(EnvironmentalObjective), () =>
      faker.helpers.arrayElement(Object.values(YesNo)),
    ),
    minimumSafeguards: valueOrUndefined(faker.helpers.arrayElement(Object.values(YesNo))),
  };
}

/**
 * Generates fake data for a single cash-flow type for the eutaxonomy-non-financials framework
 * @param reports a list of reports that can be referenced
 * @returns the generated data
 */
export function generateNewEuTaxonomyPerCashflowType(
  reports: ReferencedDocuments,
): NewEuTaxonomyDetailsPerCashFlowType {
  return {
    totalAmount: valueOrUndefined(generateDatapoint(valueOrUndefined(generateMoneyAmount()), reports)),
    totalNonEligibleShare: valueOrUndefined(generateFinancialShare()),
    totalEligibleShare: valueOrUndefined(generateFinancialShare()),
    totalNonAlignedShare: valueOrUndefined(generateFinancialShare()),
    nonAlignedActivities: valueOrUndefined(generateArray(generateActivity)),
    totalAlignedShare: valueOrUndefined(generateFinancialShare()),
    substantialContributionCriteria: generateObject(Object.values(EnvironmentalObjective), generatePercentage),
    alignedActivities: valueOrUndefined(generateArray(generateAlignedActivity)),
    totalEnablingShare: valueOrUndefined(generatePercentage()),
    totalTransitionalShare: valueOrUndefined(generatePercentage()),
  };
}

/**
 * Generates a single fixture for the eutaxonomy-non-financials framework
 * @returns the generated fixture
 */
export function generateNewEuTaxonomyDataForNonFinancials(): NewEuTaxonomyDataForNonFinancials {
  const data: NewEuTaxonomyDataForNonFinancials = {};
  data.general = generateEuTaxonomyWithBaseFields();
  data.opex = generateNewEuTaxonomyPerCashflowType(assertDefined(data.general.referencedReports));
  data.capex = generateNewEuTaxonomyPerCashflowType(assertDefined(data.general.referencedReports));
  data.revenue = generateNewEuTaxonomyPerCashflowType(assertDefined(data.general.referencedReports));
  return data;
}
