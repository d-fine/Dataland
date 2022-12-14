import { formatExpiryDate } from "@/utils/DateFormatUtils";

describe("As a user I expect my api key will be generate correctly", () => {
  it("successfully generate api key", () => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/api-key");
    cy.intercept("GET", "**/api-keys/getApiKeyMetaInfoForUser*", { fixture: "ApiKeyInfoMockWithNOKey.json" }).as(
      "apiKeyInfo"
    );
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
    cy.get("div.middle-center-div button").contains("CREATE NEW API KEY").click();
    cy.get("div#expireTime").find('div[role="button"]').click();
    cy.get('ul[role="listbox"]').find('[aria-label="7 days"]').click();
    cy.get("#expireTimeWrapper").should("contain.text", `The API Key will expire on ${formatExpiryDate(7)}`);
    cy.get("div#expireTime").find('div[role="button"]').click();
    cy.get('ul[role="listbox"]').find('[aria-label="Custom..."]').click();
    cy.get("#expireTimeWrapper").should("not.exist");
    cy.get('[data-test="expireDataPicker"]').should("be.visible");
    cy.get("div#expireTime").find('div[role="button"]').click();
    cy.get('ul[role="listbox"]').find('[aria-label="No expiry"]').click();
    cy.get("#expireTimeWrapper").should("contain.text", `The API Key has no defined expire date`);
    cy.get("button#generateApiKey").click();
    cy.get('[data-test="apiKeyInfo"]').should("exist");
    cy.get('textarea#newKeyHolder[rows=2][placeholder="Key goes here"][readonly]').should("exist");

    cy.get('[data-test="apiKeyInfo"]').find("em").should("exist");
    cy.reload(true);
    cy.location("pathname", { timeout: 10000 }).should("include", "/api-key");
    cy.intercept("GET", "**/api-keys/getApiKeyMetaInfoForUser*", { fixture: "ApiKeyInfoMockWithKey.json" }).as(
      "apiKeyInfo"
    );
    cy.get('[data-test="regenerateApiKeyMessage"]').should("exist");
    cy.get('[data-test="text-info"]').should(
      "contain",
      "If you don't have access to your API Key you can generate a new one"
    );
    cy.get('[data-test="action-button"]').should("contain", "REGENERATE API KEY").click();
    cy.get("div#regenerateApiKeyModal").should("be.visible").find('[data-test="regenerateApiKeyCancelButton"]').click();
    cy.get("div#regenerateApiKeyModal").should("not.exist");
    cy.get('[data-test="action-button"]').should("contain", "REGENERATE API KEY").click();
    cy.get("div#regenerateApiKeyModal")
      .should("be.visible")
      .find('[data-test="regenerateApiKeyConfirmButton"]')
      .click();
    cy.get("h1").should("contain.text", "Create new API Key");
  });
});
