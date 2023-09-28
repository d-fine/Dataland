import fs from "fs";
import { type DataAndMetaInformationEuTaxonomyDataForNonFinancials } from "@clients/backend";
import { DataMetaInformationGenerator } from "@e2e/fixtures/data_meta_information/DataMetaInformationFixtures";
import {
  EuNonFinancialsGenerator,
  generateEuTaxonomyDataForNonFinancials,
} from "@e2e/fixtures/eutaxonomy/non-financials/EuTaxonomyDataForNonFinancialsFixtures";
import { generatePercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { DEFAULT_PROBABILITY } from "@e2e/utils/FakeFixtureUtils";
import { generateArray } from "@e2e/fixtures/FixtureUtils";
import { generateNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";

/**
 * Generates and exports fake fixtures for the LKSG framework
 */
export function exportServerResponses(): void {
  fs.writeFileSync(
    "../testing/data/EuTaxonomyForNonFinancialsMocks.json",
    JSON.stringify(generateEuTaxonomyForNonFinancialsMocks(), null, "\t"),
  );
}

/**
 * Generates a list of data and meta information with EU taxonomy for non financials data
 * @returns a list of data and meta information with EU taxonomy for non financials data
 */
function generateEuTaxonomyForNonFinancialsMocks(): DataAndMetaInformationEuTaxonomyDataForNonFinancials[] {
  const dataMetaInfoGenerator = new DataMetaInformationGenerator(true);
  const generatedDataAndMetaInfo = Array.from(Array(3).keys()).map(
    (): DataAndMetaInformationEuTaxonomyDataForNonFinancials => ({
      metaInfo: dataMetaInfoGenerator.generateDataMetaInformation(),
      data: generateEuTaxonomyDataForNonFinancials(true),
    }),
  );
  const euTaxonomyNonFinancialsGenerator = new EuNonFinancialsGenerator(0, true);
  const data = generatedDataAndMetaInfo[0].data;
  data.revenue = euTaxonomyNonFinancialsGenerator.generateEuTaxonomyPerCashflowType();
  data.opex = euTaxonomyNonFinancialsGenerator.generateEuTaxonomyPerCashflowType();
  data.capex = euTaxonomyNonFinancialsGenerator.generateEuTaxonomyPerCashflowType();
  euTaxonomyNonFinancialsGenerator.missingValueProbability = DEFAULT_PROBABILITY;
  data.capex.nonAlignedActivities = generateArray(() => euTaxonomyNonFinancialsGenerator.generateActivity(), 1);
  data.capex.nonAlignedActivities[0].naceCodes = generateNaceCodes(1);
  data.capex.nonAlignedActivities[0].share ??= {};
  data.capex.nonAlignedActivities[0].share.relativeShareInPercent = generatePercentageValue();
  data.capex.nonAlignedActivities[0].share.absoluteShare ??= euTaxonomyNonFinancialsGenerator.generateAmountWithCurrency();
  data.capex.nonAlignedShare ??= {};
  data.capex.nonAlignedShare.relativeShareInPercent ??= generatePercentageValue();
  generatedDataAndMetaInfo[0].data = data;
  return generatedDataAndMetaInfo;
}
