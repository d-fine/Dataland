import CompanyRolesCard from '@/components/resources/companyCockpit/CompanyRolesCard.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { CompanyRole, type CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles.ts';

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
 * Validates the user table row contents
 * @param rowIndex the row index to validate
 * @param user the expected user data
 */
function validateUserTableRow(rowIndex: number, user: CompanyRoleAssignmentExtended): void {
  cy.get('tbody tr')
    .eq(rowIndex)
    .within(() => {
      cy.get('td').eq(0).should('contain', user.firstName);
      cy.get('td').eq(1).should('contain', user.lastName);
      cy.get('td').eq(2).should('contain', user.email);
      cy.get('td').eq(3).should('contain', user.userId);
    });
}

/**
 * Validates button visibility and state
 */
function validateButtonState(buttonState: string): void {
  const shouldExist = buttonState !== 'not visible';
  const shouldBeEnabled = buttonState === 'visible and enabled';

  if (!shouldExist) {
    cy.get('[data-test="dialog-button"]').should('not.exist');
    cy.get('[data-test="add-user-button"]').should('not.exist');
    return;
  }

  cy.get('[data-test="dialog-button"]').should('exist');
  cy.get('[data-test="add-user-button"]').should('exist');

  if (!shouldBeEnabled) {
    cy.get('[data-test="dialog-button"]').and('be.disabled');
    cy.get('[data-test="add-user-button"]').and('be.disabled');
  }
}

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
 * Mounts the Member role card component with Company Owner privileges or with custom role assignments
 * @param userRole The role of the user mounting the card
 * @param roleOfCard The role to display on the card (default: CompanyRole.Member)
 * @param existingCompanyRoleAssignments Optional: existing role assignments to use instead of default
 */
function mountCardAs(
  userRole: CompanyRole = CompanyRole.CompanyOwner,
  roleOfCard: CompanyRole = CompanyRole.Member,
  existingCompanyRoleAssignments?: CompanyRoleAssignmentExtended[]
): void {
  const roleAssignments = existingCompanyRoleAssignments ?? [generateCompanyRoleAssignment(roleOfCard, dummyCompanyId)];
  mockCompanyRoleAssignments(roleAssignments);
  mountCompanyRolesCard(roleOfCard, userRole);
  cy.wait('@getRoleAssignments');
}

/**
 * Validates empty user table
 */
function validateEmptyUserTable(): void {
  cy.get('table').should('exist');
  cy.get('table').contains(dummyFirstName).should('not.exist');
  cy.get('table').contains(dummyLastName).should('not.exist');
  cy.get('table').contains(dummyEmail).should('not.exist');
  cy.get('table').contains(dummyUserId).should('not.exist');
}

/**
 * Validates the user table contents
 * @param expectedUsers array of expected user data
 */
function validateUserTable(expectedUsers: CompanyRoleAssignmentExtended[]): void {
  if (expectedUsers.length === 0) {
    validateEmptyUserTable();
    return;
  }

  cy.get('table').should('exist');
  for (const [index, user] of expectedUsers.entries()) {
    validateUserTableRow(index, user);
  }
}

