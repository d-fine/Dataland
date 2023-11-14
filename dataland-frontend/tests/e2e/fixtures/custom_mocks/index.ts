import fs from "fs";
import { generateMetaInfoAssociatedWithReportingPeriodByDataType } from "@e2e/fixtures/custom_mocks/CustomMetaDataFormatFixtures";
import { generateMetaInfoDataForOneCompany } from "@e2e/fixtures/custom_mocks/DataMetaInformationFaker";
import { generateEuTaxonomyForNonFinancials } from "@e2e/fixtures/custom_mocks/EuTaxonomyDataForNonFinancialsFaker";
import { generateMapOfFrameworkNameToAggregatedFrameworkDataSummary } from "@e2e/fixtures/custom_mocks/MapOfDataTypeToAggregatedFrameworkDataSummaryFaker";
import { generateListOfDataSearchStoredCompany } from "@e2e/fixtures/custom_mocks/DataSearchStoredCompanyFaker";

/**
 * Generates mocks that are not only dataset mocks
 */
export function exportCustomMocks(): void {
  fs.writeFileSync(
    "../testing/data/EuTaxonomyForNonFinancialsMocks.json",
    JSON.stringify(generateEuTaxonomyForNonFinancials(), null, "\t"),
  );
  fs.writeFileSync(
    "../testing/data/MetaInfoDataMocksForOneCompany.json",
    JSON.stringify(generateMetaInfoDataForOneCompany(), null, "\t"),
  );
  fs.writeFileSync(
    "../testing/data/MetaInfoAssociatedWithReportingPeriodByDataTypeMock.json",
    JSON.stringify(generateMetaInfoAssociatedWithReportingPeriodByDataType(), null, "\t"),
  );
  fs.writeFileSync(
    "../testing/data/MapOfFrameworkNameToAggregatedFrameworkDataSummaryMock.json",
    JSON.stringify(generateMapOfFrameworkNameToAggregatedFrameworkDataSummary(), null, "\t"),
  );
  fs.writeFileSync(
    "../testing/data/DataSearchStoredCompanyMocks.json",
    JSON.stringify(generateListOfDataSearchStoredCompany(), null, "\t"),
  );
}
