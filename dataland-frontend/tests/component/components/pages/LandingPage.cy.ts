import { checkButton, checkImage, checkAnchorByContent, checkAnchorByTarget } from "@ct/testUtils/ExistenceChecks";
import NewLandingPage from "@/components/pages/NewLandingPage.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import content from "@/assets/content.json";
import { type Page, type Section } from "@/types/ContentTypes";
import { assertDefined } from "@/utils/TypeScriptUtils";

describe("Component test for the landing page", () => {
  it("Check if essential elements are present", () => {
    cy.mountWithPlugins(NewLandingPage, {
      keycloak: minimalKeycloakMock({
        authenticated: false,
      }),
    }).then(() => {
      validateTheHeader();
      validateIntroSection();
      validateBrandsSection();
      validateStruggleSection();
      validateQuotesSection();
      validateQuotesSectionAttributes();
      validateSlideshowArrows();
      validateThumbnailOverlays();
      validateSlideTextContent();
      validateHowItWorksSection();

      assertFrameworkPanelExists("Pathways to Paris");
      assertFrameworkPanelExists("LkSG");
      assertFrameworkPanelExists("EU Taxonomy");
      assertFrameworkPanelExists("SFDR");
      cy.get("button.joincampaign__button").should("exist");
      cy.get("button.getintouch__text-button").should("exist");
      checkNewFooter();
    });
  });
});

/**
 * Validates the elements of the top bar
 */
function validateTheHeader(): void {
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
  expect(images?.length).to.eq(10);
  images!.forEach((image, index) => {
    const filename = image.split("/").slice(-1)[0];
    checkImage(`Brand ${index + 1}`, filename);
  });
}

/**
 * Check the new footer
 */
function checkNewFooter(): void {
  cy.get("footer").should("exist");
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
 * Asserts that there is a join campaing panel for the given framework
 * @param frameworkTitle the title of the framework to check for
 */
function assertFrameworkPanelExists(frameworkTitle: string): void {
  cy.get(`.joincampaign__cell:contains("${frameworkTitle}")`).should("exist");
}

/**
 * Validates the existence and general structure of the "Struggle" section
 */
function validateStruggleSection(): void {
  cy.get("section.struggle").should("exist");
  const struggleCellContent: { imageFilename: string; title: string }[] = [
    {
      imageFilename: "Diagram.svg",
      title: "Data Gaps",
    },
    {
      imageFilename: "Badge.svg",
      title: "Quality Issues",
    },
    {
      imageFilename: "3d-mpr-toggle.svg",
      title: "Usage Restrictions",
    },
    {
      imageFilename: "Airline--rapid-board.svg",
      title: "High Price",
    },
  ];
  cy.get(".struggle__cell").each((element, index) => {
    checkImage(struggleCellContent[index].title, struggleCellContent[index].imageFilename);
    cy.wrap(element).should("contain.text", struggleCellContent[index].title);
  });
}

/**
 * Validates the existence and general structure of the "Quotes" section
 */
function validateQuotesSection(): void {
  cy.get("section.quotes").should("exist");
  cy.get(".quotes__slide").should("have.length", 5);
}

/**
 * Validates that the "Quotes" section renders correctly with the right attributes
 */
function validateQuotesSectionAttributes(): void {
  cy.get("section.quotes").should("have.attr", "role", "region");
  cy.get("section.quotes").should("have.attr", "aria-label", "The Quotes");
}

/**
 * Validates the presence and functionality of the slideshow navigation arrows
 */
function validateSlideshowArrows(): void {
  cy.get(".quotes__arrow--left").should("exist").and("be.visible");
  cy.get(".quotes__arrow--right").should("exist").and("be.visible");

  // Capture the initial text content of the first slide
  cy.get(".quotes__slide")
    .first()
    .invoke("text")
    .then((initialFirstSlide) => {
      // Check if clicking the right arrow moves the slideshow forward
      cy.get(".quotes__arrow--right").click();
      cy.get(".quotes__slide").first().should("not.have.text", initialFirstSlide);

      // After moving forward, clicking the left arrow should bring back the original slide
      cy.get(".quotes__arrow--left").click();
      cy.get(".quotes__slide").first().should("have.text", initialFirstSlide);
    });
}

/**
 * Validates the video thumbnails and overlay play button functionality
 */
function validateThumbnailOverlays(): void {
  // Assert that the overlays are present
  cy.get(".quotes__slide-thumbnail-overlay").should("exist").and("have.length.at.least", 1);

  // Simulate clicking an overlay to play a video
  cy.get(".quotes__slide-thumbnail-overlay").first().as("firstOverlay");
  cy.window().then((win) => {
    // Stub the playVideo method
    cy.stub(win.YT.Player.prototype, "playVideo").as("playVideo");
  });

  // Simulate the click and verify the thumbnail is hidden and the video is played
  cy.get("@firstOverlay")
    .click()
    .then(() => {
      // The thumbnail should not be visible after clicking
      cy.get("@firstOverlay").should("not.be.visible");
      // The playVideo function should have been called
      cy.get("@playVideo").should("have.been.calledOnce");
    });
}

/**
 *  Validates the text content changes when the slide changes
 */
function validateSlideTextContent(): void {
  cy.get(".quotes__slide-title").should("exist");
  cy.get(".quotes__slide-text").should("exist");

  const initialTextContent = cy.get(".quotes__slide-text").invoke("text");
  cy.get(".quotes__arrow--right").click();
  const changedTextContent = cy.get(".quotes__slide-text").invoke("text");

  expect(initialTextContent).not.to.equal(changedTextContent);
}

describe("Quotes Section Tests", () => {
  beforeEach(() => {
    cy.visit("/");
  });

  it("should validate the structure of quotes section", validateQuotesSection);
  it("should validate quotes section attributes", validateQuotesSectionAttributes);
  it("should validate slideshow navigation arrows", validateSlideshowArrows);
  it("should validate thumbnail overlays", validateThumbnailOverlays);
  it("should validate slide text content", validateSlideTextContent);
});
/**
 * Validates the existence and general structure of the "How it works" section
 */
function validateHowItWorksSection(): void {
  cy.get("section.howitworks").should("exist");
  cy.get(".howitworks__slide").should("have.length", 4);
  cy.get(".howitworks__slide").eq(0).should("contain.text", "Search");
  cy.get(".howitworks__slide").eq(1).should("contain.text", "Request company’s inclusion");
  cy.get(".howitworks__slide").eq(2).should("contain.text", "Request framework data");
  cy.get(".howitworks__slide").eq(3).should("contain.text", "Download");
}
