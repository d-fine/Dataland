import { formatExpiryDate } from "@/utils/DateFormatUtils";

describe("As a user I expect my api key will be generate correctly", () => {
  it("successfully generate api key", () => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/companies");
    cy.get("div#profile-picture-dropdown-toggle").click();
    cy.get('[data-test="profileMenu"]').should("be.visible");
    cy.get("div#profile-picture-dropdown-toggle").click();
    cy.get('[data-test="profileMenu"]').should("not.exist");
    cy.get("div#profile-picture-dropdown-toggle").click();
    cy.get('a[id="profile-api-generate-key-button"]').click({ force: true }).url().should("include", "/api-key");
    cy.intercept("GET", "**/api-keys/getApiKeyMetaInfoForUser*", { fixture: "ApiKeyInfoMockWithNOKey.json" }).as(
      "apiKeyInfo"
    );
    cy.wait("@apiKeyInfo").then((apiKeyInfoFixture) => {
      console.log("ApiKeyInfoMock", apiKeyInfoFixture);
    });
    cy.get("div.midlle-center-div").should("contain.text", "You have no API Key!");
    cy.get("div.midlle-center-div button").contains("CREATE NEW API KEY").click();
    cy.get("h1").should("contain.text", "Create new API Key");
    cy.get("div.custom-dropdown").click();
    cy.get("ul.p-dropdown-items").find('[aria-label="7 days"]').click();
    cy.get("span#expireTimeWrapper").should("contain.text", `The API Key will expire on ${formatExpiryDate(7)}`);
    cy.get("ul.p-dropdown-items").find('[aria-label="90 days"]').click();
    cy.get("span#expireTimeWrapper").should("contain.text", `The API Key will expire on ${formatExpiryDate(90)}`);
    cy.get("button#generateApiKey").click();
    cy.get("textarea#newKeyHolder").should("exist");
  });
});