describe('Company Roles Card Tests', () => {
  before(function () {
    cy.clearLocalStorage();
  });

  const roleDisplayCases = [
    {
      role: CompanyRole.CompanyOwner,
      expectedTitle: 'Company Owners',
      expectedIcon: 'pi pi-crown',
      expectedInfo: 'Company owners have the highest level of access and can add other users as company owners',
    },
    {
      role: CompanyRole.MemberAdmin,
      expectedTitle: 'Admins',
      expectedIcon: 'pi pi-shield',
      expectedInfo: 'The User Admin has the rights to add or remove other user admins and members',
    },
    {
      role: CompanyRole.Member,
      expectedTitle: 'Members',
      expectedIcon: 'pi pi-users',
      expectedInfo: 'Members have the ability to request unlimited data',
    },
    {
      role: CompanyRole.DataUploader,
      expectedTitle: 'Uploaders',
      expectedIcon: 'pi pi-cloud-upload',
      expectedInfo: 'Uploaders have the responsibility of ensuring all relevant data is uploaded',
    },
  ];

  for (const { role, expectedTitle, expectedIcon, expectedInfo } of roleDisplayCases) {
    it(`displays ${expectedTitle} role card correctly`, () => {
      mountCardAs(undefined, role);
      validateCardHeader(expectedTitle, expectedIcon);
      validateInfoMessage(expectedInfo);
    });

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

      mountCardAs(undefined, undefined, roleAssignments);
      validateUserTable(roleAssignments);
    });

    it('shows empty state when no users have the role', () => {
      mountCardAs(undefined, undefined, []);
      validateUserTable([]);
      cy.get('[data-test="add-user-button"]').should('exist');
    });
  }

  const permissionTestCases: {
    allowedRoles: CompanyRole[];
    userRole: CompanyRole | null;
    keycloakRoles: string[];
  }[] = [
    {
      allowedRoles: Object.values(CompanyRole) as CompanyRole[],
      userRole: CompanyRole.CompanyOwner,
      keycloakRoles: [],
    },
    {
      allowedRoles: [CompanyRole.Member, CompanyRole.MemberAdmin],
      userRole: CompanyRole.MemberAdmin,
      keycloakRoles: [],
    },
    {
      allowedRoles: [],
      userRole: CompanyRole.Member,
      keycloakRoles: [],
    },
    {
      allowedRoles: [],
      userRole: CompanyRole.DataUploader,
      keycloakRoles: [],
    },
    {
      allowedRoles: Object.values(CompanyRole) as CompanyRole[],
      userRole: null,
      keycloakRoles: [KEYCLOAK_ROLE_ADMIN],
    },
    {
      allowedRoles: [],
      userRole: null,
      keycloakRoles: [KEYCLOAK_ROLE_USER],
    },
  ];

  /**
   * Determines the expected button state based on permissions
   */
  function getButtonState(testCase: (typeof permissionTestCases)[0], role: CompanyRole): string {
    const shouldExistAndBeEnabled = testCase.allowedRoles.includes(role);
    const shouldBeDisabled = !shouldExistAndBeEnabled && testCase.allowedRoles.length > 0;

    if (shouldExistAndBeEnabled) return 'visible and enabled';
    if (shouldBeDisabled) return 'visible and disabled';
    return 'not visible';
  }

  /**
   * Gets a user-friendly label for the test case
   */
  function getUserRoleLabel(testCase: (typeof permissionTestCases)[0]): string {
    if (testCase.userRole !== null) return testCase.userRole;
    if (testCase.keycloakRoles.includes(KEYCLOAK_ROLE_ADMIN)) return 'Global Admin';
    if (testCase.keycloakRoles.includes(KEYCLOAK_ROLE_USER)) return 'Global User';
    return 'Unknown';
  }

  const permissionScenarios = permissionTestCases.flatMap((testCase) =>
    Object.values(CompanyRole).map((role) => ({ testCase, role }))
  );

  for (const { testCase, role } of permissionScenarios) {
    const buttonState = getButtonState(testCase, role as CompanyRole);
    const userRoleLabel = getUserRoleLabel(testCase);

    it(`As a ${userRoleLabel} on the ${role} card, the Add User button should be ${buttonState}`, () => {
      mockCompanyRoleAssignments([generateCompanyRoleAssignment(role as CompanyRole, dummyCompanyId)]);
      mountCompanyRolesCard(role as CompanyRole, testCase.userRole, true, testCase.keycloakRoles);
      cy.wait('@getRoleAssignments');

      validateButtonState(buttonState);
    });
  }

  it('opens row menu when ellipsis button is clicked', () => {
    mountCardAs();

    cy.get('[data-test="dialog-button"]').click();
    cy.get('[role="menu"]').should('be.visible');
    cy.get('[data-test="dialog-menu"]').should('contain', 'Change User’s Role');
    cy.get('[data-test="dialog-menu"]').should('contain', 'Remove User');
  });

  it('opens change role dialog when menu item is clicked', () => {
    mountCardAs();

    cy.get('[data-test="dialog-button"]').click();
    cy.get('[data-test="dialog-menu"]').contains('Change User’s Role').click();

    cy.get('[role="dialog"]').should('contain', 'Change Role');
    cy.get('[role="dialog"]').should('contain', `${dummyFirstName} ${dummyLastName}, ${dummyEmail}`);
    cy.get('[role="listbox"]').should('exist');
  });

  it('disables current role in change role dialog', () => {
    mountCardAs();

    cy.get('[data-test="dialog-button"]').click();
    cy.get('[data-test="dialog-menu"]').contains('Change User’s Role').click();
    cy.get('[role="listbox"]').within(() => {
      cy.contains('[role="option"]', 'Members').should('have.class', 'p-disabled');
    });
  });

  it('emits users-changed event after successful role change', () => {
    const roleAssignments = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
    mockCompanyRoleAssignments(roleAssignments);
    mountCompanyRolesCard(CompanyRole.Member, CompanyRole.CompanyOwner).then(({ wrapper }) => {
      cy.wait('@getRoleAssignments');
      cy.get('[data-test="dialog-button"]').click();
      cy.get('[data-test="dialog-menu"]').contains('Change User’s Role').click();
      cy.get('[role="option"]').contains('Admins').click();
      cy.get('[data-test="change-role-button"]').click();
      cy.get('[data-test="confirm-self-role-change-button"]').click();
      cy.wait('@assignRole').then(() => {
        cy.wrap(wrapper.emitted()).should('have.property', 'users-changed');
      });
    });
  });
});
