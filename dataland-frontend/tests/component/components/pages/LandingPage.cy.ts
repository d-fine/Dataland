import { checkButton, checkImage, checkLinkByName, checkLinkByTarget } from "@ct/testUtils/ExistenceChecks";
import NewLandingPage from "@/components/pages/NewLandingPage.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";

describe("Component test for the landing page", () => {
  it("Check if essential elements are present", () => {
    cy.mountWithPlugins(NewLandingPage, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      const landingPagePath = mounted.component.$route.path;
      validateTopBar();
      validateIntroSection();
      validateBrandsSection();
      checkLinkByTarget("/mission", "OUR MISSION");
      // TODO slide show test

      // TODO test targets
      cy.get("button.campaigns__button").should("have.length", 4);
      cy.get("button.campaigns__button").each((element) => {
        expect(element.text()).to.equal("JOIN");
        element.click();
        cy.wait(100);
        cy.wrap(mounted.component).its("$route.path").should("eq", landingPagePath);
      });

      checkNewFooter();

      // TODO check redirecting buttons that work for redirection
      // TODO unfunctional buttons for staying on the page or being disabled
    });
  });
});

/**
 * Validates the elements of the top bar
 */
function validateTopBar(): void {
  /**
   * Gets the top bar element
   * @returns the top bar element
   */
  function getTopBar(): Cypress.Chainable {
    return cy.get("header");
  }
  checkImage("Dataland banner logo", "gfx_logo_dataland_orange_S.svg", getTopBar());
  // TODO validate targets after clicking
  checkButton("signup_dataland_button", "Sign Up", getTopBar());
  checkLinkByName("login_dataland_button", "Login", getTopBar());
  checkLinkByTarget("/mission", "Mission", getTopBar());
  checkLinkByTarget("/community", "Community", getTopBar());
  checkLinkByTarget("/campaigns", "Campaigns", getTopBar());
}

/**
 * Validates the elements of the intro section
 */
function validateIntroSection(): void {
  const title1 = "Liberate Data";
  const title2 = "Empower Autonomy. Break Monopolies.";
  checkImage(`${title1} -  ${title2}`, "gfx_logo_d_orange_S.svg");
  cy.get("h1").should("contain.text", title1);
  cy.get("h1").should("contain.text", title2);
  // TODO check searchbar and button
}

/**
 * Validates the images of the brands trusting in dataland
 */
function validateBrandsSection(): void {
  checkImage("Brand 1", "img_chom_capital.png");
  checkImage("Brand 2", "img_envoria.png");
  checkImage("Brand 3", "img_fuerstl_bank.png");
  checkImage("Brand 4", "img_ampega.png");
  checkImage("Brand 5", "img_metzler.png");
  checkImage("Brand 6", "img_deka.png");
}

/**
 * Check the new footer
 */
function checkNewFooter(): void {
  /**
   * Gets the footer element
   * @returns the footer element
   */
  function getFooter(): Cypress.Chainable {
    return cy.get("footer");
  }
  getFooter().should("exist");
  checkImage("Copyright ©   Dataland", "gfx_logo_dataland_orange_S.svg", getFooter());
  cy.get(".footer__copyright").should("contain.text", "Copyright © 2023 Dataland");

  // TODO check targets
  checkLinkByTarget("/legal", "Legal", getFooter());
  checkLinkByTarget("/imprint", "Imprint", getFooter());
  checkLinkByTarget("/dataprivacy", "Data Privacy", getFooter());
}
