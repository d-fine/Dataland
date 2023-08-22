import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import ThreeLayerTable from "@/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsPanel.vue"
import {FixtureData, getPreparedFixture} from "../../../sharedUtils/Fixtures";
import {
    CompanyAssociatedDataLksgData, CompanyAssociatedDataNewEuTaxonomyDataForNonFinancials,
    DataAndMetaInformationLksgData,
    DataAndMetaInformationNewEuTaxonomyDataForNonFinancials, DataMetaInformation,
    LksgData, NewEuTaxonomyDataForNonFinancials
} from "../../../../build/clients/backend";
import {
    newEuTaxonomyForNonFinancialsDisplayDataModel
} from "../../../../src/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsDisplayDataModel";
describe("Component test for the NewEUTaxonomy Page", () => {
    let preparedFixtures: Array<FixtureData<NewEuTaxonomyDataForNonFinancials>>;

    before(() => {
        cy.fixture("CompanyInformationWithNewEuTaxonomyDataForNonFinancialsPreparedFixtures").then(function (jsonContent) {
            preparedFixtures = jsonContent as Array<FixtureData<NewEuTaxonomyDataForNonFinancials>>;
        });
    });


    let kpiList:string[] = ["Basic Information", "Assurance", "Total Revenue", "Total CapEx", "Total OpEx"];
    kpiList = ["General", "Revenue", "Capex", "Opex"];
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
        //cy.get("[data-test='ThreeLayerTableTest']").get(".d-table-style").eq(2).eq(0).get(".p-badge").eq(2).should("have.text","GENERAL");
        /**
        cy.get("[data-test='ThreeLayerTableTest']").get(".d-table-style")
            .each((element, index) =>  {
                element.eq(index).eq(0).get(".p-badge").eq(index).should("have.text","General");
            });
**/
        //find(".p-rowgroup-header").eq(0).should("have.text","General");

        //cy.get("[data-test='ThreeLayerTableTest']").eq(0).eq(0).should("have.text", "General");

        //cy.get("[data-test='General']");
        //cy.get("[data-test='Revenue']");


       // cy.get("tbody").find(`span:contains(${newEuTaxonomyDataForNonFinancialsData.general.masterData.dataDate})`).should("exist");

        /**
         *

        cy.get("[data-test='ThreeLayerTableTest']").eq(0).find("span")
            .each((element, index) => {
                element.should('have.text', kpiList[index]);
            });
        **/
    });

});

