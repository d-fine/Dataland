import {
  admin_name,
  admin_pw,
  admin_userId,
  reader_name,
  reader_pw,
  reader_userId,
  reviewer_userId,
  uploader_userId,
  premium_user_userId,
} from '@e2e/utils/Cypress';
import { searchBasicCompanyInformationForDataType } from '@e2e/utils/GeneralApiUtils';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { describeIf } from '@e2e/support/TestUtility';
import { type CompanyIdAndName, DataTypeEnum } from '@clients/backend';
import { assignCompanyRole, removeAllCompanyRoles } from '@e2e/utils/CompanyRolesUtils.ts';
import { CompanyRole } from '@clients/communitymanager';

describeIf(
  'As a user, I want the users page to behave as expected',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    let alphaCompanyIdAndName: CompanyIdAndName;
    let betaCompanyIdAndName: CompanyIdAndName;

    before(() => {
      getKeycloakToken(reader_name, reader_pw)
        .then((token: string) => {
          return searchBasicCompanyInformationForDataType(token, DataTypeEnum.EutaxonomyNonFinancials);
        })
        .then((basicCompanyInfos) => {
          expect(basicCompanyInfos).to.be.not.empty;
          alphaCompanyIdAndName = {
            companyId: basicCompanyInfos[0].companyId,
            companyName: basicCompanyInfos[0].companyName,
          };
          betaCompanyIdAndName = {
            companyId: basicCompanyInfos[1].companyId,
            companyName: basicCompanyInfos[1].companyName,
          };
        });
    });

    beforeEach(() => {
      cy.intercept('https://youtube.com/**', []);
      cy.intercept('https://jnn-pa.googleapis.com/**', []);
      cy.intercept('https://play.google.com/**', []);
      cy.intercept('https://googleads.g.doubleclick.net/**', []);
    });

    it("When navigating to the company cockpit as a basic data reader who is also a company member sees the users page of the company of which it is a member and doesn't see the users page of a company of which it has no company affiliation", () => {
      removeCompanyRoles(alphaCompanyIdAndName.companyId, reader_userId);
      cy.ensureLoggedIn(reader_name, reader_pw);
      cy.visitAndCheckAppMount(`/companies/${alphaCompanyIdAndName.companyId}`);
      cy.get('[data-test="usersTab"]').should('not.exist');
      cy.then(() => getKeycloakToken(admin_name, admin_pw))
        .then((token) => assignCompanyRole(token, CompanyRole.Member, alphaCompanyIdAndName.companyId, reader_userId))
        .then(() => cy.visit(`/companies/${alphaCompanyIdAndName.companyId}`));
      cy.intercept('GET', `**/api/companies/${alphaCompanyIdAndName.companyId}/aggregated-framework-data-summary`).as(
        'fetchAggregatedFrameworkSummaryForAlpha'
      );
      cy.visit(`/companies/${alphaCompanyIdAndName.companyId}`);
      cy.wait('@fetchAggregatedFrameworkSummaryForAlpha');
      cy.get('[data-test="usersTab"]').click();
      cy.contains('[data-test="company-roles-card"]', 'Members').within(() => {
        cy.get('td').contains(reader_userId).should('exist');
      });
      cy.intercept('GET', `**/api/companies/${betaCompanyIdAndName.companyId}/aggregated-framework-data-summary`).as(
        'fetchAggregatedFrameworkSummaryForBeta'
      );
      cy.visit(`/companies/${betaCompanyIdAndName.companyId}/users`);
      cy.wait('@fetchAggregatedFrameworkSummaryForBeta');
      cy.get('[data-test="usersTab"]').should('not.exist');
    });

    it('As a basic company member you should not be able to add members, change the role of other members or remove them', () => {
      removeCompanyRoles(alphaCompanyIdAndName.companyId, reader_userId);
      cy.ensureLoggedIn(reader_name, reader_pw);
      cy.visitAndCheckAppMount(`/companies/${alphaCompanyIdAndName.companyId}/users`);
      cy.intercept('GET', `**/api/companies/${alphaCompanyIdAndName.companyId}/aggregated-framework-data-summary`).as(
        'fetchAggregatedFrameworkSummaryForAlpha'
      );
      assignAllRoles();
      cy.wait('@fetchAggregatedFrameworkSummaryForAlpha');
      cy.contains('[data-test="company-roles-card"]', 'Members').within(() => {
        cy.get('[data-test="add-user-button"]').should('not.exist');
      });
    });

    it('As a company admin you should be able to add members, change the role of other members or remove them', () => {
      removeCompanyRoles(alphaCompanyIdAndName.companyId, premium_user_userId);
      cy.ensureLoggedIn(reader_name, reader_pw);
      cy.then(() => getKeycloakToken(admin_name, admin_pw))
        .then((token) =>
          assignCompanyRole(token, CompanyRole.MemberAdmin, alphaCompanyIdAndName.companyId, reader_userId)
        )
        .then(() => cy.visit(`/companies/${alphaCompanyIdAndName.companyId}/users`));
      cy.intercept('GET', `**/api/companies/${alphaCompanyIdAndName.companyId}/aggregated-framework-data-summary`).as(
        'fetchAggregatedFrameworkSummaryForAlpha'
      );
      cy.wait('@fetchAggregatedFrameworkSummaryForAlpha');
      cy.contains('[data-test="company-roles-card"]', 'Members').within(() => {
        cy.get('[data-test="add-user-button"]').click();
      });
      cy.get('[data-test="email-input-field"]').type('data.premium-user@example.com');
      cy.get('[data-test="email-input-field"]').should('have.value', 'data.premium-user@example.com');
      cy.get('[data-test="select-user-button"]').click();
      cy.get('[data-test="save-changes-button"]').click();
      cy.get('.p-dialog').within(() => {
        cy.contains('Success');
        cy.contains('button', 'OK').click();
      });
      cy.contains('[data-test="company-roles-card"]', 'Members').within(() => {
        cy.get('td').contains('PremiumUser').should('exist');
        cy.get('[data-test="dialog-button"]').click();
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
        cy.get('td')
          .contains('PremiumUser')
          .parent()
          .within(() => {
            cy.get('[data-test="dialog-button"]').click();
          });
      });

      cy.get('[data-test="dialog-menu"]').contains('Remove User').click();

      cy.get('[data-test="remove-user-button"]').should('be.visible').click();

      cy.contains('[data-test="company-roles-card"]', 'Admins').within(() => {
        cy.get('td').contains('PremiumUser').should('not.exist');
      });
    });

    /**
     * Assign several company roles.
     */
    function assignAllRoles(): void {
      cy.then(() => getKeycloakToken(admin_name, admin_pw))
        .then((token) =>
          assignCompanyRole(token, CompanyRole.Member, alphaCompanyIdAndName.companyId, reader_userId).then(() => token)
        )
        .then((token) =>
          assignCompanyRole(token, CompanyRole.Member, alphaCompanyIdAndName.companyId, reviewer_userId).then(
            () => token
          )
        )
        .then((token) =>
          assignCompanyRole(token, CompanyRole.DataUploader, alphaCompanyIdAndName.companyId, uploader_userId).then(
            () => token
          )
        )
        .then((token) =>
          assignCompanyRole(token, CompanyRole.MemberAdmin, alphaCompanyIdAndName.companyId, admin_userId).then(
            () => token
          )
        )
        .then(() => cy.visit(`/companies/${alphaCompanyIdAndName.companyId}`));
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
