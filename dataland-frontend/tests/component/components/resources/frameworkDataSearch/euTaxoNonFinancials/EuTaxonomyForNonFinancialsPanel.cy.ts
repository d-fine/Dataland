import EuTaxonomyForNonFinancialsPanel from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsPanel.vue";
import ShowMultipleReportsBanner from "@/components/resources/frameworkDataSearch/ShowMultipleReportsBanner.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import {
  Activity,
  type DataAndMetaInformationEuTaxonomyDataForNonFinancials,
  DataTypeEnum,
  type EuTaxonomyAlignedActivity,
  type EuTaxonomyDetailsPerCashFlowType,
} from "@clients/backend";
import { assertDefined } from "@/utils/TypeScriptUtils";
import type { CompanyReport } from "@clients/backend";
import { roundNumber } from "@/utils/NumberConversionUtils";
import { formatAmountWithCurrency } from "@/utils/Formatter";

describe("Component test for the Eu-Taxonomy-Non-Financials view page", () => {
  let mockedBackendDataForTest: DataAndMetaInformationEuTaxonomyDataForNonFinancials[];

  let capexOfDatasetAlpha: EuTaxonomyDetailsPerCashFlowType;

  let capexOfDatasetBeta: EuTaxonomyDetailsPerCashFlowType;

  let capexOfDatasetGamma: EuTaxonomyDetailsPerCashFlowType;
  let gammaReportingPeriod: string;
  let gammaCapexFirstAlignedActivity: EuTaxonomyAlignedActivity;

  before(function () {
    cy.fixture("EuTaxonomyForNonFinancialsMocks.json").then(
      (mockedBackendResponse: DataAndMetaInformationEuTaxonomyDataForNonFinancials[]) => {
        mockedBackendDataForTest = mockedBackendResponse;

        const revenueOfDatasetAlpha = assertDefined(mockedBackendDataForTest[0].data.revenue);
        const revenueOfDatasetAlphaTotalAmount = assertDefined(revenueOfDatasetAlpha.totalAmount);
        revenueOfDatasetAlphaTotalAmount.value = 0;
        revenueOfDatasetAlphaTotalAmount.unit = "EUR";
        capexOfDatasetAlpha = assertDefined(mockedBackendDataForTest[0].data.capex);

        capexOfDatasetBeta = assertDefined(mockedBackendDataForTest[1].data.capex);

        capexOfDatasetGamma = assertDefined(mockedBackendDataForTest[2].data.capex);
        gammaReportingPeriod = mockedBackendDataForTest[2].metaInfo.reportingPeriod;
        const gammaCapexAlignedActivities = assertDefined(capexOfDatasetGamma.alignedActivities);
        if (gammaCapexAlignedActivities.length < 1) {
          throw new Error(
            "Aligned activities list for capex of gamma dataset needs at least one element for this test to make sense.",
          );
        }
        gammaCapexAlignedActivities.splice(1);
        gammaCapexFirstAlignedActivity = gammaCapexAlignedActivities[0];
        gammaCapexFirstAlignedActivity.activityName = Activity.Afforestation;
        gammaCapexFirstAlignedActivity.substantialContributionToClimateChangeAdaptionInPercent = 0;
      },
    );
  });

  it("Check if the panel fetches, converts and displays data correctly", () => {
    const mockCompanyId = "mock-company-Id";
    cy.intercept(
      `/api/data/${DataTypeEnum.EutaxonomyNonFinancials}/companies/${mockCompanyId}`,
      mockedBackendDataForTest,
    );
    cy.mountWithDialog(
      EuTaxonomyForNonFinancialsPanel,
      {
        keycloak: minimalKeycloakMock({}),
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
      },
      {
        companyId: "mock-company-Id",
      },
    ).then(() => {
      const alphaContributionToClimateChangeMitigation = roundNumber(
        assertDefined(capexOfDatasetAlpha.substantialContributionToClimateChangeMitigationInPercent) * 100,
        2,
      );
      const betaTotalAlignedCapexPercentage = roundNumber(
        assertDefined(capexOfDatasetBeta.alignedShare?.relativeShareInPercent) * 100,
        2,
      );
      const gammaTotalAlignedCapexAbsoluteShareString = formatAmountWithCurrency(
        assertDefined(capexOfDatasetGamma.alignedShare?.absoluteShare),
      );
      const gammaContributionToClimateChangeMitigation = roundNumber(
        assertDefined(capexOfDatasetGamma.substantialContributionToClimateChangeMitigationInPercent) * 100,
        2,
      );

      cy.get(`[data-test='Revenue']`).click();

      cy.get('tr:has(td > span:contains("Total Revenue"))')
        .next("tr")
        .next("tr")
        .find("span")
        .should("contain", "Value");

      cy.get('tr:has(td > span:contains("Total Revenue"))')
        .next("tr")
        .next("tr")
        .find("td")
        .eq(1)
        .invoke("text")
        .should("match", /^0$/);

      cy.get('[data-test="CapEx"]').click();
      cy.get('tr:has(td > span:contains("Aligned CapEx"))')
        .first()
        .next("tr")
        .find("span")
        .should("contain", "Percentage")
        .should("contain", betaTotalAlignedCapexPercentage);

      cy.get('tr:has(td > span:contains("Aligned CapEx"))')
        .first()
        .next("tr")
        .next("tr")
        .find("span")
        .should("contain", "Absolute share")
        .should("contain", gammaTotalAlignedCapexAbsoluteShareString);

      cy.get('[data-test="CapEx"]')
        .parent()
        .parent()
        .parent()
        .contains("td", "Substantial Contribution to Climate Change Mitigation")
        .siblings("td")
        .find("span")
        .should("contain", alphaContributionToClimateChangeMitigation)
        .should("contain", gammaContributionToClimateChangeMitigation);

      cy.get(`[data-test="${gammaReportingPeriod}_capex_alignedActivities"]`).click();
      cy.get("tr")
        .contains("td", assertDefined(gammaCapexFirstAlignedActivity.activityName))
        .nextAll()
        .eq(4)
        .invoke("text")
        .should("match", /^0 %$/);
    });
  });

  it("Checks if the reports banner and the corresponding modal is properly displayed", () => {
    const reportsAndReportingPeriods =
      extractReportsAndReportingPeriodsFromDataAndMetaInfoSets(mockedBackendDataForTest);
    const hightestIndexOfReportingPeriods = calculateIndexOfNewestReportingPeriod(reportsAndReportingPeriods[1]);
    cy.mountWithDialog(
      ShowMultipleReportsBanner,
      {
        keycloak: minimalKeycloakMock({}),
      },
      {
        reports: reportsAndReportingPeriods[0],
        reportingPeriods: reportsAndReportingPeriods[1],
      },
    ).then(() => {
      cy.get(`[data-test="frameworkNewDataTableTitle"`).contains(
        "Data extracted from the company report. Company Reports",
      );
      cy.get('[data-test="documentLinkTest"]').contains("IntegratedReport");

      cy.get(`[data-test="previousReportsLinkToModal"]`).contains("Previous years reports").click();
      for (let i = 0; i < reportsAndReportingPeriods[1]?.length; i++) {
        if (i != hightestIndexOfReportingPeriods) {
          cy.get(`[data-test="previousReportsList"]`).contains(`Company Reports (${reportsAndReportingPeriods[1][i]})`);
          for (const key in reportsAndReportingPeriods[0][i]) {
            cy.get(`[data-test='Report-Download-${key}']`).contains(key);
          }
        }
      }
    });
  });
});

