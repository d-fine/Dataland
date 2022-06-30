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
      register: typeof register;
      logout: typeof logout;
      verifyResultTable: typeof verifyResultTable;
      checkViewButtonWorks: typeof checkViewButtonWorks;
    }
  }
}

function retrieveIdsList(
  idKey: string,
  endpoint: string
): Chainable<Array<string>> {
  return getKeycloakToken(
    "data_uploader",
    Cypress.env("KEYCLOAK_UPLOADER_PASSWORD")
  )
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
): Chainable<JQuery> {
  return cy
    .visit("/")
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

    .get("button[name='logout_dataland_button']")
    .should("exist")
    .should("be.visible");
}

export function register(
  email: string = "some_user",
  password: string = "test"
): Chainable<JQuery> {
  return cy
    .visit("/")
    .get("button[name='join_dataland_button']")
    .click()
    .get("#firstName")
    .should("exist")
    .type("data", { force: true })

    .get("#lastName")
    .should("exist")
    .type("land", { force: true })

    .get("#email")
    .should("exist")
    .type(email.concat(Date.now().toString()).concat("@dataland.com"), {
      force: true,
    })

    .get("#password")
    .should("exist")
    .type(password, { force: true })
    .get("#password-confirm")
    .should("exist")
    .type(password, { force: true })

    .get("#kc-form-buttons")
    .should("exist")
    .click()

    .get("button[name='logout_dataland_button']")
    .should("exist")
    .should("be.visible");
}

export function logout(): Chainable<JQuery> {
  return cy
    .visit("/")
    .get("button[name='logout_dataland_button']")
    .click()
    .get("button[name='login_dataland_button']")
    .should("exist")
    .should("be.visible");
}

export function restoreLoginSession(
  username?: string,
  password?: string
): Chainable<null> {
  return cy.session(
    [username, password],
    () => {
      login(username, password);
    },
    {
      validate: () => {
        cy.visit("/")
          .get("button[name='logout_dataland_button']")
          .should("exist");
      },
    }
  );
}

export function verifyResultTable() {
  return cy
    .get("table.p-datatable-table")
    .contains("th", "COMPANY")
    .contains("th", "PERM ID")
    .contains("th", "SECTOR")
    .contains("th", "MARKET CAP")
    .contains("th", "LOCATION");
}

export function checkViewButtonWorks() {
  return cy
    .get("table.p-datatable-table")
    .contains("td", "VIEW")
    .contains("a", "VIEW")
    .click()
    .url()
    .should("include", "/companies/")
    .url()
    .should("include", "/eutaxonomies");
}

Cypress.Commands.add("retrieveDataIdsList", retrieveDataIdsList);
Cypress.Commands.add("retrieveCompanyIdsList", retrieveCompanyIdsList);
Cypress.Commands.add("login", login);
Cypress.Commands.add("restoreLoginSession", restoreLoginSession);
Cypress.Commands.add("register", register);
Cypress.Commands.add("logout", logout);
Cypress.Commands.add("verifyResultTable", verifyResultTable);
Cypress.Commands.add("checkViewButtonWorks", checkViewButtonWorks);
