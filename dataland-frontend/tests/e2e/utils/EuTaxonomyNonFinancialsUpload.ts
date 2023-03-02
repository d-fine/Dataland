import {
  Configuration,
  DataMetaInformation,
  DataTypeEnum,
  EuTaxonomyDataForFinancials,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForNonFinancialsControllerApi,
} from "@clients/backend";
import { FixtureData } from "../fixtures/FixtureUtils";
import Chainable = Cypress.Chainable;

/**
 * Uploads a single eutaxonomy-non-financials data entry for a company via the Dataland upload form
 *
 * @param companyId The Id of the company to upload the dataset for
 * @returns the id of the dataset that has been uploaded
 */
export function uploadEuTaxonomyDataForNonFinancialsViaForm(companyId: string): Cypress.Chainable<string> {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`);
  cy.get(`input[name="reportingPeriod"]`).type("2023");
  cy.get("select[name=assurance]").select("Limited Assurance");
  cy.get('input[id="reportingObligation-option-yes"][value=Yes]').check({
    force: true,
  });
  for (const argument of ["capex", "opex"]) {
    cy.get(`div[title=${argument}] input`).each(($element, index) => {
      const inputNumber = 10 * index + 7;
      cy.wrap($element).type(inputNumber.toString(), { force: true });
    });
  }
  cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}`).as("postCompanyAssociatedData");
  cy.get('button[name="postEUData"]').click({ force: true });
  cy.wait("@postCompanyAssociatedData");
  return cy.contains("h4", "dataId").then<string>(($dataId): string => {
    return $dataId.text();
  });
}

/**
 * Extracts the first eutaxonomy-non-financials dataset from the fake fixtures
 *
 * @returns the first eutaxonomy-non-financials dataset from the fake fixtures
 */
export function getFirstEuTaxonomyNonFinancialsFixtureDataFromFixtures(): Chainable<
  FixtureData<EuTaxonomyDataForNonFinancials>
> {
  return cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (jsonContent) {
    const companiesWithEuTaxonomyDataForNonFinancials = jsonContent as Array<
      FixtureData<EuTaxonomyDataForNonFinancials>
    >;
    return companiesWithEuTaxonomyDataForNonFinancials[0];
  });
}

/**
 * Uploads a single eutaxonomy-non-financials data entry for a company via the Dataland API
 *
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param reportingPeriod The reporting period to use for the upload
 * @param data The Dataset to upload
 */
export async function uploadOneEuTaxonomyNonFinancialsDatasetViaApi(
  token: string,
  companyId: string,
  reportingPeriod: string,
  data: EuTaxonomyDataForFinancials
): Promise<DataMetaInformation> {
  const dataMetaInformation = await new EuTaxonomyDataForNonFinancialsControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedEuTaxonomyDataForNonFinancials({
    companyId,
    reportingPeriod,
    data,
  });
  return dataMetaInformation.data;
}
