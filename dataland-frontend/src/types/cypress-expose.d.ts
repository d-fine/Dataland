/// <reference types="cypress" />

declare namespace Cypress {
  /**
   * Read an exposed environment/config value provided via `cypress.config.ts` expose.
   */
  function expose<T = unknown>(key: string): T | undefined;

  /**
   * Optionally set an exposed value (used in some tests).
   */
  function expose<T = unknown>(key: string, value: T): void;
}

