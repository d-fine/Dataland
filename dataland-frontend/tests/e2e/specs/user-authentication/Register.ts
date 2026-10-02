import { login, logout } from '@e2e/utils/Auth';
import { getStringCypressEnv } from '@e2e/utils/Cypress';
import { isString } from '@/utils/TypeScriptUtils';
import { generate } from 'otplib';

/**
 * Types the value of the given Cypress environment variable into the input field with the given selector.
 * @param envName name of the Cypress environment variable
 * @param selector selector of the input field
 */
function typeCypressEnvValueIntoField(envName: string, selector: string): void {
  getStringCypressEnv(envName).then((value) => {
    cy.get(selector).should('exist').type(value, { force: true });
  });
}

describe('As a user I want to be able to register for an account and be able to log in and out of that account', () => {
  const email = `test_user${Date.now()}@example.com`;
  const firstName = 'Dummy';
  const lastName = 'User';
  const passwordBytes = crypto.getRandomValues(new Uint32Array(8));
  const randomHexPassword = [...passwordBytes].map((x): string => x.toString(16).padStart(2, '0')).join('');

  const mediumTimeoutInMs = Number(Cypress.expose('medium_timeout_in_ms') ?? 30000);
  const shortTimeoutInMs = Number(Cypress.expose('short_timeout_in_ms') ?? 10000);

  /**
   * Reads the TOTP secret key from the Keycloak account page, confirms it with a generated token,
   * completes the setup and stores the key via a Cypress task.
   */
  function enterTotpKeyAndSaveIt(): void {
    let totpKey: string;
    cy.get("span[id='kc-totp-secret-key']")
      .should('be.visible', { timeout: shortTimeoutInMs })
      .invoke('text')
      .then((text) => {
        totpKey = text.replaceAll(/\s/g, '');
        return cy.wrap(generate({ secret: totpKey }));
      })
      .then((token) => {
        cy.get("input[id='totp']").type(token as string);
        cy.get("input[id='saveTOTPBtn']").click();
        cy.get(`button:contains('${firstName} ${lastName}')`).click();
        cy.get("span:contains('Sign out')").should('exist', {
          timeout: mediumTimeoutInMs,
        });
        cy.task('setTotpKey', totpKey);
      });
  }

  it('Checks that the Dataland password-policy gets respected', () => {
    cy.visitAndCheckAppMount('/').get("[data-test='signup-dataland-button']").click();
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
    cy.visitAndCheckAppMount('/').get("[data-test='signup-dataland-button']").click();
    cy.get('#email').should('exist').type(email, { force: true });
    cy.get('#firstName').should('exist').type(firstName, { force: true });
    cy.get('#lastName').should('exist').type(lastName, { force: true });
    cy.get('#password').should('exist').type(randomHexPassword, { force: true });
    cy.get('#password-confirm').should('exist').type(randomHexPassword, { force: true });

    cy.get("input[type='submit']").should('exist').click();
    cy.get('#accept_terms', { timeout: 60000 }).should('be.visible').click();
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
      typeCypressEnvValueIntoField('KC_BOOTSTRAP_ADMIN_USERNAME', '#username');
      typeCypressEnvValueIntoField('KC_BOOTSTRAP_ADMIN_PASSWORD', '#password');
      cy.get('#kc-login').should('exist').click();
      cy.intercept('GET', '/keycloak/admin/realms/datalandsecurity/ui-ext/*example.com').as('typedUsernameInSearch');
      cy.get('input.pf-v5-c-text-input-group__text-input').type(`${returnEmail}{enter}`, { force: true });
      cy.wait('@typedUsernameInSearch');
      cy.get('table');
      cy.intercept('GET', '/keycloak/admin/realms/datalandsecurity/users/*rue').as('openedDummyUserProfile');
      cy.contains('a', returnEmail).click();
      cy.wait('@openedDummyUserProfile');
      cy.intercept('GET', 'keycloak/admin/realms/datalandsecurity/users/*userProfileMetadata=true').as(
        'savedUserProfileSettings'
      );
      cy.get('input[id="emailVerified"]').click({ force: true });
      cy.get('button[data-testid="user-creation-save"]').click({ force: true });
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
          cy.get("[data-test='user-profile-toggle']").click();
          cy.get('.p-menu-item-link').contains('USER SETTINGS').click();
          // eslint-disable-next-line cypress/no-unnecessary-waiting
          cy.wait(100);
          cy.get("button:contains('Account security')").should('exist').click();
          cy.get("a:contains('Signing in')").should('exist').click();
          cy.get("button:contains('Set up Authenticator application')")
            .should('be.visible', { timeout: mediumTimeoutInMs })
            .click();
          cy.get("a:contains('Unable to scan')").should('be.visible', { timeout: shortTimeoutInMs }).click();
          enterTotpKeyAndSaveIt();
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

            cy.wait(mediumTimeoutInMs);

            login(returnEmail, returnPassword, () => {
              return generate({ secret: key });
            });
          });
        });
      });
    });
  });
});
