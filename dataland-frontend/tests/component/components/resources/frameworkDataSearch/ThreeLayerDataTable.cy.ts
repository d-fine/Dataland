import ThreeLayerDataTable from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { newEuTaxonomyForNonFinancialsDisplayDataModel } from "@/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsDisplayDataModel";
import { DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel } from "@/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsViewModel";
import {
  type DataAndMetaInformationNewEuTaxonomyDataForNonFinancials,
  LksgData,
  NewEuTaxonomyDataForNonFinancials,
} from "@clients/backend";

describe("Component test for the NewEUTaxonomy Page", () => {
  let mockedDataForTest: Array<DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel>;

  before(function () {
    cy.fixture("NewEuTaxonomyForNonFinancialsMock.json").then(
      (fixtureData: DataAndMetaInformationNewEuTaxonomyDataForNonFinancials) => {
        const mockBackendResponse: DataAndMetaInformationNewEuTaxonomyDataForNonFinancials = fixtureData;
        const singleMockDataAndMetaInfo = new DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel(
          mockBackendResponse,
        );
        mockedDataForTest = [singleMockDataAndMetaInfo];
      },
    );
  });

  const expectedOrderOfCategories: string[] = ["BASIC INFORMATION", "ASSURANCE", "REVENUE", "CAPEX", "OPEX"];
  const dataTestTagsOfCategories: string[] = ["Basic Information", "Assurance", "Revenue", "CapEx", "OpEx"];

  /**
   * Creates a list with the labels of the subcategories inside a cash flow category in the right order.
   * @param categoryName is the name of the cash flow category
   * @returns an array with the subcategory labels in the right order
   */
  function buildExpectedOrderOfSubcategoriesForCategory(categoryName: "Revenue" | "CapEx" | "OpEx"): string[] {
    return [
      `Total Aligned ${categoryName}`,
      `Total ${categoryName}`,
      `Total Eligible ${categoryName}`,
      `Total Non-Aligned ${categoryName}`,
      `Total Non-Eligible ${categoryName}`,
    ];
  }

  const expectedOrderOfSubcategoriesGroupedByCategories: string[][] = [
    ["Basic Information"],
    ["Assurance"],
    buildExpectedOrderOfSubcategoriesForCategory("Revenue"),
    buildExpectedOrderOfSubcategoriesForCategory("CapEx"),
    buildExpectedOrderOfSubcategoriesForCategory("OpEx"),
  ];

  const dataTestTagsOfCashFlowSubcategory = [
    "totalAlignedShare",
    "totalAmount",
    "totalEligibleShare",
    "totalNonAlignedShare",
    "totalNonEligibleShare",
  ];

  const dataTestTagsOfSubcategoriesGroupedByCategories: string[][] = [
    ["_basicInformation"],
    ["assurance"],
    dataTestTagsOfCashFlowSubcategory,
    dataTestTagsOfCashFlowSubcategory,
    dataTestTagsOfCashFlowSubcategory,
  ];

  /**
   * Toggle a category by clicking on it via its data-test tag
   * @param dataTestTagOfCategory the data-test tag of the category
   */
  function toggleCategoryByClick(dataTestTagOfCategory: string): void {
    cy.get(`[data-test='${dataTestTagOfCategory}']`).click();
  }

  it("Check order of the displayed KPIs and its entries", () => {
    cy.mountWithPlugins(ThreeLayerDataTable, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        dataModel: newEuTaxonomyForNonFinancialsDisplayDataModel,
        dataAndMetaInfo: mockedDataForTest,
      },
    }).then(() => {
      cy.get("[data-test='TwoLayerTest']")
        .get(".d-table-style")
        .each((element, index) => {
          cy.wrap(element).eq(0).eq(0).get(".p-badge").eq(index).should("have.text", expectedOrderOfCategories[index]);
        });

      toggleCategoryByClick(dataTestTagsOfCategories[0]);

      /**
       * The goal for the loop is to expand one KPI at a time and check the order of the entries.
       */

      let subcategoriesForCurrentCategory;
      for (let categoryIndex = 1; categoryIndex < dataTestTagsOfCategories.length; categoryIndex++) {
        subcategoriesForCurrentCategory = dataTestTagsOfSubcategoriesGroupedByCategories[categoryIndex];

        toggleCategoryByClick(dataTestTagsOfCategories[categoryIndex]);

        for (let subCategoryIndex = 0; subCategoryIndex < subcategoriesForCurrentCategory.length; subCategoryIndex++) {
          cy.get(".p-rowgroup-header")
            .filter(":visible")
            .eq(subCategoryIndex)
            .get(`span[id="${subcategoriesForCurrentCategory[subCategoryIndex]}"]`)
            .should("contain", `${expectedOrderOfSubcategoriesGroupedByCategories[categoryIndex][subCategoryIndex]}`);
        }
        toggleCategoryByClick(dataTestTagsOfCategories[categoryIndex]);
      }
    });
  });

  it("Opens the aligned activities modal and checks that it works as intended", () => {
    cy.mountWithDialog(
      ThreeLayerDataTable,
      {
        keycloak: minimalKeycloakMock({}),
      },
      {
        dataModel: newEuTaxonomyForNonFinancialsDisplayDataModel,
        dataAndMetaInfo: mockedDataForTest,
      },
    ).then(() => {
      toggleCategoryByClick("CapEx");
      cy.get(`[data-test='totalAlignedShare']`).filter(":visible").click();
      cy.get(`[data-test='totalAlignedShare']`)
        .filter(":visible")
        .find(`a:contains('Show "Aligned CapEx per Activity" ')`)
        .click();

      cy.get("span[data_id='pv_id_61_header']");
      cy.get("table").find(`tr:contains("Activity")`);
      cy.get("table").find(`tr:contains("Code(s)")`);
      cy.get("table").find(`tr:contains("Revenue")`);
      cy.get("table").find(`tr:contains("Climate change mitigation")`);
      cy.get("table").find(`tr:contains("Climate change adaptation")`);
      cy.get("table").find(`tr:contains("Water and marine resources")`);
      cy.get("table").find(`tr:contains("Circular economy")`);
    });
  });
});
