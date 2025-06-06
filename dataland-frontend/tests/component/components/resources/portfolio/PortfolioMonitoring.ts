import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import PortfolioMonitoringContent from '@/components/resources/portfolio/PortfolioMonitoring.vue';
import { type EnrichedPortfolio } from '@clients/userservice';

describe('Check the Portfolio Monitoring view', function () {
    let portfolioFixture: EnrichedPortfolio;

    before(function () {
        cy.fixture('enrichedPortfolio.json').then(function (jsonContent) {
            portfolioFixture = jsonContent as EnrichedPortfolio;
        });
    });

    beforeEach(function () {
        // @ts-ignore
        cy.mountWithPlugins(PortfolioMonitoringContent, {
            keycloak: minimalKeycloakMock({}),
            global: {
                provide: {
                    dialogRef: {
                        value: {
                            data: {
                                portfolio: portfolioFixture,
                            },
                            close: cy.stub(),
                        },
                    },
                    getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})),
                },
            },
        });
    });

    it('Check reporting period selector exists and can toggle options', function () {
        cy.get('[data-test="listOfReportingPeriods"]').should('exist');
        cy.get('[data-test="listOfReportingPeriods"]')
            .find('button')
            .each(($btn) => {
                cy.wrap($btn).should('be.visible').click({ force: true });
            });
    });

    it('Check framework switches exist and can be toggled', function () {
        cy.get('.framework-switch-row').should('have.length.greaterThan', 0);
        cy.get('.framework-switch-row')
            .first()
            .within(() => {
                cy.get('input[type="checkbox"]').check({ force: true }).should('be.checked');
                cy.get('input[type="checkbox"]').uncheck({ force: true }).should('not.be.checked');
            });
    });

    it('Shows error messages if no reporting period or framework selected on save', function () {
        cy.get('[data-test="saveChangesButton"]').click();
        cy.get('[data-test="frameworkError"]').should('be.visible').and('contain.text', 'Please select Starting Period.');
        cy.get('[data-test="frameworkError"]').should('be.visible').and('contain.text', 'Please select at least one Framework.');
    });

    it('Allows saving changes when reporting period and frameworks are selected', function () {
        cy.get('[data-test="listOfReportingPeriods"]')
            .find('button')
            .contains('2023')
            .click({ force: true });

        cy.get('.framework-switch-row')
            .first()
            .within(() => {
                cy.get('input[type="checkbox"]').check({ force: true }).should('be.checked');
            });

        cy.get('[data-test="saveChangesButton"]').click();

        cy.get('@dialogRefClose').should('not.have.been.called');
    });
});
