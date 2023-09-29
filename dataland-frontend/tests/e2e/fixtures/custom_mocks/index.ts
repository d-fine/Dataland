import fs from "fs";
import { extractMetaInfoForEuFinancialsAndLksg } from "@e2e/fixtures/custom_mocks/CustomMetaDataFormatFixtures";
import { generateListOfMetaInformationForOneCompany } from "@e2e/fixtures/custom_mocks/ListOfMetaInfoFixtures";
import { generateEuTaxonomyForNonFinancialsMocks } from "@e2e/fixtures/custom_mocks/AllEuNonFinancialsDataServerResponse";

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
