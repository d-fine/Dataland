import MyDataRequestsOverview from '@/components/pages/MyDataRequestsOverview.vue';
import router from '@/router';
import {DataTypeEnum} from '@clients/backend';
import {
    RequestState,
    type ExtendedStoredRequest,
    RequestPriority,
} from '@clients/datasourcingservice';
import {minimalKeycloakMock} from '@ct/testUtils/Keycloak';

const mockDataRequests: ExtendedStoredRequest[] = [];
const expectedHeaders = ['COMPANY', 'REPORTING PERIOD', 'FRAMEWORK', 'REQUESTED', 'LAST UPDATED', 'REQUEST STATE'];
const dummyRequestId = 'dummyRequestId';

before(function () {
    /**
     * Builds an extended stored data request object and assures type-safety.
     * @param dataType to include in the data request
     * @param reportingPeriod to include in the data request
     * @param companyName to include in the data request
     * @param companyId to include in the data request
     * @param state to set in the data request
     * @param requestPriority to set in the data request
     * @returns an extended sorted data request object
     */
    function buildExtendedStoredRequest(
        dataType: DataTypeEnum,
        reportingPeriod: string,
        companyName: string,
        companyId: string,
        state: RequestState,
        requestPriority: RequestPriority
    ): ExtendedStoredRequest {
        return {
            id: dummyRequestId,
            userId: 'some-user-id',
            creationTimeStamp: 1709204495770,
            dataType: dataType,
            reportingPeriod: reportingPeriod,
            companyId: companyId,
            companyName: companyName,
            lastModifiedDate: 1709204495770,
            state: state,
            requestPriority: requestPriority,
        };
    }

    mockDataRequests.push(
        buildExtendedStoredRequest(
            DataTypeEnum.Lksg,
            '2020',
            'companyProcessed',
            'compA',
            RequestState.Processed,
            RequestPriority.Low
        ),
        buildExtendedStoredRequest(
            DataTypeEnum.Sfdr,
            '2022',
            'companyOpen',
            'someId',
            RequestState.Open,
            RequestPriority.Low
        ),
        buildExtendedStoredRequest(
            DataTypeEnum.EutaxonomyFinancials,
            '9999',
            'z-company-that-will-always-be-sorted-to-bottom',
            'someId',
            RequestState.Withdrawn,
            RequestPriority.Low
        ),
        buildExtendedStoredRequest(
            DataTypeEnum.EutaxonomyNonFinancials,
            '2021',
            'companyWithdrawn',
            'someId',
            RequestState.Withdrawn,
            RequestPriority.Low
        ),
        buildExtendedStoredRequest(
            DataTypeEnum.Lksg,
            '1021',
            'a-company-that-will-always-be-sorted-to-top',
            'someId',
            RequestState.Processed,
            RequestPriority.Low
        )
    );
});
describe('Component tests for the data requests search page', function (): void {
    it('Check sorting', function (): void {
        cy.intercept('**data-sourcing/requests/user', {
            body: mockDataRequests,
            status: 200,
        }).as('UserRequests');
        cy.mountWithPlugins(MyDataRequestsOverview, {
            keycloak: minimalKeycloakMock({}),
        });
        const sortingColumHeader = ['COMPANY', 'REPORTING PERIOD', 'REQUESTED','REQUEST STATE'];
        for (const value of sortingColumHeader) {
            cy.get(`table th:contains(${value})`).should('exist').click();
            cy.get('[data-test="requested-datasets-table"]')
                .find('tr')
                .find('td')
                .contains('a-company-that-will-always-be-sorted-to-top')
                .parent()
                .invoke('index')
                .should('eq', 0);
            cy.get(`table th:contains(${value})`).should('exist').click();
            cy.get('[data-test="requested-datasets-table"]')
                .find('tr')
                .find('td')
                .contains('z-company-that-will-always-be-sorted-to-bottom')
                .parent()
                .invoke('index')
                .should('eq', 0);
        }
    });

    it('Check page when there are no requested datasets', function (): void {
        cy.intercept('data-sourcing/requests/user', {
            body: [],
            status: 200,
        }).as('UserRequests');
        cy.spy(router, 'push').as('routerPush');
        cy.mountWithPlugins(MyDataRequestsOverview, {
            keycloak: minimalKeycloakMock({}),
            router: router,
        }).then(() => {
            cy.get('[data-test="requested-datasets-table"]').should('not.exist');
            cy.get('[data-test="myPortfoliosButton"]').should('exist').should('be.visible').click();
            cy.get('@routerPush').should('have.been.calledWith', '/portfolios');
        });
    });

    it('Check static layout of the search page', function () {
        const placeholder = 'Search by company name';
        const inputValue = 'A company name';

        cy.intercept('data-sourcing/requests/user', {
            body: mockDataRequests,
            status: 200,
        }).as('UserRequests');
        cy.mountWithPlugins(MyDataRequestsOverview, {
            keycloak: minimalKeycloakMock({}),
        });

        cy.get('[data-test="requested-datasets-table"]').should('exist');
        for (const value of expectedHeaders) {
            cy.get(`table th:contains(${value})`).should('exist');
        }
        cy.get('[data-test="requested-datasets-searchbar"]')
            .should('exist')
            .should('not.be.disabled')
            .type(inputValue)
            .should('have.value', inputValue)
            .invoke('attr', 'placeholder')
            .should('contain', placeholder);
        cy.get('[data-test="requested-datasets-frameworks"]').should('exist');
    });

    it('Check the content of the data table', function (): void {
        const expectedCompanys = [
            'companyProcessed',
            'companyOpen',
            'companyWithdrawn',
            'z-company-that-will-always-be-sorted-to-bottom',
            'a-company-that-will-always-be-sorted-to-top',
        ];
        const expectedReportingPeriods = ['2020', '2021', '2022'];

        cy.intercept('data-sourcing/requests/user', {
            body: mockDataRequests,
            status: 200,
        }).as('UserRequests');

        cy.mountWithPlugins(MyDataRequestsOverview, {
            keycloak: minimalKeycloakMock({}),
        });

        for (const value of expectedCompanys) {
            cy.get('[data-test="requested-datasets-table"]').find('tr').find('td').contains(value).should('exist');
        }
        cy.get('[data-test="requested-datasets-table"]').find('tr').find('td').contains('DummyName').should('not.exist');
        for (const value of expectedReportingPeriods) {
            cy.get('[data-test="requested-datasets-table"]').find('tr').find('td').contains(value).should('exist');
        }
        cy.get('[data-test="requested-datasets-table"]').find('tr').find('td').contains('2019').should('not.exist');
    });

    it('Check existence and functionality of searchbar and resolve button', function (): void {
        cy.intercept('data-sourcing/requests/user', {
            body: mockDataRequests,
            status: 200,
        }).as('UserRequests');
        cy.spy(router, 'push').as('routerPush');

        cy.mountWithPlugins(MyDataRequestsOverview, {
            keycloak: minimalKeycloakMock({}),
            router: router,
        }).then(() => {
            cy.get('[data-test="requested-datasets-searchbar"]')
                .should('exist')
                .should('not.be.disabled')
                .clear()
                .type('companyOpen');
            cy.get('[data-test="requested-Datasets-Resolve"]').should('not.exist');
            cy.get('[data-test="requested-datasets-searchbar"]')
                .should('exist')
                .should('not.be.disabled')
                .clear()
                .type('companyProcessed');
            cy.get('[data-test="requested-Datasets-Resolve"]').should('exist').should('be.visible').click();
            cy.get('@routerPush').should('have.been.calledWith', `/requests/${dummyRequestId}`);
        });
    });

    it('Check filter functionality and reset button', function (): void {
        const expectedFrameworkNameSubstrings = [
            'SFDR',
            'EU Taxonomy',
            'for financial companies',
            'for non-financial companies',
        ];

        cy.intercept('data-sourcing/requests/user', {
            body: mockDataRequests,
            status: 200,
        }).as('UserRequests');

        cy.mountWithPlugins(MyDataRequestsOverview, {
            keycloak: minimalKeycloakMock({}),
        }).then(() => {
            cy.get('[data-test="requested-datasets-frameworks"]')
                .click()
                .get('.p-multiselect-option')
                .contains('LkSG')
                .click();
            cy.get('[data-test="requested-datasets-frameworks"]').click();
            for (const value of expectedFrameworkNameSubstrings) {
                cy.get(`table tbody:contains(${value})`).should('not.exist');
            }
            cy.get('[data-test="reset-filter"]').should('exist').click();
            for (const value of expectedFrameworkNameSubstrings) {
                cy.get(`table tbody:contains(${value})`).should('exist');
            }
            cy.get(`table tbody:contains("SME")`).should('not.exist');
        });
    });

    it('Check the functionality of rowClick event', function (): void {
        cy.intercept('data-sourcing/requests/user', {
            body: mockDataRequests,
            status: 200,
        }).as('UserRequests');
        cy.spy(router, 'push').as('routerPush');
        cy.mountWithPlugins(MyDataRequestsOverview, {
            keycloak: minimalKeycloakMock({}),
            router: router,
        }).then(() => {
            cy.get('[data-test="requested-datasets-table"]').within(() => {
                cy.get('tr:last').click();
            });
            cy.get('@routerPush').should('have.been.calledWith', `/requests/${dummyRequestId}`);
        });
    });
});
