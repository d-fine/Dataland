import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import AddMemberDialog from '@/components/resources/companyCockpit/AddUserDialog.vue';

describe('AddMemberDialog Component Tests', function () {
  const existingUsers = [
    { userId: '1', email: 'existing@test.com', name: 'Existing User', initials: 'EU' },
  ];

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
                role: 'ADMIN',
                existingUsers,
              },
              close: cy.stub().as('dialogClose'),
            },
          },
          getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})),
        },
      },
    });
  });

  it('should render empty state initially', function () {
    cy.contains('No users selected').should('be.visible');
    cy.get('[data-test="user-count-tag"]').should('contain', '0 Users');
  });

  it('should show error when API rejects validation', function () {
    cy.intercept('POST', '**/userValidationController/emailAddressValidation', {
      statusCode: 400,
      body: { errors: [{ message: 'Invalid email' }] },
    });

    cy.get('.search-input').type('invalid@test.com');
    cy.get('[data-test="select-user-button"]').click();

    cy.get('[data-test="unknown-user-error"]').should('contain', 'Invalid email');
  });

  it('should add a valid user after validation', function () {
    cy.intercept('POST', '**/userValidationController/emailAddressValidation', {
      statusCode: 200,
      body: { id: '2', firstName: 'John', lastName: 'Doe', email: 'john@doe.com' },
    });

    cy.get('.search-input').type('john@doe.com');
    cy.get('[data-test="select-user-button"]').click();

    cy.contains('John Doe').should('be.visible');
    cy.contains('john@doe.com').should('be.visible');
    cy.get('[data-test="user-count-tag"]').should('contain', '1 User');
  });

  it('should not add duplicate users', function () {
    cy.intercept('POST', '**/userValidationController/emailAddressValidation', {
      statusCode: 200,
      body: { id: '1', firstName: 'Existing', lastName: 'User', email: 'existing@test.com' },
    });

    cy.get('.search-input').type('existing@test.com');
    cy.get('[data-test="select-user-button"]').click();

    cy.get('[data-test="unknown-user-error"]').should('contain', 'already been selected');
  });

  it('should remove a user', function () {
    cy.intercept('POST', '**/userValidationController/emailAddressValidation', {
      statusCode: 200,
      body: { id: '2', firstName: 'John', lastName: 'Doe', email: 'john@doe.com' },
    });

    cy.get('.search-input').type('john@doe.com');
    cy.get('[data-test="select-user-button"]').click();

    cy.contains('John Doe').parent().find('button').click();
    cy.contains('No users selected').should('be.visible');
    cy.get('[data-test="user-count-tag"]').should('contain', '0 Users');
  });

  it('should call API and close dialog on save', function () {
    cy.intercept('POST', '**/userValidationController/emailAddressValidation', {
      statusCode: 200,
      body: { id: '2', firstName: 'John', lastName: 'Doe', email: 'john@doe.com' },
    });
    cy.intercept('POST', '**/companyRolesController/assignCompanyRole', {
      statusCode: 200,
    }).as('assignRole');

    cy.get('.search-input').type('john@doe.com');
    cy.get('[data-test="select-user-button"]').click();

    cy.get('[data-test="save-changes-button"]').click();
    cy.wait('@assignRole');
    cy.get('@dialogClose').should('have.been.called');
  });
});
