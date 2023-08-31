import EuTaxonomyForNonFinancialsPanel from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsPanel.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { type DataAndMetaInformationEuTaxonomyDataForNonFinancials, DataTypeEnum } from "@clients/backend";
import { assertDefined } from "@/utils/TypeScriptUtils";

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

      const betaTotalAlignedCapexPercentage = capexOfDatasetBeta.alignedShare?.relativeShareInPercent?.toFixed(2);

      const gammaTotalAlignedCapexAbsoluteShareString =
        Math.round(assertDefined(capexOfDatasetGamma.alignedShare?.absoluteShare?.amount)).toString() +
        ` ${assertDefined(capexOfDatasetGamma.alignedShare?.absoluteShare?.currency)}`;

      const alphaContributionToClimateChangeMitigation = assertDefined(
        capexOfDatasetAlpha.substantialContributionToClimateChangeMitigation,
      )
        .toFixed(2)
        .toString();

      const gammaContributionToClimateChangeMitigation = assertDefined(
        capexOfDatasetGamma.substantialContributionToClimateChangeMitigation,
      )
        .toFixed(2)
        .toString();

      cy.get(`[data-test='CapEx']`).click();
      cy.contains("span", "Aligned CapEx").click();

      cy.contains("td", "Percentage").siblings("td").find("span").should("contain", betaTotalAlignedCapexPercentage);

      cy.contains("td", "Absolute share")
        .siblings("td")
        .find("span")
        .should("contain", gammaTotalAlignedCapexAbsoluteShareString);

      cy.contains("td", "Substantial Contribution to Climate Change Mitigation")
        .siblings("td")
        .find("span")
        .should("contain", alphaContributionToClimateChangeMitigation)
        .should("contain", gammaContributionToClimateChangeMitigation);
    });
  });
});
