import { checkButton, checkImage, checkAnchorByContent, checkAnchorByTarget } from "@ct/testUtils/ExistenceChecks";
import NewLandingPage from "@/components/pages/NewLandingPage.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import content from "@/assets/content.json";
import { type Page, type Section } from "@/types/ContentTypes";
import { assertDefined } from "@/utils/TypeScriptUtils";

const SLIDE_DELTA = 440;

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
  checkImage(
    "Liberate Data -  Empower Autonomy.   The alternative to data monopolies.",
    getSingleImageNameInSection("Intro"),
  );
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
  return assertDefined(
    (content.pages?.find((page) => page.title == "Landing Page") as Page | undefined)?.sections.find(
      (section) => section.title == sectionTitle,
    ),
  );
}

/**
 * Gets the first image in the section of the landing page with the specified title
 * @param sectionTitle the title of the section to find
 * @returns the filename of the found image
 */
function getSingleImageNameInSection(sectionTitle: string): string {
  return assertDefined(getLandingPageSection(sectionTitle)?.image?.[0])
    .split("/")
    .slice(-1)[0];
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
  dragCenterSlideLeft(".quotes__slide", 0);
  assertSlidesPosition(slidesSelector, 1, 1);
  dragCenterSlideRight(".quotes__slide", 1);
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
  dragCenterSlideLeft(".howitworks__slide", 2);
  assertSlidesPosition(slidesSelector, 2);
  dragCenterSlideRight(".howitworks__slide", 3);
  assertSlidesPosition(slidesSelector, 1);
}

/**
 * Checks that a slide show is centered at a given slide
 * @param slidesSelector the selector for the slides wrapper
 * @param centerSlide the index of the slide that is expected to be in the center
 * @param initialCenterSlide the index of the slide that was centered initially
 */
function assertSlidesPosition(slidesSelector: string, centerSlide?: number, initialCenterSlide = 0): void {
  const expectedTransformValue =
    centerSlide == undefined ? "none" : `matrix(1, 0, 0, 1, ${-SLIDE_DELTA * (centerSlide - initialCenterSlide)}, 0)`;
  cy.get(slidesSelector).should("have.css", "transform", expectedTransformValue);
}

/**
 * Drags the selected center slide one position to the left
 * @param genericSlideSelector a selector that applies for each of the slides in the slide show
 * @param centerSlide the center slide
 */
function dragCenterSlideLeft(genericSlideSelector: string, centerSlide: number): void {
  const viewportWidth = Cypress.config("viewportWidth");
  const viewportHeight = Cypress.config("viewportHeight");
  dragSlideTo(genericSlideSelector, centerSlide, viewportWidth / 2 - SLIDE_DELTA, viewportHeight / 2);
}

/**
 * Drags the selected center slide one position to the right
 * @param genericSlideSelector a selector that applies for each of the slides in the slide show
 * @param centerSlide the center slide
 */
function dragCenterSlideRight(genericSlideSelector: string, centerSlide: number): void {
  const viewportWidth = Cypress.config("viewportWidth");
  const viewportHeight = Cypress.config("viewportHeight");
  dragSlideTo(genericSlideSelector, centerSlide, viewportWidth / 2 + SLIDE_DELTA, viewportHeight / 2);
}

/**
 * Drags the selected slide to the given position
 * @param genericSlideSelector a selector that applies for each of the slides in the slide show
 * @param slideIndex the slide to drag
 * @param targetX the targets position x value in viewport coordinates
 * @param targetY the targets position y value in viewport coordinates
 */
function dragSlideTo(genericSlideSelector: string, slideIndex: number, targetX: number, targetY: number): void {
  cy.get(genericSlideSelector).eq(slideIndex).click();
  cy.get(genericSlideSelector)
    .eq(slideIndex)
    .trigger("pointerdown", 10, 10, { button: 0 })
    .trigger("pointermove", {
      eventConstructor: "MouseEvent",
      clientX: targetX,
      clientY: targetY,
    })
    .trigger("pointerup", { button: 0 });
}
