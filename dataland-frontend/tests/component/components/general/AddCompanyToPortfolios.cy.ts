import AddCompanyToPortfolios from '@/components/general/AddCompanyToPortfolios.vue';
import { type BasePortfolio, NotificationFrequency } from '@clients/userservice';
import { BasePortfolioTimeWindowThresholdEnum } from '@clients/userservice';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

/**
 * Stubs the request for the portfolios of the current user. Sets are converted to arrays as in real JSON responses.
 * @param portfolios the portfolios returned for the current user
 */
function interceptPortfolios(portfolios: Partial<BasePortfolio>[]): void {
  const body = JSON.parse(JSON.stringify(portfolios, (_key, value) => (value instanceof Set ? [...value] : value)));
  cy.intercept('GET', '**/users/portfolios/', { body }).as('fetchPortfolios');
}

/**
 * Mounts the `AddCompanyToPortfolios` component with the provided dialogRef
 * using Cypress and the default mocked authentication context.
 *
 * @param {object} dialogRef - The mock dialog reference to inject into the component.
 * This includes the `companyId` and optionally a `close` stub.
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

describe('Tests for AddCompanyToPortfolios Component', () => {
  const companyId = 'COMP-123';
  const mockPortfolios: BasePortfolio[] = [
    {
      portfolioId: 'p1',
      portfolioName: 'One',
      identifiers: new Set(),
      userId: 'user-id',
      creationTimestamp: 0,
      lastUpdateTimestamp: 1,
      isMonitored: true,
      monitoredFrameworks: new Set('sfdr'),
      notificationFrequency: NotificationFrequency.NoNotifications,
      timeWindowThreshold: BasePortfolioTimeWindowThresholdEnum.Standard,
      sharedUserIds: new Set(),
    },
    {
      portfolioId: 'p2',
      portfolioName: 'Two',
      identifiers: new Set(),
      userId: 'user-id',
      creationTimestamp: 123,
      lastUpdateTimestamp: 456,
      isMonitored: false,
      monitoredFrameworks: new Set(),
      notificationFrequency: NotificationFrequency.NoNotifications,
      timeWindowThreshold: undefined,
      sharedUserIds: new Set(),
    },
    {
      portfolioId: 'p3',
      portfolioName: 'Three',
      identifiers: new Set(),
      userId: 'user-id',
      creationTimestamp: 999,
      lastUpdateTimestamp: 9999,
      isMonitored: true,
      monitoredFrameworks: new Set(['sfdr', 'eutaxonomy']),
      notificationFrequency: NotificationFrequency.NoNotifications,
      timeWindowThreshold: BasePortfolioTimeWindowThresholdEnum.Standard,
      sharedUserIds: new Set(),
    },
  ];

  /**
   * Creates a mock dialogRef object for mounting the AddCompanyToPortfolios component in tests and stubs the
   * portfolios of the current user.
   * @param {Partial<BasePortfolio[]>} [override=mockPortfolios] - Optional override for the list of user portfolios.
   * @param {() => void} [closeStub] - Optional stub function to replace the default `dialogRef.close()` method.
   * Useful for assertions in tests that validate whether the dialog was closed.
   *
   * @returns {{ value: { data: { companyId: string }; close: () => void } }}
   * A mock dialogRef object structured as expected by the component.
   */
  const getMockDialogRef = (
    override: Partial<BasePortfolio>[] = mockPortfolios,
    closeStub?: () => void
  ): {
    value: {
      data: {
        companyId: string;
      };
      close: () => void;
    };
  } => {
    interceptPortfolios(override);
    return {
      value: {
        data: {
          companyId,
        },
        close: closeStub ?? cy.stub(),
      },
    };
  };

  it('shows empty selection state for a new portfolio', () => {
    mountComponent(getMockDialogRef());

    cy.get('.p-listbox').should('exist');
    cy.get('.p-listbox-list .p-listbox-option.p-highlight').should('not.exist');
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

    cy.get('.p-listbox').should('exist');
    cy.get('.p-listbox-option').eq(0).click(); // One
    cy.get('.p-listbox-option').eq(1).click(); // Two
    cy.get('.p-listbox-option').eq(2).click(); // Three

    cy.get('.p-listbox-option').eq(2).should('have.class', 'p-focus');
    cy.get('.p-listbox-option').eq(0).should('not.have.class', 'p-focus');
    cy.get('.p-listbox-option').eq(1).should('not.have.class', 'p-focus');

    cy.get('[data-test="saveButton"]').should('be.enabled');
  });

  it('calls replace API with correct data when adding a company', () => {
    const newCompanyId = 'NEW-COMPANY-ID';
    const mockDialogRef = getMockDialogRef(
      mockPortfolios.map((p) => {
        return p.portfolioId === 'p2' ? { ...p, identifiers: new Set(newCompanyId) } : p;
      })
    );

    mockDialogRef.value.data.companyId = newCompanyId;

    cy.intercept('PUT', '**/portfolios/**').as('updatePortfolio');

    mountComponent(mockDialogRef);

    cy.get('.p-listbox').should('exist');
    cy.get('.p-listbox-option').eq(0).click();
    cy.get('.p-listbox-option').eq(1).click();

    cy.get('[data-test="saveButton"]').click();

    cy.wait('@updatePortfolio').then((interception) => {
      const url = interception.request.url;
      expect(['p1', 'p2'].some((id) => url.includes(id))).to.be.true;

      const body = interception.request.body;
      expect(body.identifiers).to.include(newCompanyId);
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

    cy.get('.p-listbox').should('exist');
    cy.get('.p-listbox-option').first().click();
    cy.get('[data-test="saveButton"]').click();
    cy.wait('@replacePortfolio');

    cy.get('.p-message-text').should('be.visible').and('contain.text', 'fail'); // UI should show fallback error
  });

  it('closes the dialog after successful company addition', () => {
    const closeStub = cy.stub().as('closeStub');

    cy.intercept('PUT', '**/portfolios/**', {
      statusCode: 200,
    }).as('addCompany');

    cy.clock();

    mountComponent(getMockDialogRef(mockPortfolios, closeStub));

    cy.get('.p-listbox').should('exist');
    cy.get('.p-listbox-option').first().click();
    cy.get('[data-test="saveButton"]').click();

    cy.wait('@addCompany').then(() => {
      cy.tick(200000);
      cy.get('@closeStub').should('have.been.called');
    });
  });

  it('shows an error message if the portfolios cannot be loaded', () => {
    cy.intercept('GET', '**/users/portfolios/', { statusCode: 500 }).as('fetchPortfolios');

    mountComponent({ value: { data: { companyId }, close: cy.stub() } });

    cy.wait('@fetchPortfolios');
    cy.get('[data-test="portfoliosErrorMessage"]').should('be.visible');
    cy.get('[data-test="saveButton"]').should('be.disabled');
  });

  it('requests the portfolios again after the company was added', () => {
    cy.intercept('PUT', '**/portfolios/**', { statusCode: 200, body: {} }).as('addCompany');

    mountComponent(getMockDialogRef());

    cy.wait('@fetchPortfolios');
    cy.get('.p-listbox-option').first().click();
    cy.get('[data-test="saveButton"]').click();
    cy.wait('@addCompany');
    cy.wait('@fetchPortfolios');
  });
});
