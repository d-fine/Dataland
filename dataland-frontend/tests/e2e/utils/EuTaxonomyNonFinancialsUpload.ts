import {
  Configuration,
  DataMetaInformation,
  EuTaxonomyDataForFinancials,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForNonFinancialsControllerApi,
} from "@clients/backend";
import { FixtureData } from "../fixtures/FixtureUtils";
import Chainable = Cypress.Chainable;

export function uploadEuTaxonomyDataForNonFinancialsViaForm(companyId: string): Cypress.Chainable<string> {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-non-financials/upload`);
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
  cy.intercept("**/api/data/eutaxonomy-non-financials").as("postCompanyAssociatedData");
  cy.get('button[name="postEUData"]').click({ force: true });
  return cy
    .wait("@postCompanyAssociatedData")
    .get("body")
    .should("contain", "success")
    .get("span[title=dataId]")
    .then<string>(($dataId): string => {
      return $dataId.text();
    });
}

export function getFirstEuTaxonomyNonFinancialsDatasetFromFixtures(): Chainable<EuTaxonomyDataForNonFinancials> {
  return cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (jsonContent) {
    const companiesWithEuTaxonomyDataForNonFinancials = jsonContent as Array<
      FixtureData<EuTaxonomyDataForNonFinancials>
    >;
    return companiesWithEuTaxonomyDataForNonFinancials[0].t;
  });
}

export async function uploadOneEuTaxonomyNonFinancialsDatasetViaApi(
  token: string,
  companyId: string,
  data?: EuTaxonomyDataForFinancials
): Promise<DataMetaInformation> {
  const dataMetaInformation = await new EuTaxonomyDataForNonFinancialsControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedEuTaxonomyDataForNonFinancials({
    companyId,
    data,
  });
  return dataMetaInformation.data;
}
