import { formatExpiryDate } from "@/utils/DateFormatUtils";

describe("As a user I expect my api key will be generate correctly", () => {
  it("successfully generate api key", () => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/companies");
    cy.get('img[alt="Open drop down menu icon"]').click();
    cy.get('a[id="profile-api-generate-key-button"]').click({ force: true }).url().should("include", "/api-key");
    cy.get("div.midlle-center-div").should("contain.text", "You have no API Key!");
    cy.get("div.midlle-center-div button").contains("CREATE NEW API KEY").click();
    cy.get("h1").should("contain.text", "Create new API Key");
    cy.get("div.custom-dropdown").click();
    cy.get("ul.p-dropdown-items").find('[aria-label="7 days"]').click();
    cy.get("span#expireTimeWrapper").should("contain.text", `${formatExpiryDate(7)}: string`);
    cy.get("ul.p-dropdown-items").find('[aria-label="90 days"]').click();
    cy.get("span#expireTimeWrapper").should("contain.text", `${formatExpiryDate(90)}: string`);
    cy.get("button#generateApiKey").click();
    cy.get("textarea#newKeyHolder").should("exist");
    cy.reload();
    cy.get("textarea#newKeyHolder").should("not.exist");
  });
});
