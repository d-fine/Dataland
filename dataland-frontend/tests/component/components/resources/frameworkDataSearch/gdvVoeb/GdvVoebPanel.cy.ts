import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import {
  type BaseDataPointString,
  type CompanyAssociatedDataGdvData,
  type DataMetaInformation,
  DataTypeEnum,
  type GdvData,
} from "@clients/backend";

import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";

import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { formatGdvYearlyDecimalTimeseriesDataForTable } from "@/components/resources/dataTable/conversion/gdv/GdvYearlyDecimalTimeseriesDataGetterFactory";
import { formatStringForDatatable } from "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory";
import { formatListOfBaseDataPoint } from "@/components/resources/dataTable/conversion/gdv/GdvListOfBaseDataPointGetterFactory";
import { formatNumberToReadableFormat } from "@/utils/Formatter";
import { type DataAndMetaInformation } from "@/api-models/DataAndMetaInformation";
import MultiLayerDataTableFrameworkPanel from "@/components/resources/frameworkDataSearch/frameworkPanel/MultiLayerDataTableFrameworkPanel.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";

const configForGdvVoebPanelWithOneRollingWindow: MLDTConfig<GdvData> = [
  {
    type: "cell",
    label: "Treibhausgas-Berichterstattung und Prognosen",
    explanation:
        "Welche Treibhausgasinformationen werden derzeit auf Unternehmens-/Konzernebene berichtet und prognostiziert?" +
        " Bitte geben Sie die Scope1, Scope 2 und Scope 3 Emissionen# für das aktuelle Kalenderjahr," +
        " die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an (in tCO2-Äquiv.).",
    shouldDisplay: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
    valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
        formatGdvYearlyDecimalTimeseriesDataForTable(
            dataset.umwelt?.treibhausgasemissionen?.treibhausgasBerichterstattungUndPrognosen,
            {
              scope1: { label: "Scope 1", unitSuffix: "tCO2-Äquiv." },
              scope2: { label: "Scope 2", unitSuffix: "tCO2-Äquiv." },
              scope3: { label: "Scope 3", unitSuffix: "tCO2-Äquiv." },
            },
            "Treibhausgas-Berichterstattung und Prognosen",
        ),
  },
];

const configForGdvVoebPanelWithOneStringComponent: MLDTConfig<GdvData> = [
  {
    type: "cell",
    label: "Wirtschaftsprüfer",

    shouldDisplay: (): boolean => true,
    valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
      formatStringForDatatable(dataset.unternehmensfuehrungGovernance?.sonstige?.wirtschaftspruefer),
  },
];

const configForGdvVoebPanelWithOneListForBaseDataPointComponent: MLDTConfig<GdvData> = [
  {
    type: "cell",
    label: "Aktuelle Berichte",
    explanation: "Aktuelle Nachhaltigkeits- oder ESG-Berichte",
    shouldDisplay: (): boolean => true,
    valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes => {
      return formatListOfBaseDataPoint(
        "Aktuelle Berichte",
        dataset.allgemein?.esgBerichte?.aktuelleBerichte,
        "Beschreibung des Berichts",
        "Bericht",
      );
    },
  },
];

