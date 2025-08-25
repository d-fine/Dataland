import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import AddMemberDialog from '@/components/resources/companyCockpit/AddUserDialog.vue';

describe('AddMemberDialog Component Tests', function () {
  const existingUsers = [{ userId: '1', email: 'existing@test.com', name: 'Existing User', initials: 'EU' }];

  beforeEach(function () {
    // @ts-ignore
    cy.mountWithPlugins(AddMemberDialog, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: {
          dialogRef: {
            value: {
              data: {
                companyId: 'company-123',
                role: 'admin',
                existingUsers,
              },
              close: cy.stub().as('dialogClose'),
            },
          },
          getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})),
        },
      },
    });

    cy.intercept('POST', '**/user-validation/email', (req) => {
      const email = req.body.email;

      switch (email) {
        case 'invalid@test.com':
          req.reply({
            statusCode: 400,
            body: { errors: [{ message: 'There is no registered Dataland user with this email address.' }] },
          });
          break;
        case 'existing@test.com':
          req.reply({
            statusCode: 200,
            body: { id: '1', firstName: 'Existing', lastName: 'User', email },
          });
          break;
        case 'john@doe.com':
          req.reply({
            statusCode: 200,
            body: { id: '2', firstName: 'John', lastName: 'Doe', email },
          });
          break;
        case 'jane@doe.com':
          req.reply({
            statusCode: 200,
            body: { id: '3', firstName: 'Jane', lastName: 'Doe', email },
          });
          break;
        default:
          req.reply({
            statusCode: 400,
            body: { errors: [{ message: 'Unknown email' }] },
          });
      }
    });
  });

  it('renders empty state initially', function () {
    cy.contains('No users selected').should('be.visible');
    cy.get('[data-test="user-count-tag"]').should('contain', '0 Users');
  });

  it('shows error when API rejects validation', function () {
    cy.get('.search-input').type('invalid@test.com');
    cy.get('[data-test="select-user-button"]').click();

    cy.get('[data-test="unknown-user-error"]').should(
      'contain',
      'There is no registered Dataland user with this email address.'
    );
  });

  it('adds a valid user with first and last name', function () {
    cy.get('.search-input').type('john@doe.com');
    cy.get('[data-test="select-user-button"]').click();

    cy.contains('John Doe').should('be.visible');
    cy.contains('john@doe.com').should('be.visible');
    cy.get('[data-test="user-count-tag"]').should('contain', '1 User');
  });

  it('does not add duplicate users', function () {
    cy.get('.search-input').type('existing@test.com');
    cy.get('[data-test="select-user-button"]').click();

    cy.get('[data-test="unknown-user-error"]').should('contain', 'already been selected');
  });

  it('removes a user', function () {
    cy.get('.search-input').type('john@doe.com');
    cy.get('[data-test="select-user-button"]').click();

    cy.contains('John Doe').parent().find('button').click();
    cy.contains('No users selected').should('be.visible');
    cy.get('[data-test="user-count-tag"]').should('contain', '0 Users');
  });

  it('adds multiple valid users', function () {
    const emails = ['john@doe.com', 'jane@doe.com'];

    emails.forEach((email) => {
      cy.get('.search-input').clear().type(email);
      cy.get('[data-test="select-user-button"]').click();
    });

    cy.contains('John Doe').should('be.visible');
    cy.contains('Jane Doe').should('be.visible');
    cy.get('[data-test="user-count-tag"]').should('contain', '2 Users');
  });

  it('calls API and closes dialog on save', function () {
    cy.intercept('POST', '**/company-role-assignments/*/*/*', { statusCode: 200 }).as('assignRole');

    cy.get('.search-input').type('john@doe.com');
    cy.get('[data-test="select-user-button"]').click();

    cy.get('[data-test="save-changes-button"]').click();
    cy.wait('@assignRole');
    cy.get('@dialogClose').should('have.been.called');
  });
});
