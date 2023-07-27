import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import {
  DataMetaInformation,
  SmeData,
  CompanyAssociatedDataSmeData,
  DataAndMetaInformationSmeData,
  DataTypeEnum,
} from "@clients/backend";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import SmePanel from "@/components/resources/frameworkDataSearch/sme/SmePanel.vue";
import { threeLayerTable } from "@sharedUtils/components/ThreeLayerTable";
import { QaStatus } from "@clients/qaservice";
import { assertDefined } from "@/utils/TypeScriptUtils";

describe("Component tests for SmePanel", () => {
  let preparedFixtures: Array<FixtureData<SmeData>>;
  const companyId = "mock-company-id";

  before(function () {
    cy.fixture("CompanyInformationWithSmePreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<SmeData>>;
    });
  });

  it("Check Sme view page for company with one Sme data set", () => {
    const preparedFixture = getPreparedFixture("SME-year-2023", preparedFixtures);
    const smeData = preparedFixture.t;

    cy.intercept("/api/data/sme/mock-data-id", {
      companyId: companyId,
      reportingPeriod: preparedFixture.reportingPeriod,
      data: smeData,
    } as CompanyAssociatedDataSmeData);
    cy.mountWithPlugins(SmePanel, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          companyId: companyId,
          singleDataMetaInfoToDisplay: {
            dataId: "mock-data-id",
            reportingPeriod: preparedFixture.reportingPeriod,
          } as DataMetaInformation,
        };
      },
    });
    cy.get(
      threeLayerTable.getFieldByContentSelector(smeData.general.basicInformation.numberOfEmployees.toString())
    ).should("exist");

    cy.get(threeLayerTable.getFieldByContentSelector("< 1%")).should("not.exist");
    threeLayerTable.subcategoryIsNotVisible("Investments");
    threeLayerTable.toggleCategory("POWER");
    cy.get(threeLayerTable.getFieldByContentSelector("< 1%")).should("not.exist");
    threeLayerTable.toggleSubcategory("Investments");
    cy.get(threeLayerTable.getFieldByContentSelector("< 1%")).should("exist");
    cy.get(threeLayerTable.getFieldByContentSelector("< 25%")).should("not.exist");
    threeLayerTable.toggleSubcategory("Consumption");
    cy.get(threeLayerTable.getFieldByContentSelector("< 25%")).should("exist");
  });

  /**
   * Generates a partly predefined DataMetaInformation object
   * @param dataId the data ID to use in the meta information
   * @param reportingPeriod the reporting period to use in the meta information
   * @returns the constructed DataMetaInformation object
   */
  function generateMetaInformation(dataId: string, reportingPeriod: string): DataMetaInformation {
    return {
      dataId: dataId,
      companyId: companyId,
      reportingPeriod: reportingPeriod,
      dataType: DataTypeEnum.Sme,
      uploadTime: 0,
      currentlyActive: true,
      qaStatus: QaStatus.Accepted,
    };
  }

  it("Check that headquarter addresses are correctly displayed on the sme view page", () => {
    const minimumAddressFixture = getPreparedFixture("SME-minimum-address", preparedFixtures);
    const maximumAddressFixture = getPreparedFixture("SME-maximum-address", preparedFixtures);

    cy.intercept(`/api/data/sme/companies/${companyId}`, [
      {
        metaInfo: generateMetaInformation("minimum-address-data-id", minimumAddressFixture.reportingPeriod),
        data: minimumAddressFixture.t,
      },
      {
        metaInfo: generateMetaInformation("maximum-address-data-id", maximumAddressFixture.reportingPeriod),
        data: maximumAddressFixture.t,
      },
    ] as DataAndMetaInformationSmeData[]);
    cy.mountWithPlugins(SmePanel, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          companyId: companyId,
        };
      },
    });
    const minimumAddress = minimumAddressFixture.t.general.basicInformation.addressOfHeadquarters;
    cy.get("td span[data-test='addressOfHeadquarters']")
      .parent()
      .siblings()
      .eq(0)
      .should("have.text", `${minimumAddress.city}\n${minimumAddress.country}`);
    const maximumAddress = maximumAddressFixture.t.general.basicInformation.addressOfHeadquarters;
    cy.get("td span[data-test='addressOfHeadquarters']")
      .parent()
      .siblings()
      .eq(1)
      .should(
        "have.text",
        `${assertDefined(maximumAddress.streetAndHouseNumber)}\n${assertDefined(maximumAddress.postalCode)} ${
          maximumAddress.city
        }\n${assertDefined(maximumAddress.state)}, ${maximumAddress.country}`
      );
  });
});
