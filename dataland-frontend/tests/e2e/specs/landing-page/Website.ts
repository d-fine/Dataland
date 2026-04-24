import { checkFooter } from '@sharedUtils/ElementChecks.ts';
const cookieConsentValue =
  '{"stamp":"ywkY3Xqqc7BU3ejitK/n5QHNOWOCUdobQFyL+wVWcgRXqNVgJ9XNXQ==","necessary":true,"preferences":true,"statistics":true,method:"explicit","marketing":true,"ver":1,utc:1776958428355,"region":"de"}';

describe('Check that the website works properly', () => {
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

      cy.get("nav a[href='/product']:visible").should('exist').click();
      cy.url().should('include', '/product');
      cy.get("a[href='/']").eq(0).click();

      cy.get("nav a[href='/dataland-community']:visible").should('exist').click();
      cy.url().should('include', '/dataland-community');
      cy.get("a[href='/']").eq(0).click();

      cy.get("a[href='/legal']").click();
      cy.get("h1:contains('General terms and conditions for participation in Dataland')");
      cy.get("[data-test='terms-language-toggle-button']").should('exist').click();
      cy.get("h1:contains('Allgemeine Bedingungen für die Teilnahme an Dataland')");
      cy.get("[data-test='terms-language-toggle-button']").should('exist').click();
      cy.get("h1:contains('General terms and conditions for participation in Dataland')");
      cy.get("a[href='/']").eq(0).click();

      cy.get("a[href='/imprint']").click();
      cy.get("h2:contains('Impressum')");
      cy.get("a[href='/']").eq(0).click();

      cy.get("a[href='/dataprivacy']").click();
      cy.get("h1:contains('Datenschutzhinweise')");
      cy.get("a[href='/']").eq(0).click();
    });
  });
});
