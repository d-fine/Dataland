import PortfolioRemoveSharing from '@/components/resources/portfolio/PortfolioRemoveSharing.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

const userId = 'test-user-id';
const portfolioId = 'test-portfolio-id';

/**
 * Helper to mount the component with default props and a close spy.
 */
function mountComponent(): void {
  const onCloseSpy = cy.spy().as('onCloseSpy');
  const onSharingRemovedSpy = cy.spy().as('onSharingRemovedSpy');

  // @ts-ignore
  cy.mountWithPlugins(PortfolioRemoveSharing, {
    keycloak: minimalKeycloakMock({
      userId,
    }),
    props: {
      visible: true,
      portfolioId,
      onClose: onCloseSpy,
      onSharingRemoved: onSharingRemovedSpy,
    },
  });
}

describe('PortfolioRemoveSharing dialog', () => {
  it('renders the dialog with correct text and buttons when visible', () => {
    mountComponent();

    cy.get('[data-test="remove-sharing-modal"]').should('be.visible');
    cy.contains('Remove Portfolio Access?').should('be.visible');

    cy.contains(
      'Are you sure you want to remove this portfolio? You will no longer have access to this portfolio unless the creator shares it with you again.'
    ).should('be.visible');

    cy.get('[data-test="remove-cancel-button"]').should('be.visible').and('contain.text', 'CANCEL');

    cy.get('[data-test="remove-confirmation-button"]').should('be.visible').and('contain.text', 'REMOVE PORTFOLIO');
  });

  it('emits "close" when CANCEL is clicked and does not call the API', () => {
    cy.intercept('DELETE', '**/users/portfolios/shared/*').as('removePortfolio');
    mountComponent();

    cy.get('[data-test="remove-cancel-button"]').click();

    cy.get('@onCloseSpy').should('have.been.calledOnce');
    cy.get('@onSharingRemovedSpy').should('not.have.been.called');

    cy.get('@removePortfolio.all').should('have.length', 0);
  });

  it('calls deleteCurrentUserFromSharing with the portfolioId and emits "sharing-removed" when REMOVE PORTFOLIO is clicked', () => {
    cy.intercept('DELETE', '**/users/portfolios/shared/*', {
      statusCode: 204,
    }).as('removePortfolio');
    mountComponent();

    cy.get('[data-test="remove-confirmation-button"]').click();

    cy.wait('@removePortfolio').then((interception) => {
      expect(interception.request.method).to.eq('DELETE');
      expect(interception.request.url).to.include(portfolioId);
    });

    cy.get('@onCloseSpy').should('not.have.been.called');
    cy.get('@onSharingRemovedSpy').should('have.been.calledOnce');
  });
});
