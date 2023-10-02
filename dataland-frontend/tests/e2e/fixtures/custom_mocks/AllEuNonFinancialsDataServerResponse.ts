import {
  type DataAndMetaInformationEuTaxonomyDataForNonFinancials,
  type EuTaxonomyDataForNonFinancials,
  type EuTaxonomyDetailsPerCashFlowType,
} from "@clients/backend";
import { DataMetaInformationGenerator } from "@e2e/fixtures/data_meta_information/DataMetaInformationFixtures";
import { EuNonFinancialsGenerator } from "@e2e/fixtures/eutaxonomy/non-financials/EuTaxonomyDataForNonFinancialsFixtures";
import { generateCurrencyValue, generatePercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { DEFAULT_PROBABILITY } from "@e2e/utils/FakeFixtureUtils";
import { generateNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateDataPoint, generateReferencedReports } from "@e2e/fixtures/common/DataPointFixtures";
import { generateEuTaxonomyWithBaseFields } from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import { generateCurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { generateArray } from "@e2e/fixtures/FixtureUtils";
import { range } from "@/utils/ArrayUtils";

/**
 * This class is a generator for prepared fixtures
 * with minimum changes done for specific tests which require all rows to exist
 */
class MinimumAcceptedEuNonFinancialsGenerator extends EuNonFinancialsGenerator {
  generateMinimumAcceptedEuTaxonomyForNonFinancialsData(): EuTaxonomyDataForNonFinancials {
    return {
      general: generateEuTaxonomyWithBaseFields(this.reports, this.setMissingValuesToNull, 0),
      revenue: this.generateMinimumAcceptedDetailsPerCashFlowType(),
      capex: this.generateMinimumAcceptedDetailsPerCashFlowType(),
      opex: this.generateMinimumAcceptedDetailsPerCashFlowType(),
    };
  }

  generateMinimumAcceptedDetailsPerCashFlowType(): EuTaxonomyDetailsPerCashFlowType {
    return {
      totalAmount: generateDataPoint(
        this.valueOrMissing(generateCurrencyValue()),
        this.reports,
        this.setMissingValuesToNull,
        generateCurrencyCode(),
      ),
      nonEligibleShare: this.generateFinancialShare(),
      eligibleShare: this.generateFinancialShare(),
      nonAlignedShare: this.generateFinancialShare(),
      nonAlignedActivities: generateArray(() => this.generateActivity(), 1, 2),
      alignedShare: this.generateFinancialShare(),
      substantialContributionToClimateChangeMitigationInPercent: generatePercentageValue(),
      substantialContributionToClimateChangeAdaptionInPercent: generatePercentageValue(),
      substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent: generatePercentageValue(),
      substantialContributionToTransitionToACircularEconomyInPercent: generatePercentageValue(),
      substantialContributionToPollutionPreventionAndControlInPercent: generatePercentageValue(),
      substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent: generatePercentageValue(),
      alignedActivities: generateArray(() => this.generateAlignedActivity(), 1, 2),
      enablingShareInPercent: generatePercentageValue(),
      transitionalShareInPercent: generatePercentageValue(),
    };
  }
}

/**
 * Generates a list of data and meta information with EU taxonomy for non financials data
 * @returns a list of data and meta information with EU taxonomy for non financials data
 */
export function generateEuTaxonomyForNonFinancialsMocks(): DataAndMetaInformationEuTaxonomyDataForNonFinancials[] {
  const dataMetaInfoGenerator = new DataMetaInformationGenerator(true);
  const dataGenerator = new MinimumAcceptedEuNonFinancialsGenerator(DEFAULT_PROBABILITY, true);
  const generatedDataAndMetaInfo = range(3).map((index): DataAndMetaInformationEuTaxonomyDataForNonFinancials => {
    const metaInfo = dataMetaInfoGenerator.generateDataMetaInformation();
    metaInfo.reportingPeriod = "202" + (3 - index).toString();
    return {
      metaInfo: metaInfo,
      data: dataGenerator.generateMinimumAcceptedEuTaxonomyForNonFinancialsData(),
    };
  });
  const data = generatedDataAndMetaInfo[0].data;
  data.general!.referencedReports = generateReferencedReports(true, DEFAULT_PROBABILITY, ["IntegratedReport"]);
  data.revenue!.totalAmount!.value = 0;
  data.revenue!.alignedActivities![0].share ??= {};
  data.revenue!.alignedActivities![0].share.relativeShareInPercent = generatePercentageValue();
  data.revenue!.alignedActivities![0].share.absoluteShare ??= dataGenerator.generateAmountWithCurrency();
  data.capex!.nonAlignedActivities![0].naceCodes = generateNaceCodes(1);
  data.capex!.nonAlignedActivities![0].share ??= {};
  data.capex!.nonAlignedActivities![0].share.relativeShareInPercent = generatePercentageValue();
  data.capex!.nonAlignedActivities![0].share.absoluteShare ??= dataGenerator.generateAmountWithCurrency();
  data.capex!.nonAlignedShare!.relativeShareInPercent ??= generatePercentageValue();
  generatedDataAndMetaInfo[0].data = data;
  return generatedDataAndMetaInfo;
}
