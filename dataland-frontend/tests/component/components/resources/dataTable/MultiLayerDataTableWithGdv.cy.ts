import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import {
  type CompanyAssociatedDataEsgquestionnaireData,
  type DataMetaInformation,
  DataTypeEnum,
  type EsgquestionnaireData,
} from "@clients/backend";

import { formatNumberToReadableFormat } from "@/utils/Formatter";
import { type DataAndMetaInformation } from "@/api-models/DataAndMetaInformation";
import MultiLayerDataTableFrameworkPanel from "@/components/resources/frameworkDataSearch/frameworkPanel/MultiLayerDataTableFrameworkPanel.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { EsgquestionnaireViewConfiguration } from "@/frameworks/esgquestionnaire/ViewConfig";
import {
  getCellValueContainer,
  getSectionHead,
} from "@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils";
import { assertDefined } from "@/utils/TypeScriptUtils";

describe("Component Test for the GDV-VÖB view Page with its componenets", () => {
  let preparedFixtureForTest: FixtureData<EsgquestionnaireData>;
  const companyId = "mock-company-id";
  before(function () {
    cy.fixture("CompanyInformationWithGdvPreparedFixtures").then(function (jsonContent) {
      const preparedFixtures = jsonContent as Array<FixtureData<EsgquestionnaireData>>;
      preparedFixtureForTest = getPreparedFixture("Gdv-dataset-with-no-null-fields", preparedFixtures);
    });
  });

  it("Check that on the GDV-VÖB view Page the rolling window component works properly", () => {
    cy.intercept("/api/data/gdv/mock-data-id", {
      companyId: companyId,
      reportingPeriod: preparedFixtureForTest.reportingPeriod,
      data: preparedFixtureForTest.t,
    } as CompanyAssociatedDataEsgquestionnaireData);
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
    cy.get("body").type("{esc}");
    getSectionHead("Energieverbrauch").click();
    getCellValueContainer("Berichterstattung Energieverbrauch").children().should("have.length", 0);
  });

  it("Check that on the GDV-VÖB view Page the string for datatable component works properly", () => {
    cy.intercept("/api/data/gdv/mock-data-id", {
      companyId: companyId,
      reportingPeriod: preparedFixtureForTest.reportingPeriod,
      data: preparedFixtureForTest.t,
    } as CompanyAssociatedDataEsgquestionnaireData);
    mountGDVFrameworkFromFakeFixture([preparedFixtureForTest]);
    getSectionHead("Unternehmensführung/ Governance").click();
    getSectionHead("Sonstige").eq(1).click();
    getCellValueContainer("Wirtschaftsprüfer").contains(
      assertDefined(preparedFixtureForTest.t.unternehmensfuehrungGovernance?.sonstige?.wirtschaftspruefer),
    );
  });

  it("Check that on the GDV-VÖB view Page the list base data point component works properly", () => {
    cy.intercept("/api/data/gdv/mock-data-id", {
      companyId: companyId,
      reportingPeriod: preparedFixtureForTest.reportingPeriod,
      data: preparedFixtureForTest.t,
    } as CompanyAssociatedDataEsgquestionnaireData);
    mountGDVFrameworkFromFakeFixture([preparedFixtureForTest]);
    getSectionHead("ESG Berichte").click();
    getCellValueContainer("Aktuelle Berichte").click();
    cy.get("span").contains("Beschreibung des Berichts");
    const aktuelleBerichte = assertDefined(preparedFixtureForTest.t.allgemein?.esgBerichte?.aktuelleBerichte);

    for (const singleEsgBericht of aktuelleBerichte) {
      cy.get("div").contains(assertDefined(singleEsgBericht.value));
      if (singleEsgBericht.dataSource) {
        cy.get("div").contains(assertDefined(singleEsgBericht.dataSource.fileName));
      }
    }
    cy.get('div.p-dialog-content i[data-test="download-icon"]').should("have.length", aktuelleBerichte.length - 1);
  });
});

/**
 *
 * Mounts the MultiLayerDataTableFrameworkPanel with the given dataset for the GDV framework
 * @param fixtureDatasetsForDisplay the datasets from the fixtures to mount
 * @returns the component mounting chainable
 */
function mountGDVFrameworkFromFakeFixture(
  fixtureDatasetsForDisplay: Array<FixtureData<EsgquestionnaireData>>,
): Cypress.Chainable {
  const dummyCompanyId = "mock-company-id";
  const convertedDataAndMetaInformation: Array<DataAndMetaInformation<EsgquestionnaireData>> =
    fixtureDatasetsForDisplay.map((it, idx) => {
      const metaInformation: DataMetaInformation = {
        dataId: `data-id-${idx}`,
        companyId: dummyCompanyId,
        dataType: DataTypeEnum.Esgquestionnaire,
        uploadTime: 0,
        reportingPeriod: it.reportingPeriod,
        qaStatus: "Accepted",
        currentlyActive: true,
      };
      return {
        data: it.t,
        metaInfo: metaInformation,
      };
    });

  return mountMLDTForGdvPanel(convertedDataAndMetaInformation, dummyCompanyId, false);
}

/**
 * Mounts the MultiLayerDataTableFrameworkPanel with the given dataset
 * @param datasetsToDisplay datasets to mount
 * @param companyId company ID of the mocked requests
 * @param reviewMode toggles the reviewer mode
 * @returns the component mounting chainable
 */
export function mountMLDTForGdvPanel(
  datasetsToDisplay: Array<DataAndMetaInformation<EsgquestionnaireData>>,
  companyId: string,
  reviewMode: boolean,
): Cypress.Chainable {
  cy.intercept(`/api/data/${DataTypeEnum.Esgquestionnaire}/companies/${companyId}`, datasetsToDisplay);
  return cy.mountWithDialog(
    MultiLayerDataTableFrameworkPanel,
    {
      keycloak: minimalKeycloakMock({}),
    },
    {
      companyId: companyId,
      frameworkIdentifier: DataTypeEnum.Esgquestionnaire,
      displayConfiguration: EsgquestionnaireViewConfiguration,
      inReviewMode: reviewMode,
    },
  );
}