/**
 * Extracts the reports and reporting periods for all data sets.
 * @param dataAndMetaInfoSets array of data sets including meta information
 * @returns array containing an array of company reports and an array of the corresponding reporting periods
 * as strings
 */
export function extractReportsAndReportingPeriodsFromDataAndMetaInfoSets(
  dataAndMetaInfoSets: Array<DataAndMetaInformationEuTaxonomyDataForNonFinancials>,
): [Array<{ [p: string]: CompanyReport } | undefined>, Array<string>] {
  const reportingPeriods = [];
  let tempReportingPeriod: string | undefined;
  for (const dataAndMetaInfoSet of dataAndMetaInfoSets) {
    tempReportingPeriod = dataAndMetaInfoSet.metaInfo.reportingPeriod;
    if (tempReportingPeriod) {
      reportingPeriods.push(tempReportingPeriod);
    }
  }
  const allReports: Array<{ [p: string]: CompanyReport } | undefined> = dataAndMetaInfoSets.map(
    (dataAndMetaInfoSet) => dataAndMetaInfoSet?.data?.general?.referencedReports,
  );
  return [allReports, reportingPeriods];
}

/**
 * Returns the index of the with the newest reporting period in the array containing all reporting periods.
 * @param reportingPeriods array containing all reporting periods.
 * @returns index of the newest reporting period
 */
export function calculateIndexOfNewestReportingPeriod(reportingPeriods: Array<string>): number {
  let indexOfHighestReportingPeriod = 0;
  let tempHighestReportingPeriodNumber = 0;
  for (let i = 0; i < reportingPeriods.length; i++) {
    if (Number(reportingPeriods[i]) > tempHighestReportingPeriodNumber) {
      tempHighestReportingPeriodNumber = Number(reportingPeriods[i]);
      indexOfHighestReportingPeriod = i;
    }
  }
  return indexOfHighestReportingPeriod;
}
