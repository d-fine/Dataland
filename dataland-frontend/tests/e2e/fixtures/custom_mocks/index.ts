import fs from "fs";
import {
  type DataAndMetaInformationEuTaxonomyDataForNonFinancials,
  type DataMetaInformation,
  DataTypeEnum, EuTaxonomyDataForNonFinancials, EuTaxonomyDetailsPerCashFlowType,
  QaStatus,
} from "@clients/backend";
import { DataMetaInformationGenerator } from "@e2e/fixtures/data_meta_information/DataMetaInformationFixtures";
import {
  EuNonFinancialsGenerator,
} from "@e2e/fixtures/eutaxonomy/non-financials/EuTaxonomyDataForNonFinancialsFixtures";
import {generateCurrencyValue, generatePercentageValue} from "@e2e/fixtures/common/NumberFixtures";
import {DEFAULT_PROBABILITY} from "@e2e/utils/FakeFixtureUtils";
import { generateNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateDataPoint, generateReferencedReports } from "@e2e/fixtures/common/DataPointFixtures";
import { faker } from "@faker-js/faker";
import {generateEuTaxonomyWithBaseFields} from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import {generateCurrencyCode} from "@e2e/fixtures/common/CurrencyFixtures";
import {generateArray} from "@e2e/fixtures/FixtureUtils";

/**
 * Generates and exports fake fixtures for the LKSG framework
 */
export function exportCustomMocks(): void {
  fs.writeFileSync(
    "../testing/data/EuTaxonomyForNonFinancialsMocks.json",
    JSON.stringify(generateEuTaxonomyForNonFinancialsMocks(), null, "\t"),
  );
  const listOfMetaInformationForOneCompany = generateListOfMetaInformationForOneCompany();
  fs.writeFileSync(
    "../testing/data/MetaInfoDataForCompany.json",
    JSON.stringify(listOfMetaInformationForOneCompany, null, "\t"),
  );
  const extractedMetaInformationPerFramework = extractMetaInfoForEuFinancialsAndLksg(
    listOfMetaInformationForOneCompany,
  );
  fs.writeFileSync(
    "../testing/data/MapsForReportingsPeriodForDifferentDatasetAsArrays.json",
    JSON.stringify(extractedMetaInformationPerFramework, null, "\t"),
  );
}

class MinimumAcceptedEuNonFinancialsGenerator extends EuNonFinancialsGenerator {
  generateMinimumAcceptedEuTaxonomyForNonFinancialsData(): EuTaxonomyDataForNonFinancials {
    return {
      general: generateEuTaxonomyWithBaseFields(this.reports, this.setMissingValuesToNull, 0),
      revenue: this.generateMinimumAcceptedDetailsPerCashFlowType(),
      capex: this.generateMinimumAcceptedDetailsPerCashFlowType(),
      opex: this.generateMinimumAcceptedDetailsPerCashFlowType(),
    }
  }

  generateMinimumAcceptedDetailsPerCashFlowType(): EuTaxonomyDetailsPerCashFlowType {
    return {
      totalAmount: generateDataPoint(this.valueOrMissing(generateCurrencyValue()), this.reports, this.setMissingValuesToNull, generateCurrencyCode()),
      nonEligibleShare: this.generateFinancialShare(),
      eligibleShare: this.generateFinancialShare(),
      nonAlignedShare: this.generateFinancialShare(),
      nonAlignedActivities: generateArray(() => this.generateActivity(), 1, 2),
      alignedShare: this.generateFinancialShare(),
      substantialContributionToClimateChangeMitigationInPercent: generatePercentageValue(),
      substantialContributionToClimateChangeAdaptionInPercent: generatePercentageValue(),
      substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent:
        generatePercentageValue(),
      substantialContributionToTransitionToACircularEconomyInPercent: generatePercentageValue(),
      substantialContributionToPollutionPreventionAndControlInPercent: generatePercentageValue(),
      substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent:
        generatePercentageValue(),
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
function generateEuTaxonomyForNonFinancialsMocks(): DataAndMetaInformationEuTaxonomyDataForNonFinancials[] {
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
  let data = generatedDataAndMetaInfo[0].data;
  data.general!.referencedReports = generateReferencedReports(DEFAULT_PROBABILITY, true, ["IntegratedReport"]);
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

/**
 * Generates a list of data meta information for some data types
 * @returns a lost of data meta information
 */
function generateListOfMetaInformationForOneCompany(): DataMetaInformation[] {
  const listOfMetaInfo: DataMetaInformation[] = [];
  const metaInfoGenerator = new DataMetaInformationGenerator(true);
  const companyId = faker.string.uuid();
  let yearCounter = 2014;

  /**
   * Generates data meta information for active and by QA accepted data and adds it to the collecting list
   * @param dataType the data type for the meta information
   */
  function generateActiveMetaInfoWithTypeAndAppend(dataType: DataTypeEnum): void {
    const metaInfo = metaInfoGenerator.generateDataMetaInformation(dataType);
    yearCounter++;
    metaInfo.companyId = companyId;
    metaInfo.reportingPeriod = yearCounter.toString();
    metaInfo.currentlyActive = true;
    metaInfo.qaStatus = QaStatus.Accepted;
    listOfMetaInfo.push(metaInfo);
  }

  range(2).forEach(() => generateActiveMetaInfoWithTypeAndAppend(DataTypeEnum.EutaxonomyFinancials));
  range(4).forEach(() => generateActiveMetaInfoWithTypeAndAppend(DataTypeEnum.EutaxonomyNonFinancials));
  range(2).forEach(() => generateActiveMetaInfoWithTypeAndAppend(DataTypeEnum.Lksg));
  range(1).forEach(() => generateActiveMetaInfoWithTypeAndAppend(DataTypeEnum.Sfdr));

  return listOfMetaInfo;
}

type MetaInfoAssociatedWithReportingPeriodByFranework = { [key in DataTypeEnum]?: (string | DataMetaInformation)[][] }

/**
 * Extracts data meta information with data type "EU taxonomy for financials" and "LkSG" and stores them in a custom format
 * @param listOfMetaInformationForOneCompany the list of data meta information to parse
 * @returns the generated structure
 */
function extractMetaInfoForEuFinancialsAndLksg(
  listOfMetaInformationForOneCompany: DataMetaInformation[],
): MetaInfoAssociatedWithReportingPeriodByFranework {
  const holdingObject: MetaInfoAssociatedWithReportingPeriodByFranework = {};
  [DataTypeEnum.EutaxonomyFinancials, DataTypeEnum.Lksg].forEach((dataType) => {
    holdingObject[dataType] = listOfMetaInformationForOneCompany
      .filter((metaInfo) => metaInfo.dataType == dataType)
      .map((metaInfo) => [metaInfo.reportingPeriod, metaInfo]);
  });
  return holdingObject;
}

/**
 * Generates an array of numbers from 0 to [numElements]-1
 * @param numElements the number of elements the array should hold
 * @returns the generated array
 */
function range(numElements: number): number[] {
  return Array.from(Array(numElements).keys());
}
