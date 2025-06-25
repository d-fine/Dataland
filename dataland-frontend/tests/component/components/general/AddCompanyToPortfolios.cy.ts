import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import AddCompanyToPortfolios from '@/components/general/AddCompanyToPortfolios.vue';
import { type BasePortfolio } from '@clients/userservice';

describe('AddCompanyToPortfolios', () => {
  const companyId = 'COMP-123';
  const mockPortfolios: BasePortfolio[] = [
    {
      portfolioId: 'p1',
      portfolioName: 'One',
      companyIds: new Set(),
      userId: 'user-id',
      creationTimestamp: 0,
      lastUpdateTimestamp: 1,
    },
    {
      portfolioId: 'p2',
      portfolioName: 'Two',
      companyIds: new Set(),
      userId: 'user-id',
      creationTimestamp: 123,
      lastUpdateTimestamp: 456,
    },
    {
      portfolioId: 'p3',
      portfolioName: 'Three',
      companyIds: new Set(),
      userId: 'user-id',
      creationTimestamp: 999,
      lastUpdateTimestamp: 9999,
    },
  ];

  /**
   * Creates a mock dialogRef object for mounting the AddCompanyToPortfolios component in tests.
   * @param {Partial<BasePortfolio[]>} [override=mockPortfolios] - Optional override for the list of user portfolios.
   * This allows customizing the `allUserPortfolios` provided to the component.
   * @param {() => void} [closeStub] - Optional stub function to replace the default `dialogRef.close()` method.
   * Useful for assertions in tests that validate whether the dialog was closed.
   *
   * @returns {{ value: { data: { companyId: string; allUserPortfolios: Partial<BasePortfolio[]> }; close: () => void } }}
   * A mock dialogRef object structured as expected by the component.
   */
  const getMockDialogRef = (
    override: Partial<BasePortfolio[]> = mockPortfolios,
    closeStub?: () => void
  ): {
    value: {
      data: {
        companyId: string;
        allUserPortfolios: Partial<BasePortfolio[]>;
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
   * Mounts the `AddCompanyToPortfolios` component with the provided dialogRef
   * using Cypress and the default mocked authentication context.
   *
   * @param {object} dialogRef - The mock dialog reference to inject into the component.
   * This includes both the `companyId` and the list of `allUserPortfolios`, and optionally a `close` stub.
   *
   * @returns {void}
   */
  function mountComponent(dialogRef: object): void {
    // @ts-ignore
    cy.mountWithPlugins(AddCompanyToPortfolios, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: { dialogRef },
      },
    });
  }

  it('shows empty selection state for a new portfolio', () => {
    mountComponent(getMockDialogRef());

    cy.get('.p-multiselect').should('exist').click();
    cy.get('.p-multiselect-item.p-highlight').should('not.exist');
    cy.get('.p-multiselect').click();
    cy.get('[data-test="saveButton"]').should('contain.text', 'Add company').and('be.disabled');
  });

  it('disables button and shows "No available options" for empty portfolio list', () => {
    mountComponent(getMockDialogRef([]));

    cy.get('.p-multiselect').should('exist').click();
    cy.get('.p-multiselect-empty-message').should('be.visible').and('contain.text', 'No available options');
    cy.get('.p-multiselect').click();
    cy.get('[data-test="saveButton"]').should('be.disabled');
  });

  it('applies highlight and focus styles correctly when selecting multiple portfolios', () => {
    mountComponent(getMockDialogRef());

    cy.get('.p-multiselect').should('exist').click();
    cy.get('.p-multiselect-item').eq(0).click(); // One
    cy.get('.p-multiselect-item').eq(1).click(); // Two
    cy.get('.p-multiselect-item').eq(2).click(); // Three

    cy.get('.p-multiselect-item').eq(2).should('have.class', 'p-highlight').and('have.class', 'p-focus');
    cy.get('.p-multiselect-item').eq(0).should('have.class', 'p-highlight').and('not.have.class', 'p-focus');
    cy.get('.p-multiselect-item').eq(1).should('have.class', 'p-highlight').and('not.have.class', 'p-focus');

    cy.get('.p-multiselect').click();
    cy.get('[data-test="saveButton"]').should('be.enabled');
  });

  it('calls replace API with correct data when adding a company', () => {
    const newCompanyId = 'NEW-COMPANY-ID';
    const mockDialogRef = getMockDialogRef(
      mockPortfolios.map((p) => {
        return p.portfolioId === 'p2' ? { ...p, companyIds: new Set(newCompanyId) } : p;
      })
    );

    mockDialogRef.value.data.companyId = newCompanyId;

    cy.intercept('PUT', '**/portfolios/**').as('updatePortfolio');

    mountComponent(mockDialogRef);

    cy.get('.p-multiselect').should('exist').click();
    cy.get('.p-multiselect-item').eq(0).click();
    cy.get('.p-multiselect-item').eq(1).click();
    cy.get('.p-multiselect').click();

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

    cy.get('.p-multiselect').should('exist').click();
    cy.get('.p-multiselect-item').first().click();
    cy.get('.p-multiselect').click();
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

    cy.get('.p-multiselect').should('exist').click();
    cy.get('.p-multiselect-item').first().click();
    cy.get('.p-multiselect').click();
    cy.get('[data-test="saveButton"]').click();

    cy.wait('@addCompany').then(() => {
      cy.get('@closeStub').should('have.been.called');
    });
  });
});
