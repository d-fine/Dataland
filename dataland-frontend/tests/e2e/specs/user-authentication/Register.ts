import { login, logout } from '@e2e/utils/Auth';
import { authenticator } from 'otplib';
import { getStringCypressEnv } from '@e2e/utils/Cypress';
import { isString } from '@/utils/TypeScriptUtils';

describe('As a user I want to be able to register for an account and be able to log in and out of that account', () => {
  const email = `test_user${Date.now()}@example.com`;
  const firstName = 'Dummy';
  const lastName = 'User';
  const passwordBytes = crypto.getRandomValues(new Uint32Array(8));
  const randomHexPassword = [...passwordBytes].map((x): string => x.toString(16).padStart(2, '0')).join('');

  it('Checks that the Dataland password-policy gets respected', () => {
    cy.visitAndCheckAppMount('/').get("button[name='signup_dataland_button']").click();
    cy.get('#email').should('exist').type(email, { force: true });

    const typePasswordAndExpectError = (password: string, errorMessageSubstring: string): void => {
      cy.get('#password').should('exist').clear();
      cy.get('#password').type(password);
      cy.get("input[type='submit']").should('exist').click();
      cy.get('div[data-role=password-primary] span.input-error')
        .should('be.visible')
        .should('contain.text', errorMessageSubstring);
    };

    typePasswordAndExpectError('abc', 'at least 12 characters');
    typePasswordAndExpectError(
      'PasswordPasswordPassword',
      'Repeated character patterns like "abcabcabc" are easy to guess'
    );
    typePasswordAndExpectError('qwerty123456', 'This is a commonly used password');
    typePasswordAndExpectError('a'.repeat(200), 'at most 128 characters');
  });

  it('Checks that registering works', () => {
    cy.task('setEmail', email);
    cy.task('setPassword', randomHexPassword);
    cy.visitAndCheckAppMount('/').get("button[name='signup_dataland_button']").click();
    cy.get('#email').should('exist').type(email, { force: true });
    cy.get('#firstName').should('exist').type(firstName, { force: true });
    cy.get('#lastName').should('exist').type(lastName, { force: true });
    cy.get('#password').should('exist').type(randomHexPassword, { force: true });
    cy.get('#password-confirm').should('exist').type(randomHexPassword, { force: true });

    cy.get("input[type='submit']").should('exist').click();

    cy.get('#accept_terms').should('exist').click();
    cy.get('#accept_privacy').should('exist').click();
    cy.get("button[name='accept_button']").should('exist').click();

    cy.get('h1').should('contain', 'Email verification');
  });

  it('Checks that the admin console is working and a newly registered user can be verified', () => {
    cy.task('getEmail').then((returnEmail) => {
      if (!isString(returnEmail)) {
        throw new Error('Email retrieved by task is not a string. Cannot proceed.');
      }
      cy.visit('http://dataland-admin:6789/keycloak/admin/master/console/#/datalandsecurity/users');
      cy.get('h1').should('exist').should('contain', 'Sign in to your account');
      cy.url().should('contain', 'realms/master');
      cy.get('#username').should('exist').type(getStringCypressEnv('KEYCLOAK_ADMIN'), { force: true });
      cy.get('#password').should('exist').type(getStringCypressEnv('KEYCLOAK_ADMIN_PASSWORD'), { force: true });
      cy.get('#kc-login').should('exist').click();
      cy.intercept('GET', '/keycloak/admin/realms/datalandsecurity/ui-ext/*example.com').as('typedUsernameInSearch');
      cy.get('input')
        .should('have.class', 'pf-c-text-input-group__text-input')
        .type(`${returnEmail}{enter}`, { force: true });
      cy.wait('@typedUsernameInSearch');
      cy.get('table');
      cy.intercept('GET', '/keycloak/admin/realms/datalandsecurity/users/*rue').as('openedDummyUserProfile');
      cy.contains('a', returnEmail).click();
      cy.wait('@openedDummyUserProfile');
      cy.intercept('GET', 'keycloak/admin/realms/datalandsecurity/users/*userProfileMetadata=true').as(
        'savedUserProfileSettings'
      );
      cy.get('input[id="kc-user-email-verified"]').click({ force: true });
      cy.get('button[data-testid="save-user"]').click({ force: true });
      cy.wait('@savedUserProfileSettings');
    });
  });
  it('Checks that one can login to the newly registered account', () => {
    cy.visit('/');
    cy.task('getEmail').then((returnEmail) => {
      cy.task('getPassword').then((returnPassword) => {
        if (!isString(returnEmail) || !isString(returnPassword)) {
          throw new Error('Email or password retrieved by task is not a string. Cannot proceed.');
        }
        login(returnEmail, returnPassword);
      });
    });
    logout();
  });

  describe('Checks that TOTP-Based 2FA works', () => {
    it('Should be possible to setup 2FA on the newly created account', () => {
      cy.task('getEmail').then((returnEmail) => {
        cy.task('getPassword').then((returnPassword) => {
          if (!isString(returnEmail) || !isString(returnPassword)) {
            throw new Error('Email or password retrieved by task is not a string. Cannot proceed.');
          }
          login(returnEmail, returnPassword);
          cy.visitAndCheckAppMount('/companies');
          cy.get("div[id='profile-picture-dropdown-toggle']").click();
          cy.get("a[id='profile-picture-dropdown-settings-button']").click();
          // eslint-disable-next-line cypress/no-unnecessary-waiting
          cy.wait(100);
          cy.get("button:contains('Account security')").should('exist').click();
          cy.get("a:contains('Signing in')").should('exist').click();
          cy.get("button:contains('Set up Authenticator application')")
            .should('be.visible', { timeout: Cypress.env('medium_timeout_in_ms') as number })
            .click();
          cy.get("a:contains('Unable to scan')")
            .should('be.visible', { timeout: Cypress.env('short_timeout_in_ms') as number })
            .click();
          cy.get("span[id='kc-totp-secret-key']")
            .should('be.visible', { timeout: Cypress.env('short_timeout_in_ms') as number })
            .invoke('text')
            .then((text) => {
              const totpKey = text.replace(/\s/g, '');
              cy.get("input[id='totp']").type(authenticator.generate(totpKey));
              cy.get("input[id='saveTOTPBtn']").click();
              cy.get(`button:contains('${firstName} ${lastName}')`).click();
              cy.get("a:contains('Sign out')").should('exist', {
                timeout: Cypress.env('medium_timeout_in_ms') as number,
              });
              cy.task('setTotpKey', totpKey);
            });
        });
      });
    });
    it('Should be possible to login to the account with 2FA enabled', () => {
      cy.task('getEmail').then((returnEmail) => {
        cy.task('getPassword').then((returnPassword) => {
          cy.task('getTotpKey').then((key) => {
            if (!isString(returnEmail) || !isString(returnPassword) || !isString(key)) {
              throw new Error('Email or password or TOTP key retrieved by task is not a string. Cannot proceed.');
            }

            login(returnEmail, returnPassword, () => {
              return authenticator.generate(key);
            });
          });
        });
      });
    });
  });
});
