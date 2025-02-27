// cypress/component/DocumentOverview.cy.js
// import { mount } from '@cypress/vue';
import DocumentOverview from '@/components/pages/DocumentOverview.vue';  // Update this path
import {type CompanyInformation, type HeimathafenData} from "@clients/backend";
import type {FixtureData} from "@sharedUtils/Fixtures.ts";
// import {CompanyRole, type CompanyRoleAssignment} from "@clients/communitymanager";
import {getMountingFunction} from "@ct/testUtils/Mount.ts";
import {minimalKeycloakMock} from "@ct/testUtils/Keycloak.ts";
import {KEYCLOAK_ROLE_UPLOADER} from "@/utils/KeycloakRoles.ts";
import {type DocumentMetaInfoResponse} from "@clients/documentmanager";
import {ref} from "vue";


describe('Component test for the Document Overview', () => {
    let companyInformationForTest: CompanyInformation;
    let mockFetchedDocuments: Set<DocumentMetaInfoResponse>;
    const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
    const dummyUserId = 'mock-user-id';
    const documentsFiltered = ref<DocumentMetaInfoResponse[]>([]);

    before(function () {
        cy.fixture('CompanyInformationWithHeimathafenData').then(function (jsonContent) {
            const heimathafenFixtures = jsonContent as Array<FixtureData<HeimathafenData>>;
            companyInformationForTest = heimathafenFixtures[0].companyInformation;
        });
        cy.fixture('CompanyDocumentsMock').then(function (jsonContent) {
            mockFetchedDocuments = jsonContent as Set<DocumentMetaInfoResponse>;
        });
    });

    /**
     * Mocks the requests that happen when the document overview page is being mounted
     * @param hasCompanyAtLeastOneOwner has the company at least one company owner
     */
    function mockRequestsOnMounted(hasCompanyAtLeastOneOwner: boolean): void {
        cy.intercept(`**/api/companies/*/info`, {
            body: companyInformationForTest,
            times: 1,
        }).as('fetchCompanyInfo');
        const hasCompanyAtLeastOneOwnerStatusCode = hasCompanyAtLeastOneOwner ? 200 : 404;
        cy.intercept('**/community/company-ownership/*', {
            statusCode: hasCompanyAtLeastOneOwnerStatusCode,
        }).as('fetchCompanyOwnershipExistence');
        console.log(['before intercept', documentsFiltered.value]);
        cy.intercept(`**/?companyId=` + dummyCompanyId,{
            body: mockFetchedDocuments,
            times: 1,
        }).as('fetchDocumentsFilteredCompanyId');
        cy.intercept(`**/`,{
            body: mockFetchedDocuments,
            times: 1,
        }).as('fetchDocumentsFiltered');
        console.log(['after intercept', documentsFiltered]);
    }

    /**
     * Waits for the 4 requests that happen when the document overview page is being mounted
     */
    function waitForRequestsOnMounted(): void {
        cy.wait('@fetchCompanyInfo');
        cy.wait('@fetchCompanyOwnershipExistence');
        console.log(['before intercept', documentsFiltered.value]);
        cy.wait('@fetchDocumentsFilteredCompanyId');
        cy.wait('@fetchDocumentsFiltered');
        console.log(['before intercept', documentsFiltered.value]);
    }

    /**
     * Mounts the document overview page with a specific authentication
     * @param isLoggedIn determines if the mount shall happen from a logged-in users perspective
     * @param isMobile determines if the mount shall happen from a mobie-users perspective
     * @param keycloakRoles defines the keycloak roles of the user if the mount happens from a logged-in users perspective
     * @returns the mounted component
     */
    function mountDocumentOverviewWithAuthentication(
        isLoggedIn: boolean,
        isMobile: boolean,
        keycloakRoles?: string[],
        // companyRoleAssignments?: CompanyRoleAssignment[]
    ): Cypress.Chainable {
        return getMountingFunction({
            keycloak: minimalKeycloakMock({
                authenticated: isLoggedIn,
                roles: keycloakRoles,
                userId: dummyUserId,
            }),
        })(DocumentOverview, {
            props: {
                companyId: dummyCompanyId,
            },
        });
    }

    it('Check for all expected elements for a logged-in uploader-user and for a company without company owner', () => {
        const hasCompanyAtLeastOneOwner = false;
        mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
        mountDocumentOverviewWithAuthentication(true, false, [KEYCLOAK_ROLE_UPLOADER]);
        waitForRequestsOnMounted();
        cy.wait(Cypress.env('medium_timeout_in_ms') as number );
        cy.get("[data-test='sheet']")
            .should('exist').and('contain', companyInformationForTest.companyName);
        // console.log(['before intercept', documentsFiltered.value]);
        cy.get("[data-test='documents-overview-table']")
            .should('exist')
            .within(() => {
                cy.get('tbody tr')
                    .should('have.length', 2);
            });
    });

    // it('Check filter')
    //
    // it('Check view details')
});
