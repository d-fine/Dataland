import fs from "fs";
import {type DataAndMetaInformationEuTaxonomyDataForNonFinancials, QualityOptions} from "@clients/backend";
import { DataMetaInformationGenerator } from "@e2e/fixtures/data_meta_information/DataMetaInformationFixtures";
import {
  EuNonFinancialsGenerator,
  generateEuTaxonomyDataForNonFinancials,
} from "@e2e/fixtures/eutaxonomy/non-financials/EuTaxonomyDataForNonFinancialsFixtures";
import { generatePercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { DEFAULT_PROBABILITY } from "@e2e/utils/FakeFixtureUtils";
import {generateArray, pickOneElement} from "@e2e/fixtures/FixtureUtils";
import { generateNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
import {generateReferencedReports} from "@e2e/fixtures/common/DataPointFixtures";

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
    (index): DataAndMetaInformationEuTaxonomyDataForNonFinancials => {
      const metaInfo = dataMetaInfoGenerator.generateDataMetaInformation()
      metaInfo.reportingPeriod = "202" + (3-index)
      return{
        metaInfo: metaInfo,
        data: generateEuTaxonomyDataForNonFinancials(true),
      }
    },
  );
  const euTaxonomyNonFinancialsGenerator = new EuNonFinancialsGenerator(0, true);
  var data = generateEuTaxonomyDataForNonFinancials(true, 0);
  data.general ??= {}
  data.general.referencedReports = generateReferencedReports(DEFAULT_PROBABILITY, true, ["IntegratedReport"])
  data.revenue = euTaxonomyNonFinancialsGenerator.generateEuTaxonomyPerCashflowType();
  data.revenue.totalAmount ??= { quality: pickOneElement(Object.values(QualityOptions)) }
  data.revenue.totalAmount.value = 0;
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

  data = generateEuTaxonomyDataForNonFinancials(true, 0);
  generatedDataAndMetaInfo[1].data = data;

  data = generateEuTaxonomyDataForNonFinancials(true, 0);
  data.capex ??= {}
  data.capex.alignedActivities = generateArray(() => euTaxonomyNonFinancialsGenerator.generateAlignedActivity(), 1)
  generatedDataAndMetaInfo[2].data = data;

  return generatedDataAndMetaInfo;
}