describe("Component Test for the GDV-VÖB view Page with its componenets", () => {
  let preparedFixtures: Array<FixtureData<GdvData>>;
  const companyId = "mock-company-id";
  before(function () {
    cy.fixture("CompanyInformationWithGdvPreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<GdvData>>;
    });
  });
  it("Check that on the GDV-VÖB view Page the rolling window component works properly", () => {
    const preparedFixture = getPreparedFixture("Gdv-dataset-with-no-null-fields", preparedFixtures);
    const gdvData = preparedFixture.t;

    cy.intercept("/api/data/gdv/mock-data-id", {
      companyId: companyId,
      reportingPeriod: preparedFixture.reportingPeriod,
      data: gdvData,
    } as CompanyAssociatedDataGdvData);
    mountGDVFrameworkFromFakeFixture(DataTypeEnum.Gdv, configForGdvVoebPanelWithOneRollingWindow, [preparedFixture]);
    cy.get("span").contains("Treibhausgas-Berichterstattung und Prognosen");
    cy.get("a").should("have.class", "link").click();
    cy.get("div").contains("Historische Daten");
    cy.get("div").contains("Aktuelles Jahr");
    cy.get("div").contains("Prognosen");

    const modalDatasets =
      preparedFixture.t.umwelt?.treibhausgasemissionen?.treibhausgasBerichterstattungUndPrognosen?.yearlyData;
    for (const dataSetOfOneYear in modalDatasets) {
      cy.get("div").contains(
        formatNumberToReadableFormat(modalDatasets[dataSetOfOneYear].scope1),
      );
      cy.get("div").contains(
        formatNumberToReadableFormat(modalDatasets[dataSetOfOneYear].scope2),
      );
      cy.get("div").contains(
        formatNumberToReadableFormat(modalDatasets[dataSetOfOneYear].scope3),
      );
    }
  });

  it("Check that on the GDV-VÖB view Page the string for datatable component works properly", () => {
    const preparedFixture = getPreparedFixture("Gdv-dataset-with-no-null-fields", preparedFixtures);
    const gdvData = preparedFixture.t;

    cy.intercept("/api/data/gdv/mock-data-id", {
      companyId: companyId,
      reportingPeriod: preparedFixture.reportingPeriod,
      data: gdvData,
    } as CompanyAssociatedDataGdvData);
    mountGDVFrameworkFromFakeFixture(DataTypeEnum.Gdv, configForGdvVoebPanelWithOneStringComponent, [preparedFixture]);
    cy.get("span").contains("Wirtschaftsprüfer");
    cy.get("span").contains(preparedFixture.t.unternehmensfuehrungGovernance?.sonstige?.wirtschaftspruefer as string);
  });

  it("Check that on the GDV-VÖB view Page the list base data point component works properly", () => {
    const preparedFixture = getPreparedFixture("Gdv-dataset-with-no-null-fields", preparedFixtures);
    const gdvData = preparedFixture.t;

    cy.intercept("/api/data/gdv/mock-data-id", {
      companyId: companyId,
      reportingPeriod: preparedFixture.reportingPeriod,
      data: gdvData,
    } as CompanyAssociatedDataGdvData);
    mountGDVFrameworkFromFakeFixture(DataTypeEnum.Gdv, configForGdvVoebPanelWithOneListForBaseDataPointComponent, [
      preparedFixture,
    ]);

    const listData: Array<BaseDataPointString> = preparedFixture.t.allgemein?.esgBerichte
      ?.aktuelleBerichte as Array<BaseDataPointString>;
    cy.get("span").contains("Aktuelle Berichte");
    cy.get("a").should("have.class", "link").click();

    cy.get("span").contains("Beschreibung des Berichts");
    for (const oneListElement of listData) {
      cy.get("div").contains(oneListElement.value as string);
      cy.get("div").contains(oneListElement.dataSource?.fileName as string);
    }
    cy.get('span[data-test="Report-Download-Policy"]').should("exist");
  });
});

/**
 *
 * Mounts the MultiLayerDataTableFrameworkPanel with the given dataset for the GDV framework
 * @param frameworkIdentifier the identifier of the framework
 * @param displayConfiguration the MLDT display configuration for the current framework
 * @param fixtureDatasetsForDisplay the datasets from the fixtures to mount
 * @param companyId company ID of the mocked requests
 * @param reviewMode toggles the reviewer mode
 * @returns the component mounting chainable
 */
function mountGDVFrameworkFromFakeFixture(
  frameworkIdentifier: DataTypeEnum,
  displayConfiguration: MLDTConfig<GdvData>,
  fixtureDatasetsForDisplay: Array<FixtureData<GdvData>>,
  companyId = "mock-company-id",
  reviewMode = false,
): Cypress.Chainable {
  const convertedDataAndMetaInformation: Array<DataAndMetaInformation<GdvData>> = fixtureDatasetsForDisplay.map(
    (it, idx) => {
      const metaInformation: DataMetaInformation = {
        dataId: `data-id-${idx}`,
        companyId: companyId,
        dataType: frameworkIdentifier,
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

  return mountMLDTForGdvPanel(
    frameworkIdentifier,
    displayConfiguration,
    convertedDataAndMetaInformation,
    companyId,
    reviewMode,
  );
}

/**
 * Mounts the MultiLayerDataTableFrameworkPanel with the given dataset
 * @param frameworkIdentifier the identifier of the framework whose datasets should be displayed
 * @param displayConfiguration the MLDT display configuration
 * @param datasetsToDisplay datasets to mount
 * @param companyId company ID of the mocked requests
 * @param reviewMode toggles the reviewer mode
 * @returns the component mounting chainable
 */
export function mountMLDTForGdvPanel(
  frameworkIdentifier: DataTypeEnum,
  displayConfiguration: MLDTConfig<GdvData>,
  datasetsToDisplay: Array<DataAndMetaInformation<GdvData>>,
  companyId = "mock-company-id",
  reviewMode = false,
): Cypress.Chainable {
  cy.intercept(`/api/data/${frameworkIdentifier}/companies/${companyId}`, datasetsToDisplay);
  return cy.mountWithDialog(
    MultiLayerDataTableFrameworkPanel,
    {
      keycloak: minimalKeycloakMock({}),
    },
    {
      companyId: companyId,
      frameworkIdentifier: frameworkIdentifier,
      displayConfiguration: displayConfiguration,
      inReviewMode: reviewMode,
    },
  );
}
