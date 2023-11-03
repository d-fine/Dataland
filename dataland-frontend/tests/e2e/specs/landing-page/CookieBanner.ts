import { describeIf } from "@e2e/support/TestUtility";

describe("As a user, I expect the cookie banner to render and to be functional", () => {
  describeIf(
    "Do not execute these tests in the CI pipeline, because the github actions server is not registered on the cookiebot.com website",
    {
      executionEnvironments: ["developmentLocal"],
    },
    () => {
      it("Check that the cookie banner renders", () => {
        cy.visitAndCheckAppMount("/");
        cy.get("div[id='CybotCookiebotDialog']").should("exist");
        cy.get("#CybotCookiebotDialogPoweredbyImage").invoke("attr", "src").should("contain", "data:image/png;base64,");
        cy.get("button[id='CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll']").should("exist");
        cy.get("button[id='CybotCookiebotDialogBodyLevelButtonLevelOptinAllowallSelection']").should("exist");
        cy.get("button[id='CybotCookiebotDialogBodyButtonDecline']").should("exist");
        cy.get("div[id='CybotCookiebotDialogBodyFieldsetInnerContainer']").should("exist");
        cy.get("h2[id='CybotCookiebotDialogBodyContentTitle']")
          .should("exist")
          .should("contain", "This website uses cookies");

        cy.get("#CybotCookiebotDialogBodyEdgeMoreDetailsLink")
          .should("exist")
          .click()
          .get("#CybotCookiebotDialogDetailBodyContentCookieContainerTypes")
          .should("exist")
          .get("li.CookieCard")
          .should("have.length", 5)
          .first()
          .get("label")
          .should("contain", "Necessary");

        cy.get("#CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll").click();
        cy.get("#CybotCookiebotDialog").should("not.be.visible");
      });
    },
  );
});
