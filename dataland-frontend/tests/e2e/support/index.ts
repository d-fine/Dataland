import '@cypress/code-coverage/support';
import './Commands';
import 'cypress-wait-until';
import {
  interceptAllAndCheckFor500Errors,
  interceptAllDataPostsAndBypassQaIfPossible,
} from '@e2e/utils/GeneralApiUtils';

// Global exception map for different test contexts
const globalExceptionPatterns: Record<string, string[]> = {
  grafana: [
    "Cannot read properties of undefined (reading 'keys')",
    'Datasource grafanacloud-logs was not found',
    'Error: Datasource grafanacloud-logs was not found',
    'An unknown error has occurred: [object Object]',
  ],
};

let currentExceptionContext: string | null = null;

// Setup of global exception handler
Cypress.on('uncaught:exception', (err: Error): boolean => {
  if (currentExceptionContext && globalExceptionPatterns[currentExceptionContext]) {
    const patterns = globalExceptionPatterns[currentExceptionContext];
    const shouldIgnore = patterns.some(pattern => err.message.includes(pattern));
    if (shouldIgnore) {
      console.log(`Ignoring exception in context ${currentExceptionContext}: ${err.message}`);
      return false;
    }
  }
  return true;  // all other exceptions are not ignored
});

Cypress.Commands.add('setExceptionContext', (context: string | null) => {
  currentExceptionContext = context;
});

beforeEach(() => {
  interceptAllAndCheckFor500Errors();
  if (!Cypress.env('excludeBypassQaIntercept')) {
    interceptAllDataPostsAndBypassQaIfPossible();
  }
});
