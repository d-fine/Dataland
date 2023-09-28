import fs from "fs";
import {
  type DataAndMetaInformationEuTaxonomyDataForNonFinancials,
  DataMetaInformation, DataTypeEnum, QaStatus,
  QualityOptions
} from "@clients/backend";
import { DataMetaInformationGenerator } from "@e2e/fixtures/data_meta_information/DataMetaInformationFixtures";
import {
  EuNonFinancialsGenerator,
  generateEuTaxonomyDataForNonFinancials,
} from "@e2e/fixtures/eutaxonomy/non-financials/EuTaxonomyDataForNonFinancialsFixtures";
import { generatePercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { DEFAULT_PROBABILITY } from "@e2e/utils/FakeFixtureUtils";
import {generateArray, pickOneElement} from "@e2e/fixtures/FixtureUtils";
import { generateNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateReferencedReports } from "@e2e/fixtures/common/DataPointFixtures";
import {faker} from "@faker-js/faker";

function generateListOfMetaInformationForOneCompany(): DataMetaInformation[] {
  const listOfMetaInfo: DataMetaInformation[] = []
  const metaInfoGenerator = new DataMetaInformationGenerator(true)
  const companyId = faker.string.uuid()
  let yearCounter = 2014;
  function generateActiveMetaInfoWithTypeAndAppend(dataType: DataTypeEnum): void {
    const metaInfo = metaInfoGenerator.generateDataMetaInformation(dataType);
    yearCounter++;
    metaInfo.companyId = companyId;
    metaInfo.reportingPeriod = yearCounter.toString()
    metaInfo.currentlyActive = true;
    metaInfo.qaStatus = QaStatus.Accepted;
    listOfMetaInfo.push(metaInfo)
  }

  range(2).forEach(() => generateActiveMetaInfoWithTypeAndAppend(DataTypeEnum.EutaxonomyFinancials))
  range(4).forEach(() => generateActiveMetaInfoWithTypeAndAppend(DataTypeEnum.EutaxonomyNonFinancials))
  range(2).forEach(() => generateActiveMetaInfoWithTypeAndAppend(DataTypeEnum.Lksg))
  range(1).forEach(() => generateActiveMetaInfoWithTypeAndAppend(DataTypeEnum.Sfdr))

  return listOfMetaInfo;
}

function extractMetaInfoForEuFinancialsAndLksg(listOfMetaInformationForOneCompany: DataMetaInformation[]) {
  const holdingObject: { [key in DataTypeEnum]?: (string | DataMetaInformation)[][] } = {};
  [DataTypeEnum.EutaxonomyFinancials, DataTypeEnum.Lksg].forEach((dataType) => {
    holdingObject[dataType] = listOfMetaInformationForOneCompany.filter((metaInfo) => metaInfo.dataType == dataType).map((metaInfo) => [metaInfo.reportingPeriod, metaInfo]);
  });
  return holdingObject
}

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
  const extractedMetaInformationPerFramework = extractMetaInfoForEuFinancialsAndLksg(listOfMetaInformationForOneCompany);
  fs.writeFileSync(
    "../testing/data/MapsForReportingsPeriodForDifferentDatasetAsArrays.json",
    JSON.stringify(extractedMetaInformationPerFramework, null, "\t"),
  );
}

/**
 * Generates a list of data and meta information with EU taxonomy for non financials data
 * @returns a list of data and meta information with EU taxonomy for non financials data
 */
function generateEuTaxonomyForNonFinancialsMocks(): DataAndMetaInformationEuTaxonomyDataForNonFinancials[] {
  const dataMetaInfoGenerator = new DataMetaInformationGenerator(true);
  const generatedDataAndMetaInfo = range(3).map(
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

function range(numElements: number): number[] {
  return Array.from(Array(numElements).keys())
}