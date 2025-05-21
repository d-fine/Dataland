import { ensureLoggedIn, getKeycloakToken } from '@e2e/utils/Auth';
import { browserThen } from '@e2e/utils/Cypress';

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Cypress {
    interface Chainable {
      visitAndCheckAppMount: typeof visitAndCheckAppMount;
      closeCookieBannerIfItExists: typeof closeCookieBannerIfItExists;
      ensureLoggedIn: typeof ensureLoggedIn;
      getKeycloakToken: typeof getKeycloakToken;
      browserThen: typeof browserThen;

      setExceptionContext(context: string | null): void;

      visitAndCheckExternalAdminPage(options: {
        url: string;
        interceptPattern?: string;
        elementSelector?: string;
        containsText?: string;
        urlShouldInclude?: string;
        method?: string;
        timeoutInMs?: number;
        ignoreExceptions?: string[];
      }): Chainable<JQuery<HTMLElement>>;

      waitForPageLoad(options: {
        urlShouldInclude?: string;
        elementSelectors?: string[];
        containsText?: string;
        customCheck?: (bodyElement: JQuery<HTMLElement>) => boolean;
        timeout?: number;
        interval?: number;
        errorMsg?: string;
      }): Cypress.Chainable<boolean>;
    }
  }
}

/**
 * Visits a given external admin page URL and verifies that it has loaded successfully, e. g. the Vue #app component exists
 * @param endpoint the endpoint to navigate to via URL
 * @returns Cypress chainable object pointing to the `<body>` element after successful page load and checks
 */
export function visitAndCheckAppMount(endpoint: string): Cypress.Chainable<JQuery> {
  cy.visit(endpoint);
  cy.get('#app', { timeout: Cypress.env('long_timeout_in_ms') as number }).should('exist');
  closeCookieBannerIfItExists();
  return cy.get('#app');
}

/**
 * Visits an external admin page and ensures it's fully loaded before proceeding
 * @param options Configuration options for visiting the external admin page
 * @returns the cypress chainable
 */
export function visitAndCheckExternalAdminPage(options: {
  /** URL of the external admin page to visit */
  url: string;
  /** Optional pattern to intercept */
  interceptPattern?: string;
  /** Optional CSS selector to verify existence before proceeding */
  elementSelector?: string;
  /** Optional text content to verify on the page */
  containsText?: string;
  /** Optional URL path to verify */
  urlShouldInclude?: string;
  /** Optional HTTP method for the intercept, defaults to 'GET' */
  method?: string;
  /** Optional timeout in milliseconds, defaults to Cypress environment variable */
  timeoutInMs?: number;
  /** Optional array of error message patterns to ignore during page load */
  ignoreExceptions?: string[];
}): Cypress.Chainable<JQuery<HTMLElement>> {
  const {
    url,
    interceptPattern,
    elementSelector,
    containsText,
    urlShouldInclude,
    method = 'GET',
    timeoutInMs = Cypress.env('long_timeout_in_ms'),
    ignoreExceptions = [],
  } = options;

  // Setup exception handler if needed
  if (ignoreExceptions.length > 0) {
    const exceptionHandler = (err: Error): boolean => {
      const shouldIgnore = ignoreExceptions.some((pattern) => err.message.includes(pattern));
      return !shouldIgnore;
    };
    Cypress.on('uncaught:exception', exceptionHandler);
    cy.once('window:load', () => {
      Cypress.off('uncaught:exception', exceptionHandler);
    });
  }

  // Set up intercept only if a pattern is provided
  let uniqueAlias: string | undefined;
  if (interceptPattern) {
    // Generate a unique alias based on the URL and a timestamp
    const urlPart = url.replace(/[^a-zA-Z0-9]/g, '').substring(0, 10);
    uniqueAlias = `intercept_${urlPart}_${Date.now()}`;
    cy.intercept({ method, url: interceptPattern }).as(uniqueAlias);
  }

  cy.visit(url);

  // Wait for the intercept only if it was set up
  if (uniqueAlias) {
    cy.wait(`@${uniqueAlias}`, { timeout: timeoutInMs });
  }

  // Basic check - make sure the body is visible
  cy.get('body', { timeout: timeoutInMs }).should('be.visible');

  // Additional checks based on provided options
  if (urlShouldInclude) {
    cy.url().should('include', urlShouldInclude);
  }
  if (elementSelector) {
    cy.get(elementSelector, { timeout: timeoutInMs }).should('exist');
  }
  if (containsText) {
    cy.contains(containsText, { timeout: timeoutInMs }).should('exist');
  }

  return cy.get('body') as unknown as Cypress.Chainable<JQuery<HTMLElement>>;
}

/**
 * Waits until specified page elements, text, or custom conditions are met, optionally checking the URL.
 * @param options configuration options to define what to wait for
 * @returns Cypress chainable resolving to true when conditions are fulfilled
 */
export function waitForPageLoad(options: {
  /** URL path that should be included in the current URL */
  urlShouldInclude?: string;
  /** Array of element selectors to wait for (at least one must exist) */
  elementSelectors?: string[];
  /** Specific text content to verify on the page */
  containsText?: string;
  /** Function to evaluate the page content */
  customCheck?: (bodyElement: JQuery<HTMLElement>) => boolean;
  /** Timeout in milliseconds */
  timeout?: number;
  /** Polling interval in milliseconds */
  interval?: number;
  /** Custom error message if waiting fails */
  errorMsg?: string;
}): Cypress.Chainable<boolean> {
  const {
    urlShouldInclude,
    elementSelectors = [],
    containsText,
    customCheck,
    timeout = Cypress.env('long_timeout_in_ms'),
    interval = 1000,
    errorMsg = 'Page did not load expected elements within the timeout period',
  } = options;

  if (
    elementSelectors.length === 0 &&
    !containsText &&
    !customCheck
  ) {
    throw new Error('waitForPageLoad: At least one of elementSelectors, containsText, or customCheck must be provided');
  }

  // Check URL if needed
  if (urlShouldInclude) {
    cy.url().should('include', urlShouldInclude);
  }

  // Wait until any of the specified conditions are met
  return cy.waitUntil(
    () => {
      return cy.get('body').then(($body) => {
        const hasElements = elementSelectors.some((selector) => $body.find(selector).length > 0);
        const hasText = containsText ? $body.text().includes(containsText) : false;
        const customCheckResult = customCheck ? customCheck($body) : false;
        return hasElements || hasText || customCheckResult;
      });
    },
    {
      timeout,
      interval,
      errorMsg,
    }
  );
}

/**
 * Close the cookie banner if it exists and do nothing if it doesn't exist.
 */
function closeCookieBannerIfItExists(): void {
  cy.get('body').then(($body) => {
    const allowCookies = $body.find('#CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll');
    if (allowCookies.length == 1) {
      allowCookies[0].click();
    }
  });
}

Cypress.Commands.add('visitAndCheckAppMount', visitAndCheckAppMount);
Cypress.Commands.add('visitAndCheckExternalAdminPage', visitAndCheckExternalAdminPage);
Cypress.Commands.add('waitForPageLoad', waitForPageLoad);
Cypress.Commands.add('ensureLoggedIn', ensureLoggedIn);
Cypress.Commands.add('getKeycloakToken', getKeycloakToken);
Cypress.Commands.add('browserThen', browserThen);
