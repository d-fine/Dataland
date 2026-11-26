import ClaimOwnershipPanel from '@/components/resources/companyCockpit/ClaimOwnershipPanel.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';

describe('Component test for ClaimOwnershipPanel', () => {
  beforeEach(() => {
    cy.intercept('GET', `**/api/companies/${dummyCompanyId}/info`, {
      body: { companyName: 'TestClaimOwnershipPanelCompany' },
    });
  });

  it('ClaimOwnershipPanel component works correctly for authenticated user', () => {
    //@ts-ignore
    cy.mountWithPlugins(ClaimOwnershipPanel, {
      keycloak: minimalKeycloakMock({}),
      props: { companyId: dummyCompanyId },
    }).then(() => {
      cy.get("[data-test='claimOwnershipPanelHeading']").should(
        'have.text',
        'Responsible for TestClaimOwnershipPanelCompany?'
      );
      cy.get("[data-test='claimOwnershipPanelLink']")
        .should('have.text', 'Claim company ownership.')
        .click()
        .get('#claimOwnerShipDialog')
        .should('exist')
        .should('be.visible');
    });
  });
  it('ClaimOwnershipPanel component works correctly for non authenticated user', () => {
    const mockedKeycloak = minimalKeycloakMock({ authenticated: false });
    mockedKeycloak.register = function (): Promise<void> {
      return Promise.resolve();
    };
    const registerSpy = cy.spy(mockedKeycloak, 'register');
    //@ts-ignore
    cy.mountWithPlugins(ClaimOwnershipPanel, {
      keycloak: mockedKeycloak,
      props: { companyId: dummyCompanyId },
    }).then(() => {
      cy.get("[data-test='claimOwnershipPanelHeading']").should(
        'have.text',
        'Responsible for TestClaimOwnershipPanelCompany?'
      );
      cy.get("[data-test='claimOwnershipPanelLink']").should('have.text', 'Claim company ownership.').click();
      cy.get("[data-test='claimOwnershipPanelLink']")
        .should('have.text', 'Claim company ownership.')
        .click()
        .get('#claimOwnerShipDialog')
        .should('not.exist');
      cy.wrap(registerSpy).should('have.been.called');
    });
  });
});
