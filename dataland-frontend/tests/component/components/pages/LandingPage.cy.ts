import { checkButton, checkImage, checkAnchorByContent } from '@ct/testUtils/ExistenceChecks';
import NewLandingPage from '@/components/pages/NewLandingPage.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import content from '@/assets/content.json';
import { type Page, type Section } from '@/types/ContentTypes';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { setMobileDeviceViewport } from '@sharedUtils/TestSetupUtils';

describe('Component test for the landing page', () => {
  it('Check if essential elements are present', () => {
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

      assertFrameworkPanelExists('Pathways to Paris');
      assertFrameworkPanelExists('LkSG');
      assertFrameworkPanelExists('EU Taxonomy');
      assertFrameworkPanelExists('SFDR');
      cy.get('button.joincampaign__button').should('exist');
      cy.get('button.getintouch__text-button').should('exist');
      checkNewFooter();

      setMobileDeviceViewport();
      validateTheHeader();
    });
  });
});

/**
 * Validates the elements of the top bar
 */
function validateTheHeader(): void {
  checkImage('Dataland banner logo', getSingleImageNameInSection('Welcome to Dataland'));
  checkButton('signup_dataland_button', 'Sign Up');
  checkAnchorByContent('Login');
}

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
  expect(images?.length).to.eq(24);
  images!.forEach((image, index) => {
    const filename = image.split('/').slice(-1)[0];
    checkImage(`Brand ${index + 1}`, filename);
  });
}

/**
 * Check the new footer for long-term sustainability.
 */
function checkNewFooter(): void {
  cy.get('footer').should('exist');

  cy.get('.footer__logo').should('exist');

  const currentYear = new Date().getFullYear();
  const expectedCopyrightText = `Copyright © ${currentYear} Dataland`;

  cy.get('.footer__copyright').should('contain.text', expectedCopyrightText);

  const essentialLinks = [
    { href: '/imprint', text: 'IMPRINT' },
    { href: '/dataprivacy', text: 'DATA PRIVACY' },
    { href: '/terms', text: 'LEGAL' },
  ];

  essentialLinks.forEach((link) => {
    cy.get(`footer a[href='${link.href}']`).should('contain.text', link.text);
  });
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
  return assertDefined(getLandingPageSection(sectionTitle)?.image?.[0]).split('/').slice(-1)[0];
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
