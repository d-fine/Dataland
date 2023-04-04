import {
  CompanyDataControllerApi,
  CompanyIdentifierIdentifierTypeEnum,
  CompanyInformation,
  Configuration,
  StoredCompany,
} from "@clients/backend";
import { faker } from "@faker-js/faker";

/**
 * Fills the company for a company with the specified name with dummy values.
 *
 * @param companyName the company name to fill into the form
 */
export function fillCompanyUploadFields(companyName: string): void {
  cy.get("input[name=companyName]").type(companyName, { force: true });
  cy.get("input[name=alternativeName]").type("Name to remove", { force: true });
  cy.get("button[name=addAlternativeName]").click({ force: true });
  cy.get(`span.form-list-item em`).click();
  cy.get("span.form-list-item").should("not.exist");
  cy.get("input[name=alternativeName]").type("Another Name", { force: true });
  cy.get("button[name=addAlternativeName]").click({ force: true });
  cy.get("input[name=headquarters]").type("Capitol City", { force: true });
  cy.get("select[name=countryCode]").select("DE", { force: true });
  cy.get("input[name=headquartersPostalCode]").type("123456", { force: true });
  cy.get("input[name=companyLegalForm]").type("Enterprise Ltd.", { force: true });
  cy.get("input[name=website]").type("www.company.com", { force: true });
  cy.get("input[name=lei]").type(`LeiValueId:${crypto.randomUUID()}`, { force: true });
  cy.get("input[name=isin]").type(`IsinValueId:${crypto.randomUUID()}`, { force: true });
  cy.get("input[name=ticker]").type(`TickerValueId:${crypto.randomUUID()}`, { force: true });
  cy.get("input[name=permId]").type(`PermValueId:${crypto.randomUUID()}`, { force: true });
  cy.get("input[name=duns]").type(`DunsValueId:${crypto.randomUUID()}`, { force: true });
  cy.get("input[name=companyRegistrationNumber]").type(`RegValueId:${crypto.randomUUID()}`, { force: true });
  cy.get("select[name=sector]").select("Energy");
}

/**
 * Creates a company with the provided name and dummy values via the frontend and returns the company meta information of the newly created company.
 *
 * @param companyName the name of the company to create
 * @returns a cypress chainable containing the company meta information of the newly created company
 */
export function uploadCompanyViaForm(companyName: string): Cypress.Chainable<StoredCompany> {
  Cypress.Keyboard.defaults({
    keystrokeDelay: 0,
  });
  fillCompanyUploadFields(companyName);
  cy.intercept("**/api/companies").as("postCompany");
  cy.get('button[name="addCompany"]').click();
  return cy.wait("@postCompany").then((interception) => {
    return interception.response!.body as StoredCompany;
  });
}

/**
 * Generates dummy values for a company of the specified name. The dummy value for the sector may be overwritten.
 *
 * @param companyName the name of the company
 * @param sector overrides the dummy sector if specified
 * @returns a CompanyInformation object that can be sent to the API to create a company
 */
export function generateDummyCompanyInformation(companyName: string, sector = "Imaginary-Sector"): CompanyInformation {
  return {
    companyName: companyName,
    headquarters: "Imaginary-City",
    sector: sector,
    identifiers: [
      { identifierType: CompanyIdentifierIdentifierTypeEnum.PermId, identifierValue: faker.random.alphaNumeric(10) },
    ],
    countryCode: "DE",
    isTeaserCompany: false,
  };
}

/**
 * Uses the Dataland API to create a new company with the provided CompanyInformation
 *
 * @param token the bearer token used to authorize the API requests
 * @param companyInformation information about the company to create
 */
export async function uploadCompanyViaApi(
  token: string,
  companyInformation: CompanyInformation
): Promise<StoredCompany> {
  const data = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).postCompany(
    companyInformation
  );
  return data.data;
}
