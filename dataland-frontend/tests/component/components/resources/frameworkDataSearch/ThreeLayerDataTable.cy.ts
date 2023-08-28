import ThreeLayerDataTable from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { euTaxonomyForNonFinancialsDisplayDataModel } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsDisplayDataModel";
import { DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsViewModel";
import { type DataAndMetaInformationEuTaxonomyDataForNonFinancials } from "@clients/backend";
import { euTaxonomyForNonFinancialsModalColumnHeaders } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsModalColumnHeaders";
import { assertDefined } from "@/utils/TypeScriptUtils";
describe("Component test for the NewEUTaxonomy Page", () => {
  let mockedDataForTest: Array<DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel>;

  before(function () {
    cy.fixture("EuTaxonomyForNonFinancialsMocks.json").then(
      (mockedBackendResponses: DataAndMetaInformationEuTaxonomyDataForNonFinancials[]) => {
        const singleMockDataAndMetaInfo = new DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel(
          mockedBackendResponses[0],
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

  /**
   * Expands the view page and opens the modal page.
   * @param categoryToExpand name of the category that has to be expanded, since there are multiple modal pages.
   * @param fieldToClick
   */
  function expandViewPageAndOpenModal(categoryToExpand = "Revenue", fieldToClick = "totalAlignedShare"): void {
    toggleCategoryByClick("Basic Information");
    toggleCategoryByClick(`${categoryToExpand}`);
    cy.get(`[data-test='${fieldToClick}']`).filter(":visible").click();
    cy.get(`[data-test='${fieldToClick}']`)
      .filter(":visible")
      .get("em")
      .filter(":visible")
      .eq(-1)
      .should("have.text", " dataset ")
      .click();
  }

  it("Check order of the displayed KPIs and its entries", () => {
    cy.mountWithPlugins(ThreeLayerDataTable, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        dataModel: euTaxonomyForNonFinancialsDisplayDataModel,
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
    const capexOfDataset = assertDefined(mockedDataForTest[0].data.capex);
    const revenueOfDataset = assertDefined(mockedDataForTest[0].data.revenue);
    const revenueAlignedActivity = assertDefined(revenueOfDataset.totalAlignedShare?.alignedActivities)[0];
    const revenueAlignedActivitiesName = assertDefined(revenueAlignedActivity?.activityName);

    cy.mountWithDialog(
      ThreeLayerDataTable,
      {
        keycloak: minimalKeycloakMock({}),
      },
      {
        dataModel: euTaxonomyForNonFinancialsDisplayDataModel,
        dataAndMetaInfo: mockedDataForTest,
        modalColumnHeaders: euTaxonomyForNonFinancialsModalColumnHeaders,
        sortBySubcategoryKey: false,
      },
    ).then(() => {
      expandViewPageAndOpenModal("Revenue", "totalAlignedShare");
      checkDuplicateFields();
      cy.get("table").find(`tr:contains("DNSH Criteria")`);

      cy.get("table").find(`tr:contains("Climate change mitigation")`);
      cy.get("table").find(`tr:contains("Climate change adaptation")`);
      cy.get("table").find(`tr:contains("Water and marine resources")`);
      cy.get("table").find(`tr:contains("Circular economy")`);
      cy.get("table").find(`tr:contains("Pollution prevention")`);
      cy.get("table").find(`tr:contains("Biodiversity and ecosystems")`);
      cy.get("table").find(`tr:contains("Minimum safeguards")`);

      cy.get("table").find(`tr:contains("20%")`);
      cy.get("table").find(`tr:contains("Yes")`);
      cy.get("table").find(`tr:contains("No")`);

      const capexAlignedActivitiesShareInPercent: number = assertDefined(
        capexOfDataset.totalAlignedShare?.relativeShareInPercent,
      );

      cy.get("table").find(`tr:contains("${revenueAlignedActivitiesName}")`);
      cy.get("table").find(`tr:contains("${capexAlignedActivitiesShareInPercent}")`);
    });
  });

  it("Opens the non-aligned activities modal and checks that it works as intended", () => {
    const capexOfDataset = assertDefined(mockedDataForTest[0].data.capex);
    const capexNonAlignedActivities = assertDefined(capexOfDataset.totalNonAlignedShare?.nonAlignedActivities)[0];
    const capexNonAlignedActivitiesName = assertDefined(capexNonAlignedActivities.activityName);

    const capexNonAlignedActivitiesShareInPercent = assertDefined(
      capexOfDataset.totalNonAlignedShare?.relativeShareInPercent,
    );
    const capexNonAlignedActivitiesNaceCodes: string = assertDefined(capexNonAlignedActivities.naceCodes)[0];

    cy.mountWithDialog(
      ThreeLayerDataTable,
      {
        keycloak: minimalKeycloakMock({}),
      },
      {
        dataModel: euTaxonomyForNonFinancialsDisplayDataModel,
        dataAndMetaInfo: mockedDataForTest,
        modalColumnHeaders: euTaxonomyForNonFinancialsModalColumnHeaders,
        sortBySubcategoryKey: false,
      },
    ).then(() => {
      expandViewPageAndOpenModal("CapEx", "totalNonAlignedShare");
      checkDuplicateFields();

      cy.get("table").find(`tr:contains("${capexNonAlignedActivitiesName}")`);
      cy.get("table").find(`tr:contains("${capexNonAlignedActivitiesShareInPercent}")`);
      cy.get("table").find(`tr:contains("${capexNonAlignedActivitiesNaceCodes}")`);
    });
  });
});

/**
 * Searches for common fields that appear on multiple modal windows (to avoid code duplicaitons).
 */
function checkDuplicateFields(): void {
  cy.get("table").find(`tr:contains("Activity")`);
  cy.get("table").find(`tr:contains("NACE Code(s)")`);
  cy.get("table").find(`tr:contains("Revenue")`);
  cy.get("table").find(`tr:contains("Revenue (%)")`);
}
