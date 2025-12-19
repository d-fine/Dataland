import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import options from 'axios';
import CreditsCard from '@/components/resources/companyCockpit/CreditsCard.vue';
import { type CompanyInformation, IdentifierType } from '@clients/backend';

/**
 * Mounts the CreditsCard component with the provided dummyCompanyId.
 */
function mountComponent(dummyCompanyId: string): void {
  //@ts-ignore
  cy.mountWithPlugins(CreditsCard, {
    props: {
      companyId: dummyCompanyId,
    },
    global: {
      provide: {
        getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})),
      },
    },
    ...options,
  });
}

/**
 * Intercepts the credit balance API call and returns the provided body.
 * @param dummyCompanyId of the company to get the balance for
 * @param body of the response
 */
function interceptCreditBalance(dummyCompanyId: string, body: number): void {
  cy.intercept('GET', `**/accounting/credits/${dummyCompanyId}/balance*`, {
    statusCode: 200,
    body: body,
  }).as('creditBalance');
}

/**
 * Intercepts the company information API call and returns the provided body.
 * @param dummyCompanyId of the company to get the information for
 * @param companyInfo of the company
 */
function interceptCompanyInformation(dummyCompanyId: string, companyInfo: CompanyInformation): void {
  cy.intercept('GET', `**/api/companies/${dummyCompanyId}/info`, {
    statusCode: 200,
    body: companyInfo,
  }).as('companyInformation');
}

describe('As a user I want to see available credits on Dataland of the company I am associated to.', () => {
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
  const dummyCompanyInfo: CompanyInformation = {
    companyName: 'Dummy Company',
    headquarters: 'Dummy City',
    headquartersPostalCode: '00000',
    fiscalYearEnd: '2023-12-31',
    reportingPeriodShift: 0,
    sector: 'Dummy Sector',
    identifiers: {
      [IdentifierType.Lei]: ['724500973ODKK3IFQ447'],
    },
    countryCode: 'NL',
    isTeaserCompany: false,
    website: 'https://example.com',
  };

  it('Credit Balance is visible and correctly displayed if a Member has credits', () => {
    interceptCreditBalance(dummyCompanyId, 100);
    interceptCompanyInformation(dummyCompanyId, dummyCompanyInfo);
    mountComponent(dummyCompanyId);
    cy.wait('@creditBalance');
    cy.wait('@companyInformation');

    cy.get('[data-test="credits-balance-chip"]').should('be.visible');
    cy.get('[data-test="credits-balance-chip"]').should('contain', '100');
  });

  it('Credit Balance is correctly displayed if user has zero credits', () => {
    interceptCreditBalance(dummyCompanyId, 0);
    interceptCompanyInformation(dummyCompanyId, dummyCompanyInfo);
    mountComponent(dummyCompanyId);
    cy.wait('@creditBalance');
    cy.wait('@companyInformation');
    cy.get('[data-test="credits-balance-chip"]').should('be.visible');
    cy.get('[data-test="credits-balance-chip"]').should('contain', '0');
  });

  it('Credit Balance is correctly displayed if user has a negative credit balance', () => {
    interceptCreditBalance(dummyCompanyId, -345);
    interceptCompanyInformation(dummyCompanyId, dummyCompanyInfo);
    mountComponent(dummyCompanyId);
    cy.wait('@creditBalance');
    cy.wait('@companyInformation');
    cy.get('[data-test="credits-balance-chip"]').should('be.visible');
    cy.get('[data-test="credits-balance-chip"]').should('contain', '-345');
  });

  it('Info Message is correctly displayed and hidden if user clicks on button', () => {
    const expectedInfoText = 'Any questions regarding your credits? Contact info@dataland.com';
    interceptCreditBalance(dummyCompanyId, 100);
    interceptCompanyInformation(dummyCompanyId, dummyCompanyInfo);
    mountComponent(dummyCompanyId);

    cy.wait('@creditBalance');
    cy.wait('@companyInformation');

    cy.get('[data-test="info-message"]').should('contain', expectedInfoText);
    cy.get('[data-test="info-message"]').first().find('button').click();
    cy.get('[data-test="info-message"]').should('not.exist');
    cy.get('[data-test="info-icon"]').should('be.visible');

    cy.get('[data-test="info-icon"]').click();
    cy.get('[data-test="info-message"]').should('contain', expectedInfoText);
  });
});
