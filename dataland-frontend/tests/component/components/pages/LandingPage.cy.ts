import { checkButton, checkImage, checkLinkByContent, checkLinkByTarget } from "@ct/testUtils/ExistenceChecks";
import NewLandingPage from "@/components/pages/NewLandingPage.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";

describe("Component test for the landing page", () => {
  it("Check if essential elements are present", () => {
    cy.mountWithPlugins(NewLandingPage, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      validateTopBar();
      validateIntroSection();
      validateBrandsSection();
      // checkLinkByTarget("/mission", "OUR MISSION");
      cy.get("button:contains('START YOUR DATALAND JOURNEY')"); // TODO .should("be.disabled");
      // TODO slide show test
      // TODO test I AM INTERESTED button
      // TODO test get in contact button

      // TODO test targets
      // cy.get("button.campaigns__button").should("have.length", 4);
      // cy.get("button.campaigns__button").each((element) => {
      //   expect(element.text()).to.equal("JOIN");
      //   element.trigger("click");
      //   cy.wait(100);
      //   cy.wrap(mounted.component).its("$route.path").should("eq", landingPagePath);
      // });

      validateHowItWorksSlides();
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
  checkLinkByContent("Login", getTopBar());
  // checkLinkByTarget("/mission", "Mission", getTopBar());
  // checkLinkByTarget("/community", "Community", getTopBar());
  // checkLinkByTarget("/campaigns", "Campaigns", getTopBar());
}

/**
 * Validates the elements of the intro section
 */
function validateIntroSection(): void {
  checkImage("Liberate Data -  Empower Autonomy. Dataland, the Open ESG Data Platform.", "gfx_logo_d_orange_S.svg");
  cy.get("h1").should("contain.text", "Liberate Data");
  cy.get("h1").should("contain.text", "Empower Autonomy");
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

/**
 * Validates that the slide show on "How it works" works as expected
 */
function validateHowItWorksSlides(): void {
  /**
   * Gets the "How it works" element
   * @returns the "How it works" element
   */
  function getSlidesWrapper(): Cypress.Chainable {
    return cy.get("div.howitworks__wrapper");
  }

  getSlidesWrapper().find(".howitworks__slides").should("have.css", "transform", "none");
  getSlidesWrapper().find("button[aria-label='Next slide']").click();
  getSlidesWrapper().find(".howitworks__slides").should("have.css", "transform", "matrix(1, 0, 0, 1, -440, 0)");
  getSlidesWrapper().find("button[aria-label='Next slide']").click();
  getSlidesWrapper().find(".howitworks__slides").should("have.css", "transform", "matrix(1, 0, 0, 1, -880, 0)");
  getSlidesWrapper().find("button[aria-label='Previous slide']").click();
  getSlidesWrapper().find(".howitworks__slides").should("have.css", "transform", "matrix(1, 0, 0, 1, -440, 0)");
}
