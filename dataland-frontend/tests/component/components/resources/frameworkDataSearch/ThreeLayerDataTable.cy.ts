import ThreeLayerDataTable from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { euTaxonomyForNonFinancialsDisplayDataModel } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsDisplayDataModel";
import { p2pDataModel } from "@/components/resources/frameworkDataSearch/p2p/P2pDataModel";
import { DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsViewModel";
import {
  DataTypeEnum,
  type DataAndMetaInformationEuTaxonomyDataForNonFinancials,
  P2pSector,
  type PathwaysToParisData,
  type DataMetaInformation,
} from "@clients/backend";
import { euTaxonomyForNonFinancialsModalColumnHeaders } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsModalColumnHeaders";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { roundNumber } from "@/utils/NumberConversionUtils";
import { formatAmountWithCurrency } from "@/utils/Formatter";
import { getViewModelWithIdentityApiModel } from "@/components/resources/ViewModel";
import { formatValueForDisplay } from "@/components/resources/frameworkDataSearch/p2p/P2pFormatValueForDisplay";

describe("Component test for ThreeLayerDataTable", () => {
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

  const expectedOrderOfCategories: string[] = ["GENERAL", "ASSURANCE", "REVENUE", "CAPEX", "OPEX"];
  const dataTestTagsOfCategories: string[] = ["General", "Assurance", "Revenue", "CapEx", "OpEx"];

  /**
   * Creates a list with the labels of the subcategories inside a cash flow category in the right order.
   * @param categoryName is the name of the cash flow category
   * @returns an array with the subcategory labels in the right order
   */
  function buildExpectedOrderOfSubcategoriesForCategory(categoryName: "Revenue" | "CapEx" | "OpEx"): string[] {
    return [
      `Aligned ${categoryName}`,
      `Total ${categoryName}`,
      `Eligible ${categoryName}`,
      `Non-Aligned ${categoryName}`,
      `Non-Eligible ${categoryName}`,
    ];
  }

  const expectedOrderOfSubcategoriesGroupedByCategories: string[][] = [
    ["General"],
    ["Assurance"],
    buildExpectedOrderOfSubcategoriesForCategory("Revenue"),
    buildExpectedOrderOfSubcategoriesForCategory("CapEx"),
    buildExpectedOrderOfSubcategoriesForCategory("OpEx"),
  ];

  const dataTestTagsOfCashFlowSubcategory = [
    "alignedShare",
    "totalAmount",
    "eligibleShare",
    "nonAlignedShare",
    "nonEligibleShare",
  ];

  const dataTestTagsOfSubcategoriesGroupedByCategories: string[][] = [
    ["_general"],
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
   * @param fieldToClick field to click
   */
  function expandViewPageAndOpenModal(categoryToExpand = "Revenue", fieldToClick = "alignedShare"): void {
    toggleCategoryByClick("General");
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

  /**
   * Searches for common column headers that appear on multiple modal windows (to avoid code duplication).
   */
  function validateExistenceOfCommonColumnHeaders(): void {
    cy.get("table").find(`tr:contains("Activity")`);
    cy.get("table").find(`tr:contains("NACE Code(s)")`);
    cy.get("table").find(`tr:contains("Revenue")`);
    cy.get("table").find(`tr:contains("Revenue (%)")`);
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
    const revenueOfDataset = assertDefined(mockedDataForTest[0].data.revenue);
    const revenueFirstAlignedActivity = assertDefined(revenueOfDataset.alignedShare?.alignedActivities)[0];
    const revenueFirstAlignedActivityName = assertDefined(revenueFirstAlignedActivity?.activityName);
    const revenueFirstAlignedActivityRelativeShare = roundNumber(
      assertDefined(revenueFirstAlignedActivity?.share?.relativeShareInPercent) * 100,
      2,
    );
    const revenueFirstAlignedActivityAbsoluteShare = formatAmountWithCurrency(
      assertDefined(revenueFirstAlignedActivity?.share?.absoluteShare),
    );

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
      expandViewPageAndOpenModal("Revenue", "alignedShare");
      validateExistenceOfCommonColumnHeaders();
      cy.get("table").find(`tr:contains("DNSH Criteria")`);
      cy.get("table").find(`tr:contains("Climate Change Mitigation")`);
      cy.get("table").find(`tr:contains("Climate Change Adaptation")`);
      cy.get("table").find(`tr:contains("Water and Marine Resources")`);
      cy.get("table").find(`tr:contains("Circular Economy")`);
      cy.get("table").find(`tr:contains("Pollution Prevention")`);
      cy.get("table").find(`tr:contains("Biodiversity and Ecosystems")`);
      cy.get("table").find(`tr:contains("Minimum safeguards")`);
      cy.get("table").find(`tr:contains("Yes")`);
      cy.get("table").find(`tr:contains("No")`);
      cy.get("table").find(`tr:contains("${revenueFirstAlignedActivityName}")`);
      cy.get("table").find(`tr:contains("${revenueFirstAlignedActivityRelativeShare}")`);
      cy.get("table").find(`tr:contains("${revenueFirstAlignedActivityAbsoluteShare}")`);
    });
  });

  it("Opens the non-aligned activities modal and checks that it works as intended", () => {
    const capexOfDataset = assertDefined(mockedDataForTest[0].data.capex);
    const capexFirstNonAlignedActivity = assertDefined(capexOfDataset.nonAlignedShare?.nonAlignedActivities)[0];
    assertDefined(capexFirstNonAlignedActivity.activityName);
    const capexNonAlignedShareInPercent = assertDefined(capexOfDataset.nonAlignedShare?.relativeShareInPercent);
    const capexFirstNonAlignedActivityNaceCodes: string = assertDefined(capexFirstNonAlignedActivity.naceCodes)[0];
    const capexFirstNonAlignedActivityRelativeShare = roundNumber(
      assertDefined(capexFirstNonAlignedActivity.share?.relativeShareInPercent) * 100,
      2,
    );
    const capexFirstNonAlignedActivityAbsoluteShare = formatAmountWithCurrency(
      assertDefined(capexFirstNonAlignedActivity.share?.absoluteShare),
    );

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
      expandViewPageAndOpenModal("CapEx", "nonAlignedShare");
      validateExistenceOfCommonColumnHeaders();
      cy.get("table").find(
        `tr:contains("Construction, extension and operation of waste water collection and treatment")`,
      );
      cy.get("table").find(`tr:contains("${capexNonAlignedShareInPercent}")`);
      cy.get("table").find(`tr:contains("${capexFirstNonAlignedActivityNaceCodes}")`);
      cy.get("table").find(`tr:contains(${capexFirstNonAlignedActivityRelativeShare})`);
      cy.get("table").find(`tr:contains(${capexFirstNonAlignedActivityAbsoluteShare})`);
    });
  });

  it("Check P2p view page that properties with same name in different KPIs have different values", () => {
    const metaInfo: DataMetaInformation = {
      companyId: "some-fake-ID-123456789",
      dataId: "some-fake-dataId-123456789",
      dataType: DataTypeEnum.P2p,
      reportingPeriod: "2018",
      currentlyActive: true,
      qaStatus: "Accepted",
      uploadTime: 1234567890,
    };

    const p2pData: PathwaysToParisData = {
      general: {
        general: {
          dataDate: "2023-09-30",
          sectors: [P2pSector.Ammonia, P2pSector.Cement],
        },
        governance: {},
        climateTargets: {},
        emissionsPlanning: {},
        investmentPlanning: {},
      },
      ammonia: {
        decarbonisation: {
          energyMix: 0.11,
        },
        defossilisation: {},
      },
      cement: {
        energy: {
          energyMix: 0.22,
        },
        technology: {},
        material: {},
      },
    };

    const expectedAmmoniaDecarbonisationEnergyMix = roundNumber(
      assertDefined(p2pData.ammonia?.decarbonisation?.energyMix) * 100,
      2,
    );
    const expectedCementEnergyEnergyMix = roundNumber(assertDefined(p2pData.cement?.energy?.energyMix) * 100, 2);

    cy.mountWithPlugins(ThreeLayerDataTable, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        dataModel: p2pDataModel,
        dataAndMetaInfo: [getViewModelWithIdentityApiModel<PathwaysToParisData>({ metaInfo, data: p2pData })],
        formatValueForDisplay: formatValueForDisplay,
        modalColumnHeaders: {},
        sortBySubcategoryKey: false,
      },
    }).then(() => {
      toggleCategoryByClick("Ammonia");
      toggleCategoryByClick("decarbonisation");

      toggleCategoryByClick("Cement");
      toggleCategoryByClick("energy");

      cy.get('[data-test="2018_ammonia_energyMix"] span')
        .should("exist")
        .contains(`${expectedAmmoniaDecarbonisationEnergyMix} %`);
      cy.get('[data-test="2018_cement_energyMix"] span').should("exist").contains(`${expectedCementEnergyEnergyMix} %`);
    });
  });
});
