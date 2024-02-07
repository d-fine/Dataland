import { admin_name, admin_pw } from "@e2e/utils/Cypress";
import { type Interception } from "cypress/types/net-stubbing";
import { type SingleDataRequestResponse } from "@clients/communitymanager";
import { describeIf } from "@e2e/support/TestUtility";
import {CompanyIdAndName, DataTypeEnum} from "@clients/backend";
import {getKeycloakToken} from "@e2e/utils/Auth";
import {generateDummyCompanyInformation, uploadCompanyViaApi} from "@e2e/utils/CompanyUpload";

describeIf(
    "As a user I want to be able to navigate to the single data request page and submit a request",
    {
        executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    },
    () => {
        let uniqueCompanyMarker = Date.now().toString();
        let testCompanyName = "Company-for-single-data-request" + uniqueCompanyMarker;
        beforeEach(() => {
            cy.ensureLoggedIn(admin_name, admin_pw);
            cy.visitAndCheckAppMount("/requests");
        });

        it("Crate a company and navigate to its single request page via the company cockpit", () => {
            getKeycloakToken(admin_name, admin_pw).then((token: string) => {
                return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
                    cy.intercept("**/api/companies/" + storedCompany.companyId).as("goToCompanyCockpit");
                    cy.visitAndCheckAppMount(
                        "/companies/" +
                        storedCompany.companyId,
                    );
                    cy.wait("@goToCompanyCockpit", { timeout: Cypress.env("medium_timeout_in_ms") as number });
                    cy.intercept("**/api/companies/singledatarequest/" + storedCompany.companyId).as("goToSingleRequestPage");
                    cy.get('SingleDataRequestButton')
                        .should('exist')
                        .click();
                    cy.wait("@goToSingleRequestPage", { timeout: Cypress.env("medium_timeout_in_ms") as number });
                    cy.url().should(
                        "contain",
                        `/companies/singledatarequest/${storedCompany.companyId}`,
                    );
                });
            });
        });

        it("Fill out the request page and check correct validation", () => {
            cy.intercept("POST", "**/community/requests/bulk").as("postRequestData");
            checkCompanyInfoSheet();
            checkValidation();
            chooseReportingPeriod();
            chooseFramework();

            cy.get("text[name='contactDetails']")
                .type("example@Email.com")
            cy.get("textarea[name='dataRequesterMessage']")
                .type("Some message")

                .get("button[type='submit']")
                .should("exist")
                .click();

            cy.wait("@postRequestData", { timeout: Cypress.env("short_timeout_in_ms") as number }).then((interception) => {
                checkIfIdentifiersProperlyDisplayed(interception);
            });

            cy.get('[data-test="acceptedIdentifiers"] [data-test="identifiersHeading"]').contains("1 REQUESTED IDENTIFIER");
            cy.get('[data-test="rejectedIdentifiers"] [data-test="identifiersHeading"]').contains("1 REJECTED IDENTIFIER");
        });

        /**
         * Choose reporting periods
         */
        function chooseReportingPeriod(): void {
            cy.get('[data-test="reportingPeriods"] div[data-test="toggleChipsFormInput"]')
                .should("exist")
                .get('[data-test="toggle-chip"')
                .first()
                .click()
                .should("have.class", "toggled");

            cy.get("div[data-test='reportingPeriods'] p[data-test='reportingPeriodErrorMessage'").should("not.exist");
        }

        /**
         * Choose a framework
         */
        function chooseFramework(): void {
            const numberOfFrameworks = Object.keys(DataTypeEnum).length;
            cy.get('[data-test="selectFramework"] .p-multiselect')
                .should("exist")
                .click()
                .get(".p-multiselect-panel ul.p-multiselect-items li.p-multiselect-item")
                .should("have.length", numberOfFrameworks)
                .eq(3)
                .click()
                .get("div[data-test='addedFrameworks'] span")
                .should("have.length", 1);
        }

        /**
         * Checks basic validation
         */
        function checkValidation(): void {
            cy.get("button[type='submit']").should("exist").click();

            cy.get("div[data-test='reportingPeriods'] p[data-test='reportingPeriodErrorMessage'")
                .should("be.visible")
                .should("contain.text", "Select at least one reporting period.");

            cy.get("div[data-test='selectFramework'] li[data-message-type='validation']")
                .should("be.visible")
                .should("contain.text", "Select a framework");
        }
        /**
         * Checks if the information on the company banner is correct
         */
        function checkCompanyInfoSheet (): void {
            cy.get('CompanyInfoSheet').within(() => {
                cy.get('@company-id').should('have.attr', 'company-id', '123');
            });
        }
    },
);
