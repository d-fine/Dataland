import { checkButton, checkImage, checkAnchorByContent, checkAnchorByTarget } from "@ct/testUtils/ExistenceChecks";
import NewLandingPage from "@/components/pages/NewLandingPage.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import content from "@/assets/content.json"
import {Page, Section} from "../../../../src/types/ContentTypes";
import {assertDefined} from "../../../../src/utils/TypeScriptUtils";

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
  checkImage("Dataland banner logo", getSingleImageNameInSection("Welcome to Dataland"));
  checkButton("signup_dataland_button", "Sign Up");
  checkAnchorByContent("Login");
}

/**
 * Validates the elements of the intro section
 */
function validateIntroSection(): void {
  checkImage("Liberate Data -  Empower Autonomy.   The alternative to data monopolies.", getSingleImageNameInSection("Intro"));
  cy.get("h1").should("contain.text", "Liberate Data");
  cy.get("h1").should("contain.text", "Empower Autonomy");
}

/**
 * Validates the images of the brands trusting in dataland
 */
function validateBrandsSection(): void {
  const images = getLandingPageSection("Brands").image;
  expect(images?.length).to.eq(6);
  images!.forEach((image, index) => {
    const filename = image.split("/").slice(-1)[0];
    checkImage(`Brand ${index + 1}`, filename);
  });
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
  checkImage("Copyright ©   Dataland", getSingleImageNameInSection("Footer"));
  cy.get(".footer__copyright").should("contain.text", `Copyright © ${new Date().getFullYear()} Dataland`);

  checkAnchorByTarget("/imprint", "Imprint");
  checkAnchorByTarget("/dataprivacy", "Data Privacy");
}

/**
 * Gets the section of the landing page with the specified title
 * @param sectionTitle the title of the section to find
 * @returns the section with the given title
 */
function getLandingPageSection(sectionTitle: string): Section {
  return assertDefined((content.pages?.find((page) => page.title == "Landing Page") as Page | undefined)
    ?.sections.find((section) => section.title == sectionTitle))
}

/**
 * Gets the first image in the section of the landing page with the specified title
 * @param sectionTitle the title of the section to find
 * @returns the filename of the found image
 */
function getSingleImageNameInSection(sectionTitle: string): string {
  return assertDefined(getLandingPageSection(sectionTitle)?.image?.[0]).split("/").slice(-1)[0]
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
