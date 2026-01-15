import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import SharePortfolioDialog from '@/components/resources/portfolio/SharePortfolioDialog.vue';

/**
 * Helper to add a user by email
 * @param email - Email of the user to add
 */
function addUser(email: string): void {
  cy.get('[data-test="email-input-field"]').clear().type(email);
  cy.get('[data-test="select-user-button"]').click();
}

describe('SharePortfolioDialog Component Tests', function () {
  const testPortfolioId = '550e8400-e29b-41d4-a716-446655440000';
  const existingUserId = '7c9e6679-7425-40de-944b-e07fc1f90ae7';
  const johnUserId = 'a3bb189e-8bf9-3888-9912-ace4e6543002';
  const janeUserId = 'b4cc290f-9cg0-4999-0023-bdf5f7654113';
  const ownerUserId = '1d4e3a2b-5f6c-7d8e-9f0a-1b2c3d4e5f6a';

  beforeEach(function () {
    cy.intercept('POST', '**/emails/validation', (req) => {
      const testUsers: Record<string, { statusCode: number; body: unknown }> = {
        'invalid@test.com': {
          statusCode: 400,
          body: { errors: [{ message: 'There is no registered Dataland user with this email address.' }] },
        },
        'john@doe.com': {
          statusCode: 200,
          body: { id: johnUserId, firstName: 'John', lastName: 'Doe', email: 'john@doe.com' },
        },
        'jane@doe.com': {
          statusCode: 200,
          body: { id: janeUserId, firstName: 'Jane', lastName: 'Doe', email: 'jane@doe.com' },
        },
      };

      const response = testUsers[req.body.email as string] || {
        statusCode: 400,
        body: { errors: [{ message: 'Unknown email' }] },
      };

      req.reply(response);
    });

    cy.intercept('GET', `**/portfolios/${testPortfolioId}/`, {
      statusCode: 200,
      body: {
        portfolioId: testPortfolioId,
        portfolioName: 'Test Portfolio',
        userId: ownerUserId,
        sharedUserIds: [existingUserId],
        identifiers: [],
        isMonitored: false,
        monitoredFrameworks: [],
        notificationFrequency: 'Weekly',
        creationTimestamp: Date.now(),
        lastUpdateTimestamp: Date.now(),
      },
    }).as('getPortfolio');

    cy.intercept('PATCH', `**/portfolios/${testPortfolioId}/sharing`, {
      statusCode: 200,
      body: {
        portfolioId: testPortfolioId,
        sharedUserIds: [existingUserId, johnUserId],
      },
    }).as('patchSharing');

    // @ts-ignore
    cy.mountWithPlugins(SharePortfolioDialog, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: {
          dialogRef: {
            value: {
              data: {
                portfolioId: testPortfolioId,
              },
              close: cy.stub().as('dialogClose'),
            },
          },
          getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})),
        },
      },
    });
  });

  it('displays existing shared users and user count', function () {
    cy.wait('@getPortfolio');
    cy.get('[data-test="users-with-access-listbox"]').should('be.visible');
    cy.get('[data-test="user-count-tag"]').should('contain', '1 User');
  });

  it('adds multiple users and updates count', function () {
    cy.wait('@getPortfolio');

    addUser('john@doe.com');
    cy.contains('John Doe').should('be.visible');
    cy.get('[data-test="email-input-field"]').should('have.value', '');
    cy.get('[data-test="user-count-tag"]').should('contain', '2 Users');

    addUser('jane@doe.com');
    cy.get('[data-test="user-count-tag"]').should('contain', '3 Users');
  });

  it('removes user and shows empty state', function () {
    cy.wait('@getPortfolio');

    cy.get('[data-test="remove-user-button"]').first().click();
    cy.contains('Portfolio not shared with anyone').should('be.visible');
    cy.get('[data-test="user-count-tag"]').should('contain', '0 Users');
  });

  it('displays validation errors for invalid and duplicate users', function () {
    cy.wait('@getPortfolio');

    addUser('invalid@test.com');
    cy.get('[data-test="error-message"]').should(
      'contain',
      'There is no registered Dataland user with this email address.'
    );

    addUser('john@doe.com');
    cy.get('[data-test="error-message"]').should('not.exist');

    addUser('john@doe.com');
    cy.get('[data-test="error-message"]').should('contain', 'already has access');
  });

  it('saves changes and closes dialog', function () {
    cy.wait('@getPortfolio');

    addUser('john@doe.com');
    cy.get('[data-test="save-changes-button"]').click();

    cy.wait('@patchSharing')
      .its('request.body')
      .should('deep.include', {
        sharedUserIds: [existingUserId, johnUserId],
      });
    cy.get('@dialogClose').should('have.been.called');
  });
});
