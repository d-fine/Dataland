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
  expect(images?.length).to.eq(11);
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
      title: "Data Quality",
    },
    {
      imageFilename: "3d-mpr-toggle.svg",
      title: "Data Usage Rights",
    },
    {
      imageFilename: "Airline--rapid-board.svg",
      title: "Data Usage",
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
  cy.get(".quotes__slide").should("have.length", 3);
}

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
