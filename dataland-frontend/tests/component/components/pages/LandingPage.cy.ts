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
      validateTopBar();
      validateIntroSection();
      validateBrandsSection();

      assertFrameworkPanelExists("Pathway to Paris");
      assertFrameworkPanelExists("LksG");
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
 * Asserts that there is a join campaing panel for the given framework
 * @param frameworkTitle the title of the framework to check for
 */
function assertFrameworkPanelExists(frameworkTitle: string): void {
  cy.get(`.joincampaign__cell:contains("${frameworkTitle}")`).should("exist");
}
