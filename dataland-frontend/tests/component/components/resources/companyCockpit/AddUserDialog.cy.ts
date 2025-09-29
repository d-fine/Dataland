import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import AddMemberDialog from '@/components/resources/companyCockpit/AddUserDialog.vue';

/**
 * Helper to add a user by email
 * @param email - Email of the user to add
 */
function addUser(email: string): void {
  cy.get('.search-input').clear().type(email);
  cy.get('[data-test="select-user-button"]').click();
}

/**
 * Helper to remove the first user in the list
 */
function removeUser(): void {
  cy.get('[data-test="remove-selected-user-button"]').first().click();
}

/**
 * Helper to save changes
 */
function saveChanges(): void {
  cy.get('[data-test="save-changes-button"]').click();
}

/**
 * Selects the first suggested user in the Listbox and checks that the 'Selected' tag is visible.
 */
function selectFirstSuggestedUserAndCheckSuggestedTag(): void {
  cy.get('[data-test="suggested-user-row"]')
    .first()
    .within(() => {
      cy.get('[data-test="select-suggested-user-button"]').click();
      cy.get('[data-test="selected-tag"]').should('be.visible');
    });
}

describe('AddMemberDialog Component Tests', function () {
  const existingUsers = [{ userId: '1', email: 'existing@test.com', firstName: 'Existing', lastName: 'User' }];

  beforeEach(function () {
    cy.intercept('POST', '**/emails/validation', (req) => {
      const testUsers: Record<string, { statusCode: number; body: unknown }> = {
        'invalid@test.com': {
          statusCode: 400,
          body: { errors: [{ message: 'There is no registered Dataland user with this email address.' }] },
        },
        'existing@test.com': {
          statusCode: 200,
          body: { id: '1', firstName: 'Existing', lastName: 'User', email: 'existing@test.com' },
        },
        'john@doe.com': {
          statusCode: 200,
          body: { id: '2', firstName: 'John', lastName: 'Doe', email: 'john@doe.com' },
        },
        'jane@doe.com': {
          statusCode: 200,
          body: { id: '3', firstName: 'Jane', lastName: 'Doe', email: 'jane@doe.com' },
        },
        'onlyemail@test.com': { statusCode: 200, body: { id: '4', email: 'onlyemail@test.com' } },
        'emptynames@test.com': {
          statusCode: 200,
          body: { id: '5', firstName: '', lastName: '', email: 'emptynames@test.com' },
        },
        'multiword@test.com': {
          statusCode: 200,
          body: { id: '6', firstName: 'Mary Jane', lastName: 'Watson Smith', email: 'multiword@test.com' },
        },
        'singlename@test.com': {
          statusCode: 200,
          body: { id: '7', firstName: 'NoLastName', lastName: '', email: 'singlename@test.com' },
        },
      };

      const response = testUsers[req.body.email as string] || {
        statusCode: 400,
        body: { errors: [{ message: 'Unknown email' }] },
      };

      req.reply(response);
    });

    cy.intercept('POST', '**/company-role-assignments/*/*/*', { statusCode: 200 }).as('assignRole');

    cy.intercept('GET', '/community/emails/company-123/recommended-users', {
      statusCode: 200,
      body: [
        { id: '3', firstName: 'Jane', lastName: 'Doe', email: 'jane@doe.com' },
        { id: '2', firstName: 'John', lastName: 'Doe', email: 'john@doe.com' },
      ],
    }).as('getRecommendedUsers');

    // @ts-ignore
    cy.mountWithPlugins(AddMemberDialog, {
      props: {
        companyId: 'company-123',
        role: 'admin',
        existingUsers,
      },
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: {
          dialogRef: {
            value: {
              close: cy.stub().as('dialogClose'),
            },
          },
          getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})),
        },
      },
    });
  });

  describe('Initial State and Basic Functionality', function () {
    it('renders empty state and handles basic user operations', function () {
      cy.contains('No users selected').should('be.visible');
      cy.get('[data-test="user-count-tag"]').should('contain', '0 Users');

      addUser('john@doe.com');
      cy.contains('John Doe').should('be.visible');
      cy.contains('john@doe.com').should('be.visible');
      cy.get('[data-test="user-count-tag"]').should('contain', '1 User');

      removeUser();
      cy.contains('No users selected').should('be.visible');
      cy.get('[data-test="user-count-tag"]').should('contain', '0 Users');
    });

    it('handles multiple users and clears input after addition', function () {
      const emails = ['john@doe.com', 'jane@doe.com'];

      for (const email of emails) {
        addUser(email);
      }

      cy.contains('John Doe').should('be.visible');
      cy.contains('Jane Doe').should('be.visible');
      cy.get('[data-test="user-count-tag"]').should('contain', '2 Users');
      cy.get('.search-input').should('have.value', '');
    });
  });

  describe('Input Validation and Error Handling', function () {
    it('handles invalid inputs correctly', function () {
      addUser('invalid@test.com');
      cy.get('[data-test="unknown-user-error"]').should(
        'contain',
        'There is no registered Dataland user with this email address.'
      );
    });

    it('prevents duplicate user selection', function () {
      addUser('existing@test.com');
      cy.get('[data-test="unknown-user-error"]').should('contain', 'already been selected');
      addUser('john@doe.com');
      addUser('john@doe.com');
      cy.get('[data-test="unknown-user-error"]').should('contain', 'already been selected');
    });

    it('clears error message on new validation attempt', function () {
      addUser('invalid@test.com');
      cy.get('[data-test="unknown-user-error"]').should('be.visible');

      addUser('invalid2@test.com');
      cy.get('[data-test="unknown-user-error"]').should('contain', 'Unknown email');
    });
  });

  describe('User Initials Generation', function () {
    const initialTests = [
      { email: 'multiword@test.com', expected: 'MJWS', description: 'multi-word names' },
      { email: 'singlename@test.com', expected: 'N', description: 'single name' },
      { email: 'onlyemail@test.com', expected: 'O', description: 'email when no names provided' },
      { email: 'emptynames@test.com', expected: 'E', description: 'email when names are empty strings' },
    ];

    for (const { email, expected, description } of initialTests) {
      it(`generates initials from ${description}`, function () {
        addUser(email);
        cy.get('[data-test="selected-users-listbox"]').find('[class*="p-tag"]').first().should('contain', expected);
      });
    }
  });

  describe('Save Operations and API Integration', function () {
    it('handles save operations with API calls', function () {
      addUser('john@doe.com');
      addUser('jane@doe.com');
      saveChanges();

      cy.wait('@assignRole');
      cy.get('@assignRole.all').should('have.length', 2);
    });
  });

  describe('Suggested Users Listbox Functionality', function () {
    it('renders suggested users in the Listbox', function () {
      cy.get('[data-test="suggestion-listbox"]').should('exist');
      cy.get('[data-test="suggestion-listbox"]').contains('Jane Doe').should('be.visible');
      cy.get('[data-test="suggestion-listbox"]').contains('John Doe').should('be.visible');
    });

    it('allows selecting a suggested user', function () {
      cy.get('[data-test="selected-user-row"]').should('not.exist');
      selectFirstSuggestedUserAndCheckSuggestedTag();
      cy.get('[data-test="selected-user-row"]').should('contain', 'Jane Doe');
    });

    it('shows the Selected tag and Remove button for already selected users', function () {
      selectFirstSuggestedUserAndCheckSuggestedTag();
      cy.get('[data-test="selected-user-row"]')
        .first()
        .within(() => {
          cy.get('[data-test="remove-selected-user-button"]').click();
        });
    });

    it('removes a selected user from the Listbox, the Select button should appear again in the list of suggested users', function () {
      selectFirstSuggestedUserAndCheckSuggestedTag();
      cy.get('[data-test="selected-user-row"]')
        .first()
        .within(() => {
          cy.get('[data-test="remove-selected-user-button"]').click();
        });
      cy.get('[data-test="suggested-user-row"]')
        .first()
        .within(() => {
          cy.get('[data-test="selected-tag"]').should('not.exist');
          cy.get('[data-test="select-suggested-user-button"]').should('be.visible');
        });
    });
  });
});
