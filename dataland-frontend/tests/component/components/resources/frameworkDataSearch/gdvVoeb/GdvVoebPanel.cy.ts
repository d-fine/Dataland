import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { type CompanyAssociatedDataGdvData, DataTypeEnum, type GdvData } from "@clients/backend";

import { mountMLDTFrameworkPanelFromFakeFixture } from "@ct/testUtils/MultiLayerDataTableComponentTestUtils";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";

import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { formatGdvYearlyDecimalTimeseriesDataForTable } from "@/components/resources/dataTable/conversion/gdv/GdvYearlyDecimalTimeseriesDataGetterFactory";
import {
  formatStringForDatatable
} from "../../../../../../src/components/resources/dataTable/conversion/PlainStringValueGetterFactory";
import {
  formatListOfBaseDataPoint
} from "../../../../../../src/components/resources/dataTable/conversion/gdv/GdvListOfBaseDataPointGetterFactory";

const configForGdvVoebPanelWithOneRollingWindow: MLDTConfig<GdvData> = [
  {
    type: "cell",
    label: "Umsatz/Investitionsaufwand für nachhaltige Aktivitäten",
    explanation:
        "Wie hoch ist der Umsatz/Investitionsaufwand des Unternehmens aus nachhaltigen Aktivitäten (Mio. €) gemäß einer Definition der EU-Taxonomie? Bitte machen Sie Angaben zu den betrachteten Sektoren und gegebenenfalls zu den Annahmen bzgl. Taxonomie-konformen (aligned) Aktivitäten für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an.",
    shouldDisplay: (): boolean => true,
    valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
        formatGdvYearlyDecimalTimeseriesDataForTable(
            dataset.umwelt?.taxonomie?.umsatzInvestitionsaufwandFuerNachhaltigeAktivitaeten,
            {
              umsatzInvestitionsaufwandAusNachhaltigenAktivitaeten: {
                label: "Umsatz/Investitionsaufwand für nachhaltige Aktivitäten",
                unitSuffix: "Mio. €",
              },
            },
            "Umsatz/Investitionsaufwand f\u00FCr nachhaltige Aktivit\u00E4ten",
        ),
  },
];

const configForGdvVoebPanelWithOneStringComponent: MLDTConfig<GdvData> = [
  {
    type: "cell",
    label: "Sicherheitsmaßnahmen für Mitarbeiter",
    explanation:
        "Welche Maßnahmen werden ergriffen, um die Gesundheit und Sicherheit der Mitarbeiter des Unternehmens zu verbessern?",
    shouldDisplay: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
    valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
        formatStringForDatatable(dataset.soziales?.einkommensgleichheit?.sicherheitsmassnahmenFuerMitarbeiter),
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
    const preparedFixture = getPreparedFixture("Gdv-dataset-with-no-null-fields",
        preparedFixtures);
    const gdvData = preparedFixture.t;

    cy.intercept("/api/data/gdv/mock-data-id", {
      companyId: companyId,
      reportingPeriod: preparedFixture.reportingPeriod,
      data: gdvData,
    } as CompanyAssociatedDataGdvData);
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.Gdv, configForGdvVoebPanelWithOneRollingWindow, [
      preparedFixture,
    ]);
    cy.get("span").contains("Umsatz/Investitionsaufwand für nachhaltige Aktivitäten");
    cy.get("a").should("have.class", "link").click();
    cy.get("div").contains("Historical Data");
    cy.get("div").contains("Reporting");
    cy.get("div").contains("Prognosis Data");

    cy.get("td").contains("57,945.66 Mio. €");
    cy.get("td").contains("79,393.32 Mio. €");
    cy.get("td").contains("55,042.92 Mio. €");
    for (let i = 2023; i < 2028; i++) {
      cy.get("span").contains(i);
    }
  });

  it("Check that on the GDV-VÖB view Page the string for datatable component works properly", () => {
    const preparedFixture = getPreparedFixture("Gdv-dataset-with-no-null-fields",
        preparedFixtures);
    const gdvData = preparedFixture.t;

    cy.intercept("/api/data/gdv/mock-data-id", {
      companyId: companyId,
      reportingPeriod: preparedFixture.reportingPeriod,
      data: gdvData,
    } as CompanyAssociatedDataGdvData);
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.Gdv, configForGdvVoebPanelWithOneStringComponent, [
      preparedFixture,
    ]);
    cy.get("span").contains("Sicherheitsmaßnahmen für Mitarbeiter");
  });

  it("Check that on the GDV-VÖB view Page the list base data point component works properly", () => {
    const preparedFixture = getPreparedFixture("Gdv-dataset-with-no-null-fields",
        preparedFixtures);
    const gdvData = preparedFixture.t;

    cy.intercept("/api/data/gdv/mock-data-id", {
      companyId: companyId,
      reportingPeriod: preparedFixture.reportingPeriod,
      data: gdvData,
    } as CompanyAssociatedDataGdvData);
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.Gdv, configForGdvVoebPanelWithOneListForBaseDataPointComponent,
        [
            preparedFixture,
    ]);

    cy.get("span").contains("Aktuelle Berichte");
    cy.get("a").should("have.class","link").click();

    cy.get("span").contains("Beschreibung des Berichts");
    cy.get("div").contains("blockchains");
    cy.intercept('GET', '/path-to-your-resource').as('getCallAlias')
    cy.get('span[data-test="Report-Download-Policy"]').should("exist")
  });
});
