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
 * The dummy value for the sector may be overwritten
 *
 * @param companyName the company name to fill into the form
 * @param sector overrides the dummy sector if specified
 */
export function fillCompanyUploadFields(companyName: string, sector?: string): void {
  cy.get("input[name=companyName]").type(companyName, { force: true });
  cy.get("input[name=headquarters]").type("Capitol City", { force: true });
  cy.get("input[name=sector]").type(sector ?? "Handmade", { force: true });
  cy.get("input[name=countryCode]").type("DE", { force: true });
  cy.get("select[name=identifierType]").select("ISIN");
  cy.get("input[name=identifierValue]").type(`IsinValueId:${crypto.randomUUID()}`, { force: true });
  cy.get("button[name=addAlternativeName]").click();
  cy.get("input[name=0]").type(`Another Name`, { force: true });
}

/**
 * Creates a company with the provided name and dummy values via the frontend.
 * The dummy value for the sector may be overwritten. Retrieves the Id of the newly created company
 *
 * @param companyName the name of the company to create
 * @param sector overrides the dummy sector if specified
 * @returns a cypress chainable containing the id of the newly created company
 */
export function uploadCompanyViaFormAndGetId(companyName: string, sector?: string): Cypress.Chainable<string> {
  cy.visitAndCheckAppMount("/companies/upload");
  cy.get('button[name="postCompanyData"]').should("be.disabled");
  fillCompanyUploadFields(companyName, sector);
  cy.intercept("**/api/companies").as("postCompany");
  cy.get('button[name="postCompanyData"]').click();
  return cy
    .wait("@postCompany")
    .get("body")
    .should("contain", "success")
    .get("span[title=companyId]")
    .then<string>(($companyID): string => {
      return $companyID.text();
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
