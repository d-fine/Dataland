import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import AddCompanyToPortfoliosDialog from '@/components/general/AddCompanyToPortfoliosModal.vue';
import type { ReducedBasePortfolio } from '@/components/general/AddCompanyToPortfoliosModal.vue';

describe('AddCompanyToPortfoliosDialog', () => {
  const companyId = 'COMP-123';
  const mockPortfolios: ReducedBasePortfolio[] = [
    { portfolioId: 'p1', portfolioName: 'One', companyIds: [] },
    { portfolioId: 'p2', portfolioName: 'Two', companyIds: [] },
    { portfolioId: 'p3', portfolioName: 'Three', companyIds: [] },
  ];

  /**
   * Creates a mock dialogRef object for mounting the AddCompanyToPortfoliosDialog component in tests.
   *
   * @param {Partial<ReducedBasePortfolio[]>} [override=mockPortfolios] - Optional override for the list of user portfolios.
   * This allows customizing the `allUserPortfolios` provided to the component.
   * @param {() => void} [closeStub] - Optional stub function to replace the default `dialogRef.close()` method.
   * Useful for assertions in tests that validate whether the dialog was closed.
   *
   * @returns {{ value: { data: { companyId: string; allUserPortfolios: Partial<ReducedBasePortfolio[]> }; close: () => void } }}
   * A mock dialogRef object structured as expected by the component.
   */
  const getMockDialogRef = (
    override: Partial<ReducedBasePortfolio[]> = mockPortfolios,
    closeStub?: () => void
  ): {
    value: {
      data: {
        companyId: string;
        allUserPortfolios: Partial<ReducedBasePortfolio[]>;
      };
      close: () => void;
    };
  } => ({
    value: {
      data: {
        companyId,
        allUserPortfolios: override,
      },
      close: closeStub ?? cy.stub(),
    },
  });

  /**
   * Mounts the `AddCompanyToPortfoliosDialog` component with the provided dialogRef
   * using Cypress and the default mocked authentication context.
   *
   * @param {object} dialogRef - The mock dialog reference to inject into the component.
   * This includes both the `companyId` and the list of `allUserPortfolios`, and optionally a `close` stub.
   *
   * @returns {void}
   */
  function mountComponent(dialogRef: object): void {
    // @ts-ignore
    cy.mountWithPlugins(AddCompanyToPortfoliosDialog, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: { dialogRef },
      },
    });
  }

  it('shows empty selection state for a new portfolio', () => {
    mountComponent(getMockDialogRef());

    cy.get('.p-listbox').should('exist');
    cy.get('.p-listbox-item.p-highlight').should('not.exist');
    cy.get('[data-test="saveButton"]').should('contain.text', 'Add company').and('be.disabled');
  });

  it('disables button and shows "No available options" for empty portfolio list', () => {
    mountComponent(getMockDialogRef([]));

    cy.get('.p-listbox').should('exist');
    cy.get('.p-listbox-empty-message').should('be.visible').and('contain.text', 'No available options');
    cy.get('[data-test="saveButton"]').should('be.disabled');
  });

  it('applies highlight and focus styles correctly when selecting multiple portfolios', () => {
    mountComponent(getMockDialogRef());

    cy.get('.p-listbox-item').eq(0).click(); // One
    cy.get('.p-listbox-item').eq(1).click(); // Two
    cy.get('.p-listbox-item').eq(2).click(); // Three

    cy.get('.p-listbox-item').eq(2).should('have.class', 'p-highlight').and('have.class', 'p-focus');
    cy.get('.p-listbox-item').eq(0).should('have.class', 'p-highlight').and('not.have.class', 'p-focus');
    cy.get('.p-listbox-item').eq(1).should('have.class', 'p-highlight').and('not.have.class', 'p-focus');
  });

  it('calls replace API with correct data when adding a company', () => {
    const newCompanyId = 'NEW-COMPANY-ID';
    const mockDialogRef = getMockDialogRef(mockPortfolios.map((p) => ({ ...p, companyIds: [] })));

    mockDialogRef.value.data.companyId = newCompanyId;

    cy.intercept('PUT', '**/portfolios/**').as('updatePortfolio');

    mountComponent(mockDialogRef);

    cy.get('.p-listbox-item').eq(0).click();
    cy.get('.p-listbox-item').eq(1).click();

    cy.get('[data-test="saveButton"]').click();

    cy.wait('@updatePortfolio').then((interception) => {
      const url = interception.request.url;
      expect(['p1', 'p2'].some((id) => url.includes(id))).to.be.true;

      const body = interception.request.body;
      expect(body.companyIds).to.include(newCompanyId);
      expect(body.portfolioName).to.be.a('string');
    });

    cy.wait('@updatePortfolio');
  });

  it('displays error message on failed API response', () => {
    cy.intercept('PUT', '**/portfolios/**', {
      statusCode: 500,
      body: { message: 'Internal server error' },
    }).as('replacePortfolio');

    mountComponent(getMockDialogRef());

    cy.get('.p-listbox-item').first().click();
    cy.get('[data-test="saveButton"]').click();
    cy.wait('@replacePortfolio');

    cy.get('.p-message-text').should('be.visible').and('contain.text', 'fail'); // UI should show fallback error
  });

  it('closes the dialog after successful company addition', () => {
    const closeStub = cy.stub().as('closeStub');

    cy.intercept('PUT', '**/portfolios/**', {
      statusCode: 200,
    }).as('addCompany');

    mountComponent(getMockDialogRef(mockPortfolios, closeStub));

    cy.get('.p-listbox-item').first().click();
    cy.get('[data-test="saveButton"]').click();

    cy.wait('@addCompany').then(() => {
      cy.get('@closeStub').should('have.been.called');
    });
  });
});
