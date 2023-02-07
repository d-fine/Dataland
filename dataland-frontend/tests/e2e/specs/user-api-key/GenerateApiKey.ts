import {ApiKeyAndMetaInfo} from "@clients/apikeymanager";

describe("As a user I expect my api key will be generated correctly", () => {
  /**
   * Verifies that the initial state of the api key page is as expected and that the "Create Api Key" popup works
   * as expected
   */
  function verifyInitialPageStateAndCreateApiKeyCardPopup(): void {
    cy.get("[data-test='noApiKeyWelcomeComponent']").should("exist").should("contain.text", "You have no API Key!");
    cy.get("[data-test='noApiKeyWelcomeComponent']")
      .find("button")
      .should("contain.text", "CREATE NEW API KEY")
      .click();
    cy.get('[data-test="CreateApiKeyCard"]').should("exist");
    cy.get("h1").should("contain.text", "Create new API Key");
    cy.get('[data-test="cancelGenerateApiKey"]').click();
    cy.get("h1").should("contain.text", "API");
    cy.get('[data-test="CreateApiKeyCard"]').should("not.exist");
  }

  /**
   * Verifies that setting the expiration date via the dropdown options works as expected
   */
  function verifyExpirationDropdownOptions(): void {
    cy.get("div.middle-center-div button").contains("CREATE NEW API KEY").click();
    cy.get("button#generateApiKey").click();
    cy.get('label[for="expiryTime"]').should("contain.text", `Please select expiration date`);
    cy.get("div#expiryTime").click();
    cy.get('ul[role="listbox"]').find('[aria-label="Custom..."]').click();
    cy.get('label[for="expiryTime"]').should("not.contain.text", `Please select expiration date`);
    cy.get("button#generateApiKey").click();
    cy.get('label[for="expiryTime"]').should("contain.text", `Please select expiration date`);

    cy.get("div#expiryTime").click();
    cy.get('ul[role="listbox"]').find('[aria-label="7 days"]').click();
    cy.get("#expiryTimeWrapper").should("contain.text", `The API Key will expire on`);
    cy.get("div#expiryTime").click();
    cy.get('ul[role="listbox"]').find('[aria-label="Custom..."]').click({ force: true });
    cy.get("#expiryTimeWrapper").should("not.exist");
    cy.get('[data-test="expiryDatePicker"]').should("be.visible");
    cy.get("button.p-datepicker-trigger").click();
    cy.get("div.p-datepicker").find('button[aria-label="Next Month"]').click();
    cy.get("div.p-datepicker").find('span:contains("13")').click();
    cy.get('[data-test="expiryDatePicker"]')
      .find("input")
      .should(($input) => {
        const val = $input.val();
        expect(val).to.include("13");
      });
    cy.get('[data-test="cancelGenerateApiKey"]').click();
  }

  /**
   * Verifies that creating an api key works as expected, and also assures that the copy-to-clipboard button works if
   * the Chrome browser is used to execute this cypress test. For other browsers it skips that part of the test.
   */
  function verifyCreatingApiKeyAndCopyingIt(): void {
    cy.get("div.middle-center-div button").contains("CREATE NEW API KEY").click();
    cy.get("div#expiryTime").click();
    cy.get('ul[role="listbox"]').find('[aria-label="No expiry"]').click({ force: true });
    cy.intercept("GET", "**/api-keys/generateApiKey*").as("generateApiKey");
    cy.get("button#generateApiKey").click();
    cy.get('[data-test="apiKeyInfo"]').should("exist");
    cy.get("textarea#newKeyHolder").should("exist");

    if (Cypress.browser.displayName === "Chrome") {
      console.log("yep");
      cy.wrap(
        Cypress.automation("remote:debugger:protocol", {
          command: "Browser.grantPermissions",
          params: {
            permissions: ["clipboardReadWrite", "clipboardSanitizedWrite"],
            origin: window.location.origin,
          },
        })
      );
    }

    cy.get('[data-test="apiKeyInfo"]').find("em").should("exist");
    cy.get('[data-test="text-info"]').find("em").click();

    if (Cypress.browser.displayName === "Chrome") {
      cy.wait("@generateApiKey", { timeout: Cypress.env("short_timeout_in_ms") as number }).then((interception) => {
        cy.window().then((win) => {
          win.navigator.clipboard.readText().then((text) => {
            expect(text).to.eq((interception.response?.body as ApiKeyAndMetaInfo)?.apiKey);
          }, null);
        });
      });
    }

    cy.get('[data-test="text-info"]').find("textarea").should("have.focus");
    cy.get('[data-test="apiKeyInfo"]').find("textarea").should("have.attr", "readonly");
  }

  /**
   * Verifies that the api key page looks and behaves as expected if you visit it while you already have an api key.
   */
  function verifyAlreadyExistingApiKeyState(): void {
    cy.reload(true);
    cy.location("pathname", { timeout: Cypress.env("short_timeout_in_ms") as number }).should("include", "/api-key");
    cy.intercept("GET", "**/api-keys/getApiKeyMetaInfoForUser*", { fixture: "ApiKeyInfoMockWithKey.json" }).as(
      "getApiKeyMetaInfoForUser"
    );
    cy.wait("@getApiKeyMetaInfoForUser", { timeout: Cypress.env("short_timeout_in_ms") as number });
    cy.get('[data-test="regenerateApiKeyMessage"]').should("exist");
    cy.get("textarea#newKeyHolder").should("not.exist");
    cy.get('[data-test="text-info"]').should(
      "contain",
      "If you don't have access to your API Key you can generate a new one"
    );
    cy.get('[id="apiKeyUsageInfoMessage"]').should("contain", "You can use the API-Keys as bearer-tokens");
    cy.get('[data-test="action-button"]').should("contain", "REGENERATE API KEY").click();
    cy.get("div#regenerateApiKeyModal").should("be.visible").find('[data-test="regenerateApiKeyCancelButton"]').click();
    cy.get("div#regenerateApiKeyModal").should("not.exist");
    cy.get('[data-test="action-button"]').should("contain", "REGENERATE API KEY").click();
    cy.get("div#regenerateApiKeyModal")
      .should("be.visible")
      .find('[data-test="regenerateApiKeyConfirmButton"]')
      .click();
    cy.get("h1").should("contain.text", "Create new API Key");
  }

  it("check Api Key functionalities", () => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/api-key");
    cy.intercept("GET", "**/api-keys/getApiKeyMetaInfoForUser*", { fixture: "ApiKeyInfoMockWithNOKey.json" }).as(
      "getApiKeyMetaInfoForUser"
    );
    cy.wait("@getApiKeyMetaInfoForUser", { timeout: Cypress.env("short_timeout_in_ms") as number });

    verifyInitialPageStateAndCreateApiKeyCardPopup();

    verifyExpirationDropdownOptions();

    verifyCreatingApiKeyAndCopyingIt();

    verifyAlreadyExistingApiKeyState();
  });
});
