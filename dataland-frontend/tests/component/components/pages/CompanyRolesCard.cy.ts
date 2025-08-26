import CompanyRolesCard from '@/components/resources/companyCockpit/CompanyRolesCard.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { CompanyRole, type CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles';

describe('Component test for CompanyRolesCard', () => {
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
  const dummyUserId = 'mock-user-id';
  const dummyFirstName = 'John';
  const dummyLastName = 'Doe';
  const dummyEmail = 'john.doe@company.com';
  const anotherUserId = 'another-user-id';
  const anotherUserEmail = 'jane.smith@company.com';
  const anotherUserFirstName = 'Jane';
  const anotherUserLastName = 'Smith';

  /**
   * Generates a company role assignment for testing
   * @param companyRole the role to assign
   * @param companyId the company ID
   * @param userId the user ID (optional, defaults to dummyUserId)
   * @param email the email (optional, defaults to dummyEmail)
   * @param firstName the first name (optional, defaults to dummyFirstName)
   * @param lastName the last name (optional, defaults to dummyLastName)
   * @returns a mock company role assignment
   */
  function generateCompanyRoleAssignment(
    companyRole: CompanyRole,
    companyId: string,
    userId: string = dummyUserId,
    email: string = dummyEmail,
    firstName: string = dummyFirstName,
    lastName: string = dummyLastName
  ): CompanyRoleAssignmentExtended {
    return {
      companyRole,
      companyId,
      userId,
      firstName,
      lastName,
      email,
    };
  }

  /**
   * Mocks API requests for company role assignments
   * @param roleAssignments the role assignments to return
   */
  function mockCompanyRoleAssignments(roleAssignments: CompanyRoleAssignmentExtended[]): void {
    cy.intercept('GET', '**/community/company-role-assignments*', {
      statusCode: 200,
      body: roleAssignments,
    }).as('getRoleAssignments');

    cy.intercept('POST', '**/company-role-assignments/*/*/*', {
      statusCode: 200,
    }).as('assignRole');

    cy.intercept('DELETE', '**/company-role-assignments/*/*/*', {
      statusCode: 200,
    }).as('removeRole');
  }

  /**
   * Mounts the CompanyRolesCard component with specified parameters
   * @param role the company role to display
   * @param userRole the current user's role (optional)
   * @param isLoggedIn whether the user is logged in
   * @param keycloakRoles the user's Keycloak roles
   * @returns the mounted component
   */
  function mountCompanyRolesCard(
    role: CompanyRole,
    userRole?: CompanyRole | null,
    isLoggedIn: boolean = true,
    keycloakRoles: string[] = []
  ): Cypress.Chainable {
    return getMountingFunction({
      keycloak: minimalKeycloakMock({
        authenticated: isLoggedIn,
        roles: keycloakRoles,
        userId: dummyUserId,
      }),
    })(CompanyRolesCard, {
      props: {
        companyId: dummyCompanyId,
        role,
        userRole,
      },
    });
  }

  /**
   * Validates that the card displays the correct role information
   * @param expectedTitle the expected card title
   * @param expectedIcon the expected icon class
   */
  function validateCardHeader(expectedTitle: string, expectedIcon: string): void {
    cy.get('[data-test="company-roles-card"]').should('exist');
    cy.get('[data-test="company-roles-card"]').should('contain', expectedTitle);
    cy.get(`i.${expectedIcon.replace(' ', '.')}`).should('exist');
  }

  /**
   * Validates the info message functionality
   * @param expectedInfoText the expected info text content
   */
  function validateInfoMessage(expectedInfoText: string): void {
    cy.get('[data-test="info-message"]').should('contain', expectedInfoText);

    cy.get('[data-test="info-message"]').first().find('button').click();
    cy.get('[data-test="info-message"]').should('not.exist');

    cy.get('[data-test="info-icon"]').should('be.visible');

    cy.get('[data-test="info-icon"]').click();
    cy.get('[data-test="info-message"]').should('contain', expectedInfoText);
  }

  /**
   * Validates the user table contents
   * @param expectedUsers array of expected user data
   */
  function validateUserTable(expectedUsers: CompanyRoleAssignmentExtended[]): void {
    if (expectedUsers.length === 0) {
      cy.get('table').should('not.exist');
      return;
    }

    cy.get('table').should('exist');
    expectedUsers.forEach((user, index) => {
      cy.get('tbody tr')
        .eq(index)
        .within(() => {
          cy.get('td').eq(0).should('contain', user.firstName);
          cy.get('td').eq(1).should('contain', user.lastName);
          cy.get('td').eq(2).should('contain', user.email);
          cy.get('td').eq(3).should('contain', user.userId);
        });
    });
  }

  /**
   * Validates the Add User button visibility and functionality
   * @param shouldBeVisible whether the button should be visible
   * @param expectedLabel the expected button label
   */
  function validateAddUserButton(shouldBeVisible: boolean): void {
    if (shouldBeVisible) {
      cy.get('[data-test="add-user-button"]').should('exist');
    } else {
      cy.get('[data-test="add-user-button"]').should('not.exist');
    }
  }

  describe('Role Display Tests', () => {
    it('displays Company Owner role card correctly', () => {
      mockCompanyRoleAssignments([]);
      mountCompanyRolesCard(CompanyRole.CompanyOwner);
      cy.wait('@getRoleAssignments');

      validateCardHeader('Company Owners', 'pi pi-crown');
      validateInfoMessage('Company owners have the highest level of access and can add other users as company owners');
    });

    it('displays Member Admin role card correctly', () => {
      mockCompanyRoleAssignments([]);
      mountCompanyRolesCard(CompanyRole.MemberAdmin);
      cy.wait('@getRoleAssignments');

      validateCardHeader('Admins', 'pi pi-shield');
      validateInfoMessage('The User Admin has the rights to add or remove other user admins and members');
    });

    it('displays Member role card correctly', () => {
      mockCompanyRoleAssignments([]);
      mountCompanyRolesCard(CompanyRole.Member);
      cy.wait('@getRoleAssignments');

      validateCardHeader('Members', 'pi pi-users');
      validateInfoMessage('Members have the ability to request unlimited data');
    });

    it('displays Data Uploader role card correctly', () => {
      mockCompanyRoleAssignments([]);
      mountCompanyRolesCard(CompanyRole.DataUploader);
      cy.wait('@getRoleAssignments');

      validateCardHeader('Uploaders', 'pi pi-cloud-upload');
      validateInfoMessage('Uploaders have the responsibility of ensuring all relevant data is uploaded');
    });
  });

  describe('User Management Tests', () => {
    it('displays users in the role correctly', () => {
      const roleAssignments = [
        generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId),
        generateCompanyRoleAssignment(
          CompanyRole.Member,
          dummyCompanyId,
          anotherUserId,
          anotherUserEmail,
          anotherUserFirstName,
          anotherUserLastName
        ),
      ];

      mockCompanyRoleAssignments(roleAssignments);
      mountCompanyRolesCard(CompanyRole.Member, CompanyRole.CompanyOwner);
      cy.wait('@getRoleAssignments');

      validateUserTable(roleAssignments);
    });

    it('shows empty state when no users have the role', () => {
      mockCompanyRoleAssignments([]);
      mountCompanyRolesCard(CompanyRole.Member, CompanyRole.CompanyOwner);
      cy.wait('@getRoleAssignments');

      validateUserTable([]);
      validateAddUserButton(true);
    });

    it('shows Add User button for authorized users', () => {
      const roleAssignments = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
      mockCompanyRoleAssignments(roleAssignments);
      mountCompanyRolesCard(CompanyRole.Member, CompanyRole.CompanyOwner);
      cy.wait('@getRoleAssignments');

      validateAddUserButton(true);
    });

    it('hides Add User button for unauthorized users', () => {
      const roleAssignments = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
      mockCompanyRoleAssignments(roleAssignments);
      mountCompanyRolesCard(CompanyRole.Member, null);
      cy.wait('@getRoleAssignments');

      validateAddUserButton(false);
    });
  });

  describe('Permission Tests', () => {
    it('allows Company Owners to manage all roles', () => {
      const roleAssignments = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
      mockCompanyRoleAssignments(roleAssignments);
      mountCompanyRolesCard(CompanyRole.Member, CompanyRole.CompanyOwner);
      cy.wait('@getRoleAssignments');

      validateAddUserButton(true);

      cy.get('[data-test="dialog-button"]').should('exist');
    });

    it('allows Member Admins to manage Members and other Admins', () => {
      const roleAssignments = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
      mockCompanyRoleAssignments(roleAssignments);
      mountCompanyRolesCard(CompanyRole.Member, CompanyRole.MemberAdmin);
      cy.wait('@getRoleAssignments');

      validateAddUserButton(true);
      cy.get('[data-test="dialog-button"]').should('exist');
    });

    it('allows Global Admins to manage all roles', () => {
      const roleAssignments = [generateCompanyRoleAssignment(CompanyRole.CompanyOwner, dummyCompanyId)];
      mockCompanyRoleAssignments(roleAssignments);
      mountCompanyRolesCard(CompanyRole.CompanyOwner, null, true, [KEYCLOAK_ROLE_ADMIN]);
      cy.wait('@getRoleAssignments');

      validateAddUserButton(true);
      cy.get('[data-test="dialog-button"]').should('exist');
    });

    it('prevents regular users from managing roles', () => {
      const roleAssignments = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
      mockCompanyRoleAssignments(roleAssignments);
      mountCompanyRolesCard(CompanyRole.Member, null, true, [KEYCLOAK_ROLE_USER]);
      cy.wait('@getRoleAssignments');

      validateAddUserButton(false);
      cy.get('[data-test="dialog-button"]').should('not.exist');
    });
  });

  describe('User Actions Tests', () => {
    it('opens row menu when ellipsis button is clicked', () => {
      const roleAssignments = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
      mockCompanyRoleAssignments(roleAssignments);
      mountCompanyRolesCard(CompanyRole.Member, CompanyRole.CompanyOwner);
      cy.wait('@getRoleAssignments');

      cy.get('[data-test="dialog-button"]').click();
      cy.get('[role="menu"]').should('be.visible');
      cy.get('[data-test="dialog-menu"]').should('contain', 'Change User’s Role');
      cy.get('[data-test="dialog-menu"]').should('contain', 'Remove User');
    });

    it('opens change role dialog when menu item is clicked', () => {
      const roleAssignments = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
      mockCompanyRoleAssignments(roleAssignments);
      mountCompanyRolesCard(CompanyRole.Member, CompanyRole.CompanyOwner);
      cy.wait('@getRoleAssignments');

      cy.get('[data-test="dialog-button"]').click();
      cy.get('[data-test="dialog-menu"]').contains('Change User’s Role').click();

      cy.get('[role="dialog"]').should('contain', 'Change User’s Role');
      cy.get('[role="dialog"]').should('contain', `${dummyFirstName} ${dummyLastName}, ${dummyEmail}`);
      cy.get('[role="listbox"]').should('exist');
    });

    it('disables current role in change role dialog', () => {
      const roleAssignments = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
      mockCompanyRoleAssignments(roleAssignments);
      mountCompanyRolesCard(CompanyRole.Member, CompanyRole.CompanyOwner);
      cy.wait('@getRoleAssignments');

      cy.get('[data-test="dialog-button"]').click();
      cy.get('[data-test="dialog-menu"]').contains('Change User’s Role').click();
      cy.get('[role="listbox"]').within(() => {
        cy.contains('[role="option"]', 'Members').should('have.class', 'p-disabled');
      });
    });

    it('emits users-changed event after successful role change', () => {
      const roleAssignments = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
      mockCompanyRoleAssignments(roleAssignments);
      mountCompanyRolesCard(CompanyRole.Member, CompanyRole.CompanyOwner).then(({ wrapper, component }) => {
        cy.wait('@getRoleAssignments');
        cy.get('[data-test="dialog-button"]').click();
        cy.get('[data-test="dialog-menu"]').contains('Change User’s Role').click();
        cy.get('[role="option"]').contains('Admins').click();
        cy.get('[data-test="change-role-button"]').click();
        cy.wait('@assignRole').then(() => {
          cy.wrap(wrapper.emitted()).should('have.property', 'users-changed');
        });
      });
    });
  });
});
