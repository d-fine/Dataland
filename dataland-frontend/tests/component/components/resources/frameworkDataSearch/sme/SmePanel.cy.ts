import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import {
  type DataMetaInformation,
  type SmeData,
  type CompanyAssociatedDataSmeData,
  type DataAndMetaInformationSmeData,
  DataTypeEnum,
  PercentRangeForInvestmentsInEnergyEfficiency,
  PercentRangeForEnergyConsumptionCoveredByOwnRenewablePower,
} from "@clients/backend";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import SmePanel from "@/components/resources/frameworkDataSearch/sme/SmePanel.vue";
import { threeLayerTable } from "@sharedUtils/components/ThreeLayerTable";
import { QaStatus } from "@clients/qaservice";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { smeDataModel } from "@/components/resources/frameworkDataSearch/sme/SmeDataModel";

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
    cy.mountWithDialog(
      SmePanel,
      {
        keycloak: minimalKeycloakMock({}),
      },
      {
        companyId: companyId,
        singleDataMetaInfoToDisplay: {
          dataId: "mock-data-id",
          reportingPeriod: preparedFixture.reportingPeriod,
        } as DataMetaInformation,
      },
    );
    cy.get(
      threeLayerTable.getFieldByContentSelector(smeData.general.basicInformation.numberOfEmployees.toString()),
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

    threeLayerTable.getFieldByTestIdentifier("sector").find("a").eq(0).click();
    cy.get(".p-dialog-header-close").trigger("click");

    threeLayerTable.toggleSubcategory("Company Financials");
    threeLayerTable.getFieldByTestIdentifier("revenueInEUR").should("contain.text", "0 MM");
    threeLayerTable.getFieldByTestIdentifier("operatingCostInEUR").should("contain.text", "1 MM");
    threeLayerTable.getFieldByTestIdentifier("capitalAssetsInEUR").should("contain.text", "2 MM");
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
    threeLayerTable
      .getFieldByTestIdentifier("addressOfHeadquarters", 0)
      .should("have.text", `${minimumAddress.city}\n${minimumAddress.country}`);
    const maximumAddress = maximumAddressFixture.t.general.basicInformation.addressOfHeadquarters;
    threeLayerTable
      .getFieldByTestIdentifier("addressOfHeadquarters", 1)
      .should(
        "have.text",
        `${assertDefined(maximumAddress.streetAndHouseNumber)}\n${assertDefined(maximumAddress.postalCode)} ${
          maximumAddress.city
        }\n${assertDefined(maximumAddress.state)}, ${maximumAddress.country}`,
      );
  });

  /**
   * Asserts that an enum model from the backend coincedes with the one extracted from the generated data model
   * @param backendModel the backend model representing the enum to check
   * @param categoryName the name of the category containing a field with values from the enum
   * @param subcategoryName the name of the subcategory containing a field with values from the enum
   * @param fieldName the field with values from the enum
   */
  function assertBackendModelEqualsGeneratedDataModel(
    backendModel: object,
    categoryName: string,
    subcategoryName: string,
    fieldName: string,
  ): void {
    const modelOptions = smeDataModel
      .find((category) => category.name == categoryName)!
      .subcategories.find((subcategory) => subcategory.name == subcategoryName)!
      .fields.find((field) => field.name == fieldName)!.options!;
    const enumValuesFromOptions = new Set(modelOptions.map((option) => option.value));
    const enumValuesFromBackend = new Set(Object.values(backendModel));
    expect(enumValuesFromBackend).to.deep.equal(enumValuesFromOptions);
  }

  it("Check if the enum in the data model are equal to the ones in the backend", () => {
    assertBackendModelEqualsGeneratedDataModel(
      PercentRangeForInvestmentsInEnergyEfficiency,
      "power",
      "investments",
      "percentageRangeForInvestmentsInEnhancingEnergyEfficiency",
    );
    assertBackendModelEqualsGeneratedDataModel(
      PercentRangeForEnergyConsumptionCoveredByOwnRenewablePower,
      "power",
      "consumption",
      "percentageRangeForEnergyConsumptionCoveredByOwnRenewablePowerGeneration",
    );
  });
});
