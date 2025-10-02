import LandingPage from '@/components/pages/LandingPage.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import content from '@/assets/content.json';
import { type Page, type Section } from '@/types/ContentTypes';
import { assertDefined } from '@/utils/TypeScriptUtils';

describe('Component test for the landing page', () => {
  it('Check if essential elements are present', () => {
    cy.mountWithPlugins(LandingPage, {
      keycloak: minimalKeycloakMock({
        authenticated: false,
      }),
    }).then(() => {
      validateIntroSection();
      validateBrandsSection();
      validateStruggleSection();
      validateQuotesSection();
      validateHowItWorksSection();

      assertFrameworkPanelExists('VSME');
      assertFrameworkPanelExists('LkSG');
      assertFrameworkPanelExists('EU Taxonomy');
      assertFrameworkPanelExists('SFDR');
      cy.get('[data-test="join-campaign-button"]').should('exist');
      cy.get('[data-test="get-in-touch-button"]').should('exist');
    });
  });
});

/**
 * Validates the elements of the intro section
 */
function validateIntroSection(): void {
  checkImage(
    'Liberate Data -  Empower Autonomy.   The alternative to data monopolies.',
    getSingleImageNameInSection('Intro')
  );
  cy.get('h1').should('contain.text', 'Liberate Data');
  cy.get('h1').should('contain.text', 'Empower Autonomy');
  cy.get('input#company_search_bar_standard').should('exist');
}

/**
 * Validates the images of the brands trusting in dataland
 */
function validateBrandsSection(): void {
  const images = getLandingPageSection('Brands').image;
  expect(images?.length).to.eq(33);
  for (const [index, image] of (images ?? []).entries()) {
    const filename = image.split('/').slice(-1)[0];
    if (!filename) throw new Error('Filename is undefined');
    checkImage(`Brand ${index + 1}`, filename);
  }
}

/**
 * Checks if an image is present
 * @param alternativeText the "alt" identifier of the image
 * @param fileName the file the image is expected to display
 */
function checkImage(alternativeText: string, fileName: string): void {
  cy.get(`img[alt="${alternativeText}"]`)
    .should('be.visible')
    .should('have.attr', 'src')
    .should('match', new RegExp(`.*/${fileName}$`));
}

/**
 * Gets the section of the landing page with the specified title
 * @param sectionTitle the title of the section to find
 * @returns the section with the given title
 */
function getLandingPageSection(sectionTitle: string): Section {
  return assertDefined(
    (content.pages?.find((page) => page.title == 'Home') as Page | undefined)?.sections.find(
      (section) => section.title == sectionTitle
    )
  );
}

/**
 * Gets the first image in the section of the landing page with the specified title
 * @param sectionTitle the title of the section to find
 * @returns the filename of the found image
 */
function getSingleImageNameInSection(sectionTitle: string): string {
  const landingPageSection = assertDefined(getLandingPageSection(sectionTitle)?.image?.[0]);
  const singleImage = landingPageSection.split('/').slice(-1)[0];
  if (!singleImage) throw new Error(`No image found in section ${sectionTitle}`);
  return singleImage;
}

/**
 * Asserts that there is a join campaing panel for the given framework
 * @param frameworkTitle the title of the framework to check for
 */
function assertFrameworkPanelExists(frameworkTitle: string): void {
  cy.get(`.joincampaign__cell:contains("${frameworkTitle}")`).should('exist');
}

/**
 * Validates the existence and general structure of the "Struggle" section
 */
function validateStruggleSection(): void {
  cy.get('section.struggle').should('exist');
  const struggleCellContent: { imageFilename: string; title: string }[] = [
    {
      imageFilename: 'Diagram.svg',
      title: 'Data Gaps',
    },
    {
      imageFilename: 'Badge.svg',
      title: 'Quality Issues',
    },
    {
      imageFilename: '3d-mpr-toggle.svg',
      title: 'Usage Restrictions',
    },
    {
      imageFilename: 'Airline--rapid-board.svg',
      title: 'High Price',
    },
  ];

  cy.get('.struggle__cell').each((element, index) => {
    if (!struggleCellContent[index]) throw new Error(`No content for struggle cell ${index}`);
    checkImage(struggleCellContent[index].title, struggleCellContent[index].imageFilename);
    cy.wrap(element).should('contain.text', struggleCellContent[index].title);
  });
}

/**
 * Validates the existence and general structure of the "Quotes" section
 */
function validateQuotesSection(): void {
  cy.get('section.quotes').should('exist');
  cy.get('.quotes__slide').should('exist');
}

/**
 * Validates the existence and general structure of the "How it works" section
 */
function validateHowItWorksSection(): void {
  cy.get('section.howitworks').should('exist');
  cy.get('.howitworks__slide').should('have.length', 4);
  cy.get('.howitworks__slide').eq(0).should('contain.text', 'Search');
  cy.get('.howitworks__slide').eq(1).should('contain.text', 'Request company’s inclusion');
  cy.get('.howitworks__slide').eq(2).should('contain.text', 'Request framework data');
  cy.get('.howitworks__slide').eq(3).should('contain.text', 'Download');
}
