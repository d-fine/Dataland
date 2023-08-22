import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import ThreeLayerTable from "@/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsPanel.vue"
import {FixtureData, getPreparedFixture} from "../../../sharedUtils/Fixtures";
import { CompanyAssociatedDataNewEuTaxonomyDataForNonFinancials, DataMetaInformation, NewEuTaxonomyDataForNonFinancials
} from "../../../../build/clients/backend";
describe("Component test for the NewEUTaxonomy Page", () => {
    let preparedFixtures: Array<FixtureData<NewEuTaxonomyDataForNonFinancials>>;

    before(() => {
        cy.fixture("CompanyInformationWithNewEuTaxonomyDataForNonFinancialsPreparedFixtures").then(function (jsonContent) {
            preparedFixtures = jsonContent as Array<FixtureData<NewEuTaxonomyDataForNonFinancials>>;
        });
    });


    const kpiList:string[] = ["GENERAL", "REVENUE", "CAPEX", "OPEX"];
    let kpiList2 = ["REVENUE", "GENERAL", "OPEX", "CAPEX"];
    it("Check order of the displayed KPIs", () => {
        const preparedFixture = getPreparedFixture("only-eligible-numbers", preparedFixtures);
        const newEuTaxonomyDataForNonFinancialsData = preparedFixture.t;

        cy.intercept("/api/data/new-eutaxonomy-non-financials/mock-data-id", {
            companyId: "mock-company-id",
            reportingPeriod: preparedFixture.reportingPeriod,
            data: newEuTaxonomyDataForNonFinancialsData,
        } as CompanyAssociatedDataNewEuTaxonomyDataForNonFinancials);
        cy.mountWithPlugins(ThreeLayerTable, {
            keycloak: minimalKeycloakMock({}),
            global: {
                stubs: {
                    transition: false,
                },
            },
            data() {
                return {
                    companyId: "mock-company-id",
                    singleDataMetaInfoToDisplay: {
                        dataId: "mock-data-id",
                        reportingPeriod: preparedFixture.reportingPeriod,
                    } as DataMetaInformation,
                };
            },
        });
        //cy.get("[data-test='ThreeLayerTableTest']").eq(0).should("have.text", "KPIs");
        //cy.get("[data-test='TwoLayerTest']").eq(0).find(".p-rowgroup-header").eq(0).should("have.text","General");
        /**
        cy.get("[data-test='ThreeLayerTableTest']").get(".d-table-style").eq(0).eq(0).get(".p-badge").eq(0).should("have.text","GENERAL");
        cy.get("[data-test='ThreeLayerTableTest']").get(".d-table-style").eq(0).eq(0).get(".p-badge").eq(1).should("have.text","REVENUE");
        cy.get("[data-test='ThreeLayerTableTest']").get(".d-table-style").eq(0).eq(0).get(".p-badge").eq(2).should("have.text","CAPEX");
        cy.get("[data-test='ThreeLayerTableTest']").get(".d-table-style").eq(0).eq(0).get(".p-badge").eq(3).should("have.text","OPEX");
         **/

        cy.get("[data-test='ThreeLayerTableTest']").get(".d-table-style")
            .each((element, index) =>  {
                cy.wrap(element).eq(0).eq(0).get(".p-badge").eq(index).should("have.text",kpiList[index]);
            });

        cy.wait(50);


        cy.get("[data-test='ThreeLayerTableTest']").get(".d-table-style")
            .each((element, index) =>  {
                cy.wrap(element).eq(0).eq(0).get(".p-badge").eq(index).should("not.have.text", kpiList2[index]);
            });
    });
});


