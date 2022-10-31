import {
  Configuration,
  EuTaxonomyDataForFinancials,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForNonFinancialsControllerApi,
} from "../../../build/clients/backend";
import { FixtureData } from "../fixtures/FixtureUtils";
import Chainable = Cypress.Chainable;

export function fillEuTaxonomyNonFinancialsDummyUploadFields(): void {
  cy.get("select[name=assurance]").select("Limited Assurance");
  cy.get('input[id="reportingObligation-option-yes"][value=Yes]').check({
    force: true,
  });
  for (const argument of ["capex", "opex", "revenue"]) {
    cy.get(`div[title=${argument}] input[name=eligiblePercentage]`).type("0.657");
    cy.get(`div[title=${argument}] input[name=totalAmount]`).type("120000000");
  }
}

export function uploadDummyEuTaxonomyDataForNonFinancials(companyId: string): Cypress.Chainable<string> {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-non-financials/upload`);
  fillEuTaxonomyNonFinancialsDummyUploadFields();
  cy.intercept("**/api/data/eutaxonomy-non-financials").as("postCompanyAssociatedData");
  cy.get('button[name="postEUData"]').click();
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
): Promise<void> {
  await new EuTaxonomyDataForNonFinancialsControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedEuTaxonomyDataForNonFinancials({
    companyId,
    data,
  });
}
