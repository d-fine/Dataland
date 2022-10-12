import { login, logout } from "@e2e/utils/Auth";

describe("As a user I want to be able to register for an account and be able to log in and out of that account", () => {
  const email = `test_user${Date.now()}@dataland.com`;
  const passwordBytes = crypto.getRandomValues(new Uint32Array(32));
  const randomHexPassword = [...passwordBytes].map((x) => x.toString(16).padStart(2, "0")).join("");

  it("Checks that registering works", () => {
    cy.task("setEmail", email);
    cy.task("setPassword", randomHexPassword);
    cy.visitAndCheckAppMount("/")
      .get("button[name='join_dataland_button']")
      .click()
      .get("#email")
      .should("exist")
      .type(email, { force: true })

      .get("#password")
      .should("exist")
      .type(randomHexPassword, { force: true })
      .get("#password-confirm")
      .should("exist")
      .type(randomHexPassword, { force: true })

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

      .get("h1")
      .should("contain", "Email verification");
  });

  it("test", () => {
    function verifyRegisteredUser(inputemail: any): void {
      console.log(email);
      cy.visit("http://dataland-admin:6789/keycloak/admin/master/console/#/datalandsecurity/users");
      cy.get("h1").should("exist").should("contain", "Sign in to your account");
      cy.url().should("contain", "realms/master");
      cy.get("#username")
        .should("exist")
        .type(Cypress.env("KEYCLOAK_ADMIN"), { force: true })
        .get("#password")
        .should("exist")
        .type(Cypress.env("KEYCLOAK_ADMIN_PASSWORD"), { force: true })
        .get("#kc-login")
        .should("exist")
        .click();
      cy.get("input")
        .should("have.class", "pf-c-text-input-group__text-input")
        .type(inputemail, { force: true })
        .type("{enter}");
      cy.get("table");
      cy.contains("td", inputemail).click();
      cy.get('input[id="kc-user-email-verified"]').click({ force: true });
      cy.get('button[data-testid="save-user"]').click({ force: true });
    }
    cy.task("getEmail").then((returnemail) => {
      verifyRegisteredUser(returnemail);
    });
  });
  it("Checks that one can login to the newly registered account", () => {
    cy.visit("/");
    cy.task("getEmail").then((returnemail) => {
      cy.task("getPassword").then((returnpassword) => {
        login(returnemail as string, returnpassword as string);
      });
    });
    logout();
  });
});
