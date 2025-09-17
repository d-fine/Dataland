import { admin_name, admin_pw, reader_name, reader_pw, reader_userId, premium_user_userId } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { describeIf } from '@e2e/support/TestUtility';
import { type CompanyIdAndName } from '@clients/backend';
import { assignCompanyRole, removeAllCompanyRoles } from '@e2e/utils/CompanyRolesUtils.ts';
import { CompanyRole } from '@clients/communitymanager';
import { setupCommonInterceptions, fetchTestCompanies } from '@e2e/utils/CompanyCockpitPage/CompanyCockpitUtils';

describeIf(
  'As a user, I want the users page to behave as expected',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    let alphaCompanyIdAndName: CompanyIdAndName;
    let betaCompanyIdAndName: CompanyIdAndName;

    before(() => {
      fetchTestCompanies().then(([alpha, beta]) => {
        alphaCompanyIdAndName = alpha;
        betaCompanyIdAndName = beta;
      });
    });

    beforeEach(() => {
      setupCommonInterceptions();
    });

    it('When directing by url to the users page as a basic data reader who is only a company member of another company that user should be redirected to the company cockpit page', () => {
      removeCompanyRoles(alphaCompanyIdAndName.companyId, premium_user_userId);
      cy.ensureLoggedIn(reader_name, reader_pw);
      cy.then(() => getKeycloakToken(admin_name, admin_pw))
        .then((token) => assignCompanyRole(token, CompanyRole.Member, alphaCompanyIdAndName.companyId, reader_userId))
        .then(() => cy.visit(`/companies/${betaCompanyIdAndName.companyId}/users`));
      cy.intercept('GET', `**/api/companies/${betaCompanyIdAndName.companyId}/aggregated-framework-data-summary`).as(
        'fetchAggregatedFrameworkSummaryForBeta'
      );
      cy.wait('@fetchAggregatedFrameworkSummaryForBeta');
      cy.get('[data-test="usersTab"]').should('not.exist');
      cy.get('[data-test=sfdr-summary-panel]').should('be.visible');
    });

    it('As a basic company member you should not be able to add members, change the role of other members or remove them', () => {
      setupUserPage(CompanyRole.Member);
      cy.contains('[data-test="company-roles-card"]', 'Members').within(() => {
        cy.get('[data-test="add-user-button"]').should('not.exist');
      });
    });

    it('As a company admin you should be able to add members', () => {
      setupUserPage(CompanyRole.MemberAdmin);
      cy.contains('[data-test="company-roles-card"]', 'Members').within(() => {
        cy.get('[data-test="add-user-button"]').click();
      });
      cy.get('[data-test="email-input-field"]').should('be.visible').type('data.premium-user@example.com');
      cy.get('[data-test="email-input-field"]').should('have.value', 'data.premium-user@example.com');
      cy.get('[data-test="select-user-button"]').click();
      cy.get('[data-test="save-changes-button"]').click();
      cy.get('.p-dialog').within(() => {
        cy.contains('Success');
        cy.contains('button', 'OK').click();
      });
      cy.contains('[data-test="company-roles-card"]', 'Members').within(() => {
        cy.get('td').contains('PremiumUser').should('exist');
      });
    });

    it('As a company admin you should be able to change the role of other members', () => {
      setupUserPage(CompanyRole.MemberAdmin, CompanyRole.Member);

      cy.contains('[data-test="company-roles-card"]', 'Members').within(() => {
        cy.get('td')
          .contains('PremiumUser')
          .parent()
          .within(() => {
            cy.get('[data-test="dialog-button"]').click();
          });
      });
      cy.get('[data-test="dialog-menu"]').contains('Change User’s Role').click();

      cy.get('[data-test="change-user-role-dialog"]').should('be.visible');
      cy.get('[data-test="change-user-role-dialog"]').contains('.p-listbox-option', 'Admins').click();
      cy.get('[data-test="change-role-button"]').click();
      cy.get('.p-dialog').within(() => {
        cy.contains('Success');
        cy.contains('button', 'OK').click();
      });
      cy.get('[data-test="change-user-role-dialog"]').should('not.exist');
      cy.contains('[data-test="company-roles-card"]', 'Admins').within(() => {
        cy.get('td').contains('PremiumUser').should('exist');
      });
    });

    it('As a company admin you should be able to remove other members', () => {
      setupUserPage(CompanyRole.MemberAdmin, CompanyRole.MemberAdmin);
      cy.contains('[data-test="company-roles-card"]', 'Admins').within(() => {
        cy.get('td')
          .contains('PremiumUser')
          .parent()
          .within(() => {
            cy.get('[data-test="dialog-button"]').click();
          });
      });

      cy.get('[data-test="dialog-menu"]').contains('Remove User').click();

      cy.get('[data-test="remove-user-button"]').should('be.visible');
      cy.get('[data-test="remove-user-button"]').click();

      cy.contains('[data-test="company-roles-card"]', 'Admins').within(() => {
        cy.get('td').contains('PremiumUser').should('not.exist');
      });
    });

    it('When adding yourself to another role the confirmation modal should pop up. Confirm should complete adding your role.', () => {
      setupUserPage(CompanyRole.CompanyOwner);
      cy.contains('[data-test="company-roles-card"]', 'Admins').within(() => {
        cy.get('[data-test="add-user-button"]').click();
      });
      cy.get('[data-test="email-input-field"]').type('data.reader@example.com');
      cy.get('[data-test="select-user-button"]').click();
      cy.get('[data-test="save-changes-button"]').should('not.be.disabled').click();
      cy.get('[data-test="confirm-self-role-change-button"]').click();
      cy.contains('[data-test="company-roles-card"]', 'Admins').within(() => {
        cy.get('td').contains('Reader').should('exist');
      });
    });

    /**
     * Sets up the test environment for user page testing.
     */
    function setupUserPage(userRole: CompanyRole, premiumUserRole: CompanyRole | null = null): void {
      removeCompanyRoles(alphaCompanyIdAndName.companyId, premium_user_userId);
      cy.ensureLoggedIn(reader_name, reader_pw);
      cy.then(() => getKeycloakToken(admin_name, admin_pw))
        .then((token) => {
          void assignCompanyRole(token, userRole, alphaCompanyIdAndName.companyId, reader_userId);
          if (premiumUserRole) {
            void assignCompanyRole(token, premiumUserRole, alphaCompanyIdAndName.companyId, premium_user_userId);
          }
        })
        .then(() => cy.visit(`/companies/${alphaCompanyIdAndName.companyId}/users`));
      cy.intercept('GET', `**/api/companies/${alphaCompanyIdAndName.companyId}/aggregated-framework-data-summary`).as(
        'fetchAggregatedFrameworkSummaryForAlpha'
      );
      cy.wait('@fetchAggregatedFrameworkSummaryForAlpha');
    }

    /**
     * Removes all roles associated with a specific user for a given company.
     * Uses an admin token to perform the operation.
     *
     * @param companyId - The ID of the company whose roles are being removed.
     * @param userId - The ID of the user whose roles are being removed.
     */
    function removeCompanyRoles(companyId: string, userId: string): void {
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return removeAllCompanyRoles(token, companyId, userId);
      });
    }
  }
);
