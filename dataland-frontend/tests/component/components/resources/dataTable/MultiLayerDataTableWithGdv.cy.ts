import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import {
  type BaseDataPointString,
  type CompanyAssociatedDataGdvData,
  type DataMetaInformation,
  DataTypeEnum,
  type GdvData,
} from "@clients/backend";

import { formatNumberToReadableFormat } from "@/utils/Formatter";
import { type DataAndMetaInformation } from "@/api-models/DataAndMetaInformation";
import MultiLayerDataTableFrameworkPanel from "@/components/resources/frameworkDataSearch/frameworkPanel/MultiLayerDataTableFrameworkPanel.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { GdvViewConfiguration } from "@/frameworks/gdv/ViewConfig";
import {
  getCellValueContainer,
  getSectionHead,
} from "@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils";

describe("Component Test for the GDV-VÖB view Page with its componenets", () => {
  let preparedFixtureForTest: FixtureData<GdvData>;
  const companyId = "mock-company-id";
  before(function () {
    cy.fixture("CompanyInformationWithGdvPreparedFixtures").then(function (jsonContent) {
      const preparedFixtures = jsonContent as Array<FixtureData<GdvData>>;
      preparedFixtureForTest = getPreparedFixture("Gdv-dataset-with-no-null-fields", preparedFixtures);
    });
  });

  it("Check that on the GDV-VÖB view Page the rolling window component works properly", () => {
    cy.intercept("/api/data/gdv/mock-data-id", {
      companyId: companyId,
      reportingPeriod: preparedFixtureForTest.reportingPeriod,
      data: preparedFixtureForTest.t,
    } as CompanyAssociatedDataGdvData);
    mountGDVFrameworkFromFakeFixture([preparedFixtureForTest]);
    getSectionHead("Umwelt").click();
    getSectionHead("Treibhausgasemissionen").click();
    getCellValueContainer("Treibhausgas-Berichterstattung und Prognosen").click();

    cy.get("div").contains("Historische Daten");
    cy.get("div").contains("Aktuelles Jahr");
    cy.get("div").contains("Prognosen");
    const modalDatasets =
      preparedFixtureForTest.t.umwelt?.treibhausgasemissionen?.treibhausgasBerichterstattungUndPrognosen?.yearlyData;
    for (const dataSetOfOneYear in modalDatasets) {
      cy.get("div").contains(formatNumberToReadableFormat(modalDatasets[dataSetOfOneYear].scope1));
      cy.get("div").contains(formatNumberToReadableFormat(modalDatasets[dataSetOfOneYear].scope2));
      cy.get("div").contains(formatNumberToReadableFormat(modalDatasets[dataSetOfOneYear].scope3));
    }
  });

  it("Check that on the GDV-VÖB view Page the string for datatable component works properly", () => {
    cy.intercept("/api/data/gdv/mock-data-id", {
      companyId: companyId,
      reportingPeriod: preparedFixtureForTest.reportingPeriod,
      data: preparedFixtureForTest.t,
    } as CompanyAssociatedDataGdvData);
    mountGDVFrameworkFromFakeFixture([preparedFixtureForTest]);
    getSectionHead("Unternehmensführung/ Governance").click();
    getSectionHead("Sonstige").eq(1).click();
    getCellValueContainer("Wirtschaftsprüfer").contains(
      preparedFixtureForTest.t.unternehmensfuehrungGovernance?.sonstige?.wirtschaftspruefer as string,
    );
  });

  it("Check that on the GDV-VÖB view Page the list base data point component works properly", () => {
    cy.intercept("/api/data/gdv/mock-data-id", {
      companyId: companyId,
      reportingPeriod: preparedFixtureForTest.reportingPeriod,
      data: preparedFixtureForTest.t,
    } as CompanyAssociatedDataGdvData);
    mountGDVFrameworkFromFakeFixture([preparedFixtureForTest]);

    const esgBerichte: Array<BaseDataPointString> = preparedFixtureForTest.t.allgemein?.esgBerichte
      ?.aktuelleBerichte as Array<BaseDataPointString>;

    getSectionHead("ESG Berichte").click();
    getCellValueContainer("Aktuelle Berichte").click();

    cy.get("span").contains("Beschreibung des Berichts");
    for (const singleEsgBericht of esgBerichte) {
      cy.get("div").contains(singleEsgBericht.value as string);
      cy.get("div").contains(singleEsgBericht.dataSource?.fileName as string);
    }
    cy.get('span[data-test="Report-Download-Policy"]').should("exist");
  });
});

/**
 *
 * Mounts the MultiLayerDataTableFrameworkPanel with the given dataset for the GDV framework
 * @param fixtureDatasetsForDisplay the datasets from the fixtures to mount
 * @param companyId company ID of the mocked requests
 * @param reviewMode toggles the reviewer mode
 * @returns the component mounting chainable
 */
function mountGDVFrameworkFromFakeFixture(
  fixtureDatasetsForDisplay: Array<FixtureData<GdvData>>,
  companyId = "mock-company-id",
  reviewMode = false,
): Cypress.Chainable {
  const convertedDataAndMetaInformation: Array<DataAndMetaInformation<GdvData>> = fixtureDatasetsForDisplay.map(
    (it, idx) => {
      const metaInformation: DataMetaInformation = {
        dataId: `data-id-${idx}`,
        companyId: companyId,
        dataType: DataTypeEnum.Gdv,
        uploadTime: 0,
        reportingPeriod: it.reportingPeriod,
        qaStatus: "Accepted",
        currentlyActive: true,
      };
      return {
        data: it.t,
        metaInfo: metaInformation,
      };
    },
  );

  return mountMLDTForGdvPanel(convertedDataAndMetaInformation, companyId, reviewMode);
}

/**
 * Mounts the MultiLayerDataTableFrameworkPanel with the given dataset
 * @param datasetsToDisplay datasets to mount
 * @param companyId company ID of the mocked requests
 * @param reviewMode toggles the reviewer mode
 * @returns the component mounting chainable
 */
export function mountMLDTForGdvPanel(
  datasetsToDisplay: Array<DataAndMetaInformation<GdvData>>,
  companyId = "mock-company-id",
  reviewMode = false,
): Cypress.Chainable {
  cy.intercept(`/api/data/${DataTypeEnum.Gdv}/companies/${companyId}`, datasetsToDisplay);
  return cy.mountWithDialog(
    MultiLayerDataTableFrameworkPanel,
    {
      keycloak: minimalKeycloakMock({}),
    },
    {
      companyId: companyId,
      frameworkIdentifier: DataTypeEnum.Gdv,
      displayConfiguration: GdvViewConfiguration,
      inReviewMode: reviewMode,
    },
  );
}
