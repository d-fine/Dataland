import {
  CompanyDataControllerApi,
  CompanyIdentifierIdentifierTypeEnum,
  CompanyInformation,
  Configuration,
  StoredCompany,
} from "@clients/backend";
import { faker } from "@faker-js/faker";

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

export async function uploadCompanyViaApi(
  token: string,
  companyInformation: CompanyInformation
): Promise<StoredCompany> {
  const data = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).postCompany(
    companyInformation
  );
  return data.data;
}
