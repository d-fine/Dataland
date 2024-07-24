import '@cypress/code-coverage/support';
import './Commands';
import {
  interceptAllAndCheckFor500Errors,
  interceptAllDataPostsAndBypassQaIfPossible,
} from '@e2e/utils/GeneralApiUtils';

beforeEach(() => {
  interceptAllAndCheckFor500Errors();
  if (!Cypress.env('excludeBypassQaIntercept')) {
    interceptAllDataPostsAndBypassQaIfPossible();
  }
});
