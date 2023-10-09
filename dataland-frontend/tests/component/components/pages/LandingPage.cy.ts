import { checkButton, checkImage, checkAnchorByContent, checkAnchorByTarget } from "@ct/testUtils/ExistenceChecks";
import NewLandingPage from "@/components/pages/NewLandingPage.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";

describe("Component test for the landing page", () => {
  it("Check if essential elements are present", () => {
    cy.mountWithPlugins(NewLandingPage, {
      keycloak: minimalKeycloakMock({
        authenticated: false,
      }),
    }).then(() => {
      validateTopBar();
      validateIntroSection();
      validateBrandsSection();

      validateQuotesSlides();
      validateHowItWorksSlides();
      checkNewFooter();
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
  checkButton("signup_dataland_button", "Sign Up", getTopBar());
  checkAnchorByContent("Login", getTopBar());
}

/**
 * Validates the elements of the intro section
 */
function validateIntroSection(): void {
  checkImage("Liberate Data -  Empower Autonomy. Dataland, the Open ESG Data Platform.", "gfx_logo_d_orange_S.svg");
  cy.get("h1").should("contain.text", "Liberate Data");
  cy.get("h1").should("contain.text", "Empower Autonomy");
}

/**
 * Validates the images of the brands trusting in dataland
 */
function validateBrandsSection(): void {
  checkImage("Brand 1", "img_deka.png");
  checkImage("Brand 2", "img_ampega.png");
  checkImage("Brand 3", "img_chom_capital.png");
  checkImage("Brand 4", "img_metzler.png");
  checkImage("Brand 5", "img_fuerstl_bank.png");
  checkImage("Brand 6", "img_envoria.png");
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

  checkAnchorByTarget("/imprint", "Imprint", getFooter());
  checkAnchorByTarget("/dataprivacy", "Data Privacy", getFooter());
}

/**
 * Validates that the slide show on "Quotes" works as expected
 */
function validateQuotesSlides(): void {
  const slidesSelector = "section.quotes .quotes__slides";
  const leftButtonSelector = "section.quotes button[aria-label='Previous slide']";
  const rightButtonSelector = "section.quotes button[aria-label='Next slide']";

  assertSlidesPosition(slidesSelector);
  cy.get(rightButtonSelector).click();
  assertSlidesPosition(slidesSelector, 1, 1);
  cy.get(rightButtonSelector).click();
  assertSlidesPosition(slidesSelector, 2, 1);
  cy.get(leftButtonSelector).click();
  assertSlidesPosition(slidesSelector, 1, 1);
  cy.get(leftButtonSelector).click();
  assertSlidesPosition(slidesSelector, 0, 1);
  cy.get(leftButtonSelector).click();
  assertSlidesPosition(slidesSelector, 0, 1);
}

/**
 * Validates that the slide show on "How it works" works as expected
 */
function validateHowItWorksSlides(): void {
  const slidesSelector = "div.howitworks__wrapper .howitworks__slides";
  const leftButtonSelector = "div.howitworks__wrapper button[aria-label='Previous slide']";
  const rightButtonSelector = "div.howitworks__wrapper button[aria-label='Next slide']";

  assertSlidesPosition(slidesSelector);
  cy.get(rightButtonSelector).click();
  assertSlidesPosition(slidesSelector, 1);
  cy.get(rightButtonSelector).click();
  assertSlidesPosition(slidesSelector, 2);
  cy.get(leftButtonSelector).click();
  assertSlidesPosition(slidesSelector, 1);
}

function assertSlidesPosition(slidesSelector: string, position?: number, centerElement = 0): void {
  const expectedTransformValue =
    position == undefined ? "none" : `matrix(1, 0, 0, 1, ${-440 * (position - centerElement)}, 0)`;
  cy.get(slidesSelector).should("have.css", "transform", expectedTransformValue);
}
