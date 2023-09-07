import EuTaxonomyForNonFinancialsPanel from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsPanel.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { type DataAndMetaInformationEuTaxonomyDataForNonFinancials, DataTypeEnum } from "@clients/backend";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { roundNumber } from "@/utils/NumberConversionUtils";
import { formatNumberToReadableFormat, formatAmountWithCurrency } from "@/utils/ValuesConversionUtils";

describe("Component test for the EUTaxonomy Page", () => {
  let mockedBackendDataForTest: Array<DataAndMetaInformationEuTaxonomyDataForNonFinancials>;

  before(function () {
    cy.fixture("EuTaxonomyForNonFinancialsMocks.json").then(
      (mockedBackendResponse: DataAndMetaInformationEuTaxonomyDataForNonFinancials[]) => {
        mockedBackendDataForTest = mockedBackendResponse;
      },
    );
  });

  it("Check if the panel fetches, converts and displays data correctly", () => {
    const mockCompanyId = "mock-company-Id";
    cy.intercept(
      `/api/data/${DataTypeEnum.EutaxonomyNonFinancials}/companies/${mockCompanyId}`,
      mockedBackendDataForTest,
    );
    cy.mountWithPlugins(EuTaxonomyForNonFinancialsPanel, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        companyId: "mock-company-Id",
      },
    }).then(() => {
      const capexOfDatasetAlpha = assertDefined(mockedBackendDataForTest[0].data.capex);
      const capexOfDatasetBeta = assertDefined(mockedBackendDataForTest[1].data.capex);
      const capexOfDatasetGamma = assertDefined(mockedBackendDataForTest[2].data.capex);

      const betaTotalAlignedCapexPercentage = roundNumber(
        assertDefined(capexOfDatasetBeta.alignedShare?.relativeShareInPercent) * 100,
        2,
      );

      const gammaTotalAlignedCapexAbsoluteShareString =
        Math.round(assertDefined(capexOfDatasetGamma.alignedShare?.absoluteShare?.amount)).toString() +
        ` ${assertDefined(capexOfDatasetGamma.alignedShare?.absoluteShare?.currency)}`;

      const alphaContributionToClimateChangeMitigation = roundNumber(
        assertDefined(capexOfDatasetAlpha.substantialContributionToClimateChangeMitigationInPercent) * 100,
        2,
      );

      const gammaContributionToClimateChangeMitigation = roundNumber(
        assertDefined(capexOfDatasetGamma.substantialContributionToClimateChangeMitigationInPercent) * 100,
        2,
      );

      cy.get(`[data-test='CapEx']`).click();

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

      cy.get('span[data-test="CapEx"]')
        .parent()
        .parent()
        .parent()
        .contains("td", "Substantial Contribution to Climate Change Mitigation")
        .siblings("td")
        .find("span")
        .should("contain", alphaContributionToClimateChangeMitigation)
        .should("contain", gammaContributionToClimateChangeMitigation);
    });
  });
});
