import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import AddCompanyToPortfoliosDialog from '@/components/general/AddCompanyToPortfoliosModal.vue';
import type { ReducedBasePortfolio } from '@/components/general/AddCompanyToPortfoliosModal.vue';

describe('AddCompanyToPortfoliosDialog', () => {
  const mockPortfolios: ReducedBasePortfolio[] = [
    { portfolioId: 'p1', portfolioName: 'One', companyIds: [] },
    { portfolioId: 'p2', portfolioName: 'Two', companyIds: [] },
    { portfolioId: 'p3', portfolioName: 'Three', companyIds: [] },
  ];

  const mockDialogRef = {
    value: {
      data: {
        companyId: 'COMP-123',
        allUserPortfolios: mockPortfolios,
      },
    },
  };

  it('Should display empty dialog for new portfolio', () => {
    // @ts-ignore
    cy.mountWithPlugins(AddCompanyToPortfoliosDialog, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: {
          dialogRef: mockDialogRef,
        },
      },
    });

    cy.get('.p-listbox').should('exist');
    cy.get('.p-listbox-item.p-highlight').should('not.exist');
    cy.get('[data-test="saveButton"]').should('contain.text', 'Add company').and('be.disabled');
  });

  it('Should disable button and show "No available options" when user has no portfolios', () => {
    // Empty portfolio list
    const mockDialogRef = {
      value: {
        data: {
          companyId: 'COMP-123',
          allUserPortfolios: [],
        },
        close: cy.stub(),
      },
    };

    // @ts-ignore
    cy.mountWithPlugins(AddCompanyToPortfoliosDialog, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: {
          dialogRef: mockDialogRef,
        },
      },
    });

    // Check Listbox shows no options
    cy.get('.p-listbox').should('exist');
    cy.get('.p-listbox-empty-message').should('be.visible').and('contain.text', 'No available options'); // Default empty message from PrimeVue

    // ✅ Check the button is disabled
    cy.get('[data-test="saveButton"]').should('contain.text', 'Add company').and('be.disabled');
  });

  it('should apply p-highlight to all selected and p-focus only to the last selected portfolio', () => {
    // @ts-ignore
    cy.mountWithPlugins(AddCompanyToPortfoliosDialog, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: {
          dialogRef: {
            value: {
              data: {
                companyId: 'COMP-123',
                allUserPortfolios: mockPortfolios,
              },
              close: cy.stub(),
            },
          },
        },
      },
    });

    // Select multiple portfolios in order
    cy.get('.p-listbox-item').eq(0).click(); // Select "One"
    cy.get('.p-listbox-item').eq(1).click(); // Select "Two"
    cy.get('.p-listbox-item').eq(2).click(); // Select "Three" last

    // Assert "Three" has both highlight and focus
    cy.get('.p-listbox-item').eq(2).should('have.class', 'p-highlight').and('have.class', 'p-focus');

    // Assert "One" and "Two" have highlight only
    cy.get('.p-listbox-item').eq(0).should('have.class', 'p-highlight').and('not.have.class', 'p-focus');

    cy.get('.p-listbox-item').eq(1).should('have.class', 'p-highlight').and('not.have.class', 'p-focus');

    // No unselected left in this case — if you deselect one, add:
    // cy.get('.p-listbox-item').eq(0).click(); // Deselect "One"
    // cy.get('.p-listbox-item').eq(0).should('not.have.class', 'p-highlight').and('not.have.class', 'p-focus');
  });

  it('should call the correct API endpoint with correct data after clicking the add button', () => {
    const companyIdToAdd = 'NEW-COMPANY-ID';

    // Intercept the PUT call
    cy.intercept('PUT', '**/portfolios/**').as('updatePortfolio');

    // @ts-ignore
    cy.mountWithPlugins(AddCompanyToPortfoliosDialog, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: {
          dialogRef: {
            value: {
              data: {
                companyId: companyIdToAdd,
                allUserPortfolios: mockPortfolios,
              },
              close: cy.stub(),
            },
          },
        },
      },
    });

    // Select both portfolios
    cy.get('.p-listbox-item').eq(0).click();
    cy.get('.p-listbox-item').eq(1).click();

    // Click the button
    cy.get('button').contains('Add company').click();

    // Wait for and validate both API calls
    cy.wait('@updatePortfolio').then((interception) => {
      // Validate URL contains correct portfolioId
      const url = interception.request.url;
      const allowedIds = ['p1', 'p2'];
      const matchedUrls = allowedIds.some((id) => url.includes(`/users/portfolios/${id}`));
      expect(matchedUrls, `Expected URL to include one of the allowed portfolio IDs, got ${url}`).to.be.true;

      const body = interception.request.body;

      // Validate body shape and data
      expect(body).to.have.property('portfolioName').that.is.a('string');
      expect(body).to.have.property('companyIds').that.includes(companyIdToAdd);
    });

    // Wait for the second call if needed
    cy.wait('@updatePortfolio');
  });

  it('Should handle API errors gracefully', () => {
    // Intercept the replacePortfolio API and simulate a failure
    cy.intercept('PUT', '**/portfolios/**', {
      statusCode: 500,
      body: { message: 'Internal server error' },
    }).as('replacePortfolio');

    // @ts-ignore
    cy.mountWithPlugins(AddCompanyToPortfoliosDialog, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: {
          dialogRef: mockDialogRef,
        },
      },
    });

    // Select a portfolio from the list
    cy.get('.p-listbox-item').first().click();
    cy.get('button').contains('Add company').click();

    cy.wait('@replacePortfolio');

    cy.get('.p-message-text').should('be.visible').and('contain.text', 'fail'); // Assumes your error message contains "Failed"
  });

  it('Should close the modal after successful company addition', () => {
    const closeStub = cy.stub().as('closeStub'); // Tracks if modal is closed

    // Intercept PUT request to simulate success
    cy.intercept('PUT', '**/portfolios/**', {
      statusCode: 200,
      body: {},
    }).as('addCompany');

    // Mount the component
    // @ts-ignore
    cy.mountWithPlugins(AddCompanyToPortfoliosDialog, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: {
          dialogRef: {
            value: {
              data: {
                companyId: 'COMP-123',
                allUserPortfolios: mockPortfolios,
              },
              close: closeStub, // Inject the stubbed close function
            },
          },
        },
      },
    });

    // Select the portfolio
    cy.get('.p-listbox-item').first().click();

    // Click the "Add company" button
    cy.get('button').contains('Add company').click();

    // Wait for the API and check if dialogRef.close() was called
    cy.wait('@addCompany').then(() => {
      cy.get('@closeStub').should('have.been.called');
    });
  });
});
