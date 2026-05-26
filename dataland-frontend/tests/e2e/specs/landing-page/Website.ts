import { checkFooter } from '@sharedUtils/ElementChecks.ts';
const cookieConsentValue =
  '{"stamp":"ywkY3Xqqc7BU3ejitK/n5QHNOWOCUdobQFyL+wVWcgRXqNVgJ9XNXQ==","necessary":true,"preferences":true,"statistics":true,method:"explicit","marketing":true,"ver":1,utc:1776958428355,"region":"de"}';

/** Navigates to the legal page, checks the language toggle, and returns to home. */
function checkLegalPage(): void {
  cy.get("a[href='/legal']").click();
  cy.get("h1:contains('General terms and conditions for participation in Dataland')");
  cy.get("[data-test='terms-language-toggle-button']").should('exist').click();
  cy.get("h1:contains('Allgemeine Bedingungen für die Teilnahme an Dataland')");
  cy.get("[data-test='terms-language-toggle-button']").should('exist').click();
  cy.get("h1:contains('General terms and conditions for participation in Dataland')");
  cy.get("a[href='/']").eq(0).click();
}

/** Navigates to the imprint page, checks the heading, and returns to home. */
function checkImprintPage(): void {
  cy.get("a[href='/imprint']").click();
  cy.get("h2:contains('Impressum')");
  cy.get("a[href='/']").eq(0).click();
}

/** Navigates to the data privacy page, checks the heading, and returns to home. */
function checkDataPrivacyPage(): void {
  cy.get("a[href='/dataprivacy']").click();
  cy.get("h1:contains('Datenschutzhinweise')");
  cy.get("a[href='/']").eq(0).click();
}

const apiLinkSelector = '#documentation [data-test="api-link"]';

/** Clicks an API documentation link, checks the URL, and returns to the documentation section. */
function clickAndCheckApiLink(index: number): void {
  cy.location('origin').then((origin) => {
    cy.get(apiLinkSelector)
      .eq(index)
      .invoke('attr', 'href')
      .should('exist')
      .then((href) => {
        const expectedUrl = new URL(String(href), origin).href;

        cy.get(apiLinkSelector).eq(index).invoke('removeAttr', 'target').click();

        cy.url().should('eq', expectedUrl);

        cy.visit('/product#documentation');
      });
  });
}

/** Checks all API documentation links. */
function checkApiDocumentationLinks(): void {
  cy.visit('/product#documentation');

  cy.get(apiLinkSelector)
    .should('have.length.greaterThan', 0)
    .then(($links) => {
      const linkIndexes = [...$links].map((_, index) => index);

      cy.wrap(linkIndexes).each((index) => {
        clickAndCheckApiLink(Number(index));
      });
    });
}

describe('Check that the website works properly', () => {
  beforeEach(() => {
    cy.setCookie('CookieConsent', cookieConsentValue);

    cy.visitAndCheckAppMount('/');
  });

  it('Check the links and buttons', () => {
    cy.visitAndCheckAppMount('/');
    checkFooter();

    cy.get("[data-test='login-dataland-button']").click();
    cy.url().should('include', '/keycloak/realms/datalandsecurity/protocol/openid-connect/auth');
    cy.get("span:contains('HOME')").click();

    cy.get(`[data-test="signup-dataland-button"]`).click();
    cy.url().should('include', '/keycloak/realms/datalandsecurity/protocol/openid-connect/registrations');
    cy.get("span:contains('HOME')").click();

    cy.get("nav a[href='/about']:visible").should('exist').click();
    cy.url().should('include', '/about');
    cy.get("a[href='/']").eq(0).click();

    cy.get("nav a[href='/dataland-community']:visible").should('exist').click();
    cy.url().should('include', '/dataland-community');
    cy.get("a[href='/']").eq(0).click();

    cy.get("nav a[href='/product']:visible").should('exist').click();
    cy.url().should('include', '/product');

    checkApiDocumentationLinks();

    cy.get("a[href='/']").eq(0).click();

    checkLegalPage();
    checkImprintPage();
    checkDataPrivacyPage();
  });

  it('Check the links and buttons on mobile', () => {
    cy.viewport(
      Number(Cypress.expose('mobile_device_viewport_width') ?? 300),
      Number(Cypress.expose('mobile_device_viewport_height') ?? 667)
    );
    cy.visitAndCheckAppMount('/');

    cy.get('#mobile-menu-toggle').should('be.visible').click();
    cy.get('#mobile-nav').should('be.visible');

    cy.get('[data-test="login-dataland-button-mobile"]').should('be.visible').click();
    cy.url().should('include', '/keycloak/realms/datalandsecurity/protocol/openid-connect/auth');
    cy.get("span:contains('HOME')").click();

    cy.get('#mobile-menu-toggle').click();
    cy.get('[data-test="signup-dataland-button-mobile"]').should('be.visible').click();
    cy.url().should('include', '/keycloak/realms/datalandsecurity/protocol/openid-connect/registrations');
    cy.get("span:contains('HOME')").click();

    cy.get('#mobile-menu-toggle').click();
    cy.get('#mobile-nav a.mobile-nav__link[href="/about"]').should('be.visible').click();
    cy.url().should('include', '/about');
    cy.get("a[href='/']").eq(0).click();

    cy.get('#mobile-menu-toggle').click();
    cy.get('#mobile-nav a.mobile-nav__link[href="/product"]').should('be.visible').click();
    cy.url().should('include', '/product');
    cy.get("a[href='/']").eq(0).click();

    cy.get('#mobile-menu-toggle').click();
    cy.get('#mobile-nav a.mobile-nav__link[href="/dataland-community"]').should('be.visible').click();
    cy.url().should('include', '/dataland-community');
    cy.get("a[href='/']").eq(0).click();

    // Close via close button
    cy.get('#mobile-menu-toggle').click();
    cy.get('#mobile-nav').should('be.visible');
    cy.get('#mobile-nav-close').click();
    cy.get('#mobile-nav').should('not.be.visible');

    checkLegalPage();
    checkImprintPage();
    checkDataPrivacyPage();
  });
});
