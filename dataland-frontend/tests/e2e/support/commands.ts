// add new command to the existing Cypress interface
import Chainable = Cypress.Chainable;
import { getKeycloakToken } from "./utility";

declare global {
  namespace Cypress {
    interface Chainable {
      retrieveDataIdsList: typeof retrieveDataIdsList;
      retrieveCompanyIdsList: typeof retrieveCompanyIdsList;
      login: typeof login;
      restoreLoginSession: typeof restoreLoginSession;
      visitAndCheckAppMount: typeof visitAndCheckAppMount;
      register: typeof register;
      logout: typeof logout;
      verifyTaxonomySearchResultTable: typeof verifyTaxonomySearchResultTable;
      verifyCompanySearchResultTable: typeof verifyCompanySearchResultTable;
      checkViewButtonWorks: typeof checkViewButtonWorks;
      checkViewRowsWorks: typeof checkViewRowsWorks;
      fillCompanyUploadFields: typeof fillCompanyUploadFields;
      logoutDropdown: typeof logoutDropdown;
    }
  }
}

function retrieveIdsList(idKey: string, endpoint: string): Chainable<Array<string>> {
  return getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
    .then((token) => {
      return cy.request({
        url: `${Cypress.env("API")}/${endpoint}`,
        method: "GET",
        headers: { Authorization: "Bearer " + token },
      });
    })
    .then((response) => {
      return response.body.map((e: any) => e[idKey]);
    });
}

export function retrieveDataIdsList(): Chainable<Array<string>> {
  return retrieveIdsList("dataId", "metadata");
}

export function retrieveCompanyIdsList(): Chainable<Array<string>> {
  return retrieveIdsList("companyId", "companies");
}

export function login(
  username: string = "data_reader",
  password: string = Cypress.env("KEYCLOAK_READER_PASSWORD")
): void {
  cy.visit("/")
    .get("button[name='login_dataland_button']")
    .click()
    .get("#username")
    .should("exist")
    .type(username, { force: true })
    .get("#password")
    .should("exist")
    .type(password, { force: true })

    .get("#kc-login")
    .should("exist")
    .click()

    .url()
    .should("eq", Cypress.config("baseUrl") + "/companies");
}

export function register(email: string = "some_user", password: string = "test"): void {
  cy.visit("/")
    .get("button[name='join_dataland_button']")
    .click()
    .get("#email")
    .should("exist")
    .type(email.concat(Date.now().toString()).concat("@dataland.com"), { force: true })

    .get("#password")
    .should("exist")
    .type(password, { force: true })
    .get("#password-confirm")
    .should("exist")
    .type(password, { force: true })

    .get("input[type='submit']")
    .should("exist")
    .click()

    .get("#accept_terms")
    .should("exist")
    .click()
    .get("#accept_privacy")
    .should("exist")
    .click()
    .get("button[name='accept_button']")
    .should("exist")
    .click()

    .url()
    .should("eq", Cypress.config("baseUrl") + "/companies");
}

export function logout(): void {
  cy.visit("/")
    .get("button[name='logout_dataland_button']")
    .click()
    .get("button[name='login_dataland_button']")
    .should("exist")
    .should("be.visible");
}

export function logoutDropdown(): void {
  cy.visit("/companies")
    .get("div[id='profile-picture-dropdown-toggle']")
    .click()
    .get("a[id='profile-picture-dropdown-toggle']")
    .click()
    .url()
    .should("eq", Cypress.config("baseUrl") + "/")
    .get("button[name='login_dataland_button']")
    .should("exist")
    .should("be.visible");
}

export function restoreLoginSession(username?: string, password?: string): void {
  cy.session(
    [username, password],
    () => {
      login(username, password);
    },
    {
      validate: () => {
        visitAndCheckAppMount("/").get("button[name='logout_dataland_button']").should("exist");
      },
    }
  );
}

export function visitAndCheckAppMount(endpoint: string): Chainable<JQuery> {
  return cy.visit(endpoint).get("#app").should("exist");
}

export function fillCompanyUploadFields(companyName: string): void {
  cy.get("input[name=companyName]").type(companyName, { force: true });
  cy.get("input[name=headquarters]").type("Capitol City", { force: true });
  cy.get("input[name=sector]").type("Handmade", { force: true });
  cy.get("input[name=marketCap]").type("123", { force: true });
  cy.get("input[name=countryCode]").type("DE", { force: true });
  cy.get("input[name=reportingDateOfMarketCap]").type("2021-09-02", { force: true });
  cy.get("select[name=identifierType]").select("ISIN");
  cy.get("input[name=identifierValue]").type("IsinValueId", { force: true });
}

export function verifyTaxonomySearchResultTable(): void {
  cy.get("table.p-datatable-table").contains("th", "COMPANY");
  cy.get("table.p-datatable-table").contains("th", "PERM ID");
  cy.get("table.p-datatable-table").contains("th", "SECTOR");
  cy.get("table.p-datatable-table").contains("th", "MARKET CAP");
  cy.get("table.p-datatable-table").contains("th", "LOCATION");
}

export function verifyCompanySearchResultTable(): void {
  cy.get("table.p-datatable-table").contains("th", "COMPANY");
  cy.get("table.p-datatable-table").contains("th", "SECTOR");
  cy.get("table.p-datatable-table").contains("th", "MARKET CAP");
}

export function checkViewButtonWorks(): void {
  cy.get("table.p-datatable-table")
    .contains("td", "VIEW")
    .contains("a", "VIEW")
    .click()
    .url()
    .should("include", "/companies/")
    .url()
    .should("include", "/frameworks/eutaxonomy");
}

export function checkViewRowsWorks(): void {
  cy.get("table.p-datatable-table");
  cy.contains("td", "VIEW")
    .siblings()
    .contains("€")
    .click()
    .url()
    .should("include", "/companies/")
    .url()
    .should("include", "/frameworks/eutaxonomy");
}

Cypress.Commands.add("retrieveDataIdsList", retrieveDataIdsList);
Cypress.Commands.add("retrieveCompanyIdsList", retrieveCompanyIdsList);
Cypress.Commands.add("login", login);
Cypress.Commands.add("restoreLoginSession", restoreLoginSession);
Cypress.Commands.add("visitAndCheckAppMount", visitAndCheckAppMount);
Cypress.Commands.add("register", register);
Cypress.Commands.add("logout", logout);
Cypress.Commands.add("verifyTaxonomySearchResultTable", verifyTaxonomySearchResultTable);
Cypress.Commands.add("verifyCompanySearchResultTable", verifyCompanySearchResultTable);
Cypress.Commands.add("checkViewButtonWorks", checkViewButtonWorks);
Cypress.Commands.add("checkViewRowsWorks", checkViewRowsWorks);
Cypress.Commands.add("logoutDropdown", logoutDropdown);
Cypress.Commands.add("fillCompanyUploadFields", fillCompanyUploadFields);
