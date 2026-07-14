import '@cypress/code-coverage/support';
import './Commands';
import {
  interceptAllAndCheckFor500Errors,
  interceptAllDataPostsAndBypassQaIfPossible,
} from '@e2e/utils/GeneralApiUtils';

const cookieConsentValue =
  '{"stamp":"ywkY3Xqqc7BU3ejitK/n5QHNOWOCUdobQFyL+wVWcgRXqNVgJ9XNXQ==","necessary":true,"preferences":true,"statistics":true,method:"explicit","marketing":true,"ver":1,utc:1776958428355,"region":"de"}';

beforeEach(() => {
  if (Cypress.expose('EXECUTION_ENVIRONMENT') !== 'developmentLocal') {
    const appDomain = new URL(Cypress.config('baseUrl') as string).hostname;
    cy.setCookie('CookieConsent', cookieConsentValue, { domain: appDomain });
  }
  interceptAllAndCheckFor500Errors();
  if (!Cypress.expose('excludeBypassQaIntercept')) {
    interceptAllDataPostsAndBypassQaIfPossible();
  }
});
