import ThreeLayerDataTable from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue";
import { minimalKeycloakMock } from "../../testUtils/Keycloak";
import { newEuTaxonomyForNonFinancialsDisplayDataModel } from "../../../../src/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsDisplayDataModel";
import { DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel } from "../../../../src/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsViewModel";
import { mockData } from "@ct/utils/mockDataNewEuTaxonomyForNonFinancials";
describe("Component test for the NewEUTaxonomy Page", () => {
  const kpiList: string[] = ["BASIC INFORMATION", "ASSURANCE", "REVENUE", "CAPEX", "OPEX"];
  const dataTestList: string[] = ["Basic Information", "Assurance", "Revenue", "CapEx", "OpEx"];
  const kpiListOrderChanged = ["ASSURANCE", "OPEX", "BASIC INFORMATION", "REVENUE", "CAPEX"];

  const subcategoryList: string[][] = [
    ["Basic Information"],
    ["Assurance"],
    [
      "Total Aligned Revenue",
      "Total Revenue",
      "Total Eligible Revenue",
      "Total Non-Aligned Revenue",
      "Total Non-Eligible Revenue",
    ],
    [
      "Total Aligned CapEx",
      "Total CapEx",
      "Total Eligible CapEx",
      "Total Non-Aligned CapEx",
      "Total Non-Eligible CapEx",
    ],
    ["Total Aligned OpEx", "Total OpEx", "Total Eligible OpEx", "Total Non-Aligned OpEx", "Total Non-Eligible OpEx"],
  ];

  const subcategoryDataTestList: string[][] = [
    ["_basicInformation"],
    ["assurance"],
    ["totalAlignedShare", "totalAmount", "totalEligibleShare", "totalNonAlignedShare", "totalNonEligibleShare"],
    ["totalAlignedShare", "totalAmount", "totalEligibleShare", "totalNonAlignedShare", "totalNonEligibleShare"],
    ["totalAlignedShare", "totalAmount", "totalEligibleShare", "totalNonAlignedShare", "totalNonEligibleShare"],
  ];

  it("Check order of the displayed KPIs and category entries", () => {
    const singleMockDataAndMetaInfo = new DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel(mockData);
    const dataAndMetaInfo: Array<DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel> = [
      singleMockDataAndMetaInfo,
    ];

    cy.mountWithPlugins(ThreeLayerDataTable, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        dataModel: newEuTaxonomyForNonFinancialsDisplayDataModel,
        dataAndMetaInfo: dataAndMetaInfo,
      },
    }).then(() => {
      cy.get("[data-test='TwoLayerTest']")
        .eq(0)
        .get(" [data-test='_basicInformation'")
        .should("contain", "Basic Information");

      cy.get("[data-test='TwoLayerTest']")
        .get(".d-table-style")
        .each((element, index) => {
          cy.wrap(element).eq(0).eq(0).get(".p-badge").eq(index).should("have.text", kpiList[index]);
        });

      cy.wait(50);

      cy.get("[data-test='TwoLayerTest']")
        .get(".d-table-style")
        .each((element, index) => {
          cy.wrap(element).eq(0).eq(0).get(".p-badge").eq(index).should("not.have.text", kpiListOrderChanged[index]);
        });

      cy.get(`[data-test='${dataTestList[0]}']`).click();

      cy.wait(300);
      let row;
      for (let i = 1; i < dataTestList.length; i++) {
        row = subcategoryDataTestList[i];
        cy.get(`[data-test='${dataTestList[i]}']`).click();
        for (let j = 0; j < row.length; j++) {
          cy.get(".p-rowgroup-header")
            .filter(":visible")
            .eq(j)
            .get(`span[id="${row[j]}"]`)
            .should("contain", `${subcategoryList[i][j]}`);
        }
        cy.get(`[data-test='${dataTestList[i]}']`).click();
        cy.wait(50);
      }
    });
  });
});
