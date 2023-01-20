import { login, logout } from "@e2e/utils/Auth";
import { authenticator } from "otplib";

describe("As a user I want to be able to register for an account and be able to log in and out of that account", () => {
  const email = `test_user${Date.now()}@dataland.com`;
  const passwordBytes = crypto.getRandomValues(new Uint32Array(8));
  const randomHexPassword = [...passwordBytes].map((x): string => x.toString(16).padStart(2, "0")).join("");

  it("Checks that the Dataland password-policy gets respected", () => {
    cy.visitAndCheckAppMount("/")
      .get("button[name='join_dataland_button']")
      .click()
      .get("#email")
      .should("exist")
      .type(email, { force: true });

    const typePasswordAndExpectError = (password: string, errorMessageSubstring: string): void => {
      cy.get("#password")
        .should("exist")
        .clear()
        .type(password)
        .get("input[type='submit']")
        .should("exist")
        .click()
        .get("div[data-role=password-primary] span.input-error")
        .should("be.visible")
        .should("contain.text", errorMessageSubstring);
    };

    typePasswordAndExpectError("abc", "at least 12 characters");
    typePasswordAndExpectError(
      "PasswordPasswordPassword",
      'Repeated character patterns like "abcabcabc" are easy to guess'
    );
    typePasswordAndExpectError("qwerty123456", "This is a commonly used password");
    typePasswordAndExpectError("a".repeat(200), "at most 128 characters");
  });

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

  it("Checks that the admin console is working and a newly registered user can be verified", () => {
    cy.task("getEmail").then((returnEmail) => {
      cy.visit("http://dataland-admin:6789/keycloak/admin/master/console/#/datalandsecurity/users");
      cy.get("h1").should("exist").should("contain", "Sign in to your account");
      cy.url().should("contain", "realms/master");
      cy.get("#username")
        .should("exist")
        .type(Cypress.env("KEYCLOAK_ADMIN") as string, { force: true })
        .get("#password")
        .should("exist")
        .type(Cypress.env("KEYCLOAK_ADMIN_PASSWORD") as string, { force: true })
        .get("#kc-login")
        .should("exist")
        .click();
      cy.get("input")
        .should("have.class", "pf-c-text-input-group__text-input")
        .type(returnEmail as string, { force: true })
        .type("{enter}");
      cy.get("table");
      cy.contains("a", returnEmail as string).click();
      cy.get('input[id="kc-user-email-verified"]').click({ force: true });
      cy.get('button[data-testid="save-user"]').click({ force: true });
    });
  });
  it("Checks that one can login to the newly registered account", () => {
    cy.visit("/");
    cy.task("getEmail").then((returnEmail) => {
      cy.task("getPassword").then((returnPassword) => {
        login(returnEmail as string, returnPassword as string);
      });
    });
    logout();
  });

  describe("Checks that TOTP-Based 2FA works", () => {
    it("Should be possible to setup 2FA on the newly created account", () => {
      cy.task("getEmail").then((returnEmail) => {
        cy.task("getPassword").then((returnPassword) => {
          const username = returnEmail as string;
          const password = returnPassword as string;
          login(username, password);
          cy.visitAndCheckAppMount("/companies")
            .get("div[id='profile-picture-dropdown-toggle']")
            .click()
            .get("a[id='profile-picture-dropdown-settings-button']")
            .click()
            .get("div[id='landing-signingin'] > a")
            .should("be.visible", { timeout: Cypress.env("medium_timeout_in_ms") })
            .click()
            .get("button:contains('Set up authenticator application')")
            .should("be.visible", { timeout: Cypress.env("medium_timeout_in_ms") })
            .click()
            .get("a:contains('Unable to scan')")
            .should("be.visible", { timeout: Cypress.env("short_timeout_in_ms") })
            .click()
            .get("span[id='kc-totp-secret-key']")
            .should("be.visible", { timeout: Cypress.env("short_timeout_in_ms") })
            .invoke("text")
            .then((text) => {
              const totpKey = text.replace(/\s/g, "");
              cy.get("input[id='totp']")
                .type(authenticator.generate(totpKey))
                .get("input[id='saveTOTPBtn']")
                .click()
                .get("button[id='signOutButton']")
                .should("be.visible", { timeout: Cypress.env("medium_timeout_in_ms") });

              cy.task("setTotpKey", totpKey);
            });
        });
      });
    });
    it("Should be possible to login to the account with 2FA enabled", () => {
      cy.task("getEmail").then((returnEmail) => {
        cy.task("getPassword").then((returnPassword) => {
          cy.task("getTotpKey").then((key) => {
            const username = returnEmail as string;
            const password = returnPassword as string;
            const totpKey = key as string;

            login(username, password, () => {
              return authenticator.generate(totpKey);
            });
          });
        });
      });
    });
  });
});
