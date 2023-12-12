import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { type CompanyAssociatedDataGdvData, DataTypeEnum, type GdvData } from "@clients/backend";

import { mountMLDTFrameworkPanelFromFakeFixture } from "@ct/testUtils/MultiLayerDataTableComponentTestUtils";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";

import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { formatGdvYearlyDecimalTimeseriesDataForTable } from "@/components/resources/dataTable/conversion/gdv/GdvYearlyDecimalTimeseriesDataGetterFactory";
import {
  formatStringForDatatable
} from "../../../../../../src/components/resources/dataTable/conversion/PlainStringValueGetterFactory";

const configForGdvVoebPanelWithOneRollingWindow: MLDTConfig<GdvData> = [
  {
    type: "cell",
    label: "Auswirkungen auf Anteil befrister Verträge und Fluktuation",
    explanation:
      "Bitte geben Sie die Anzahl der befristeten Verträge sowie die Fluktuation (%) für die letzten drei Jahre an.",
    shouldDisplay: (): boolean => true,
    valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
      formatGdvYearlyDecimalTimeseriesDataForTable(
        dataset.soziales?.auswirkungenAufAnteilBefristerVertraegeUndFluktuation,
        {
          anzahlbefristeteVertraege: { label: "Anzahl der befristeten Verträge", unitSuffix: "" },
          fluktuation: { label: "Fluktuation", unitSuffix: "%" },
        },
        "Auswirkungen auf Anteil befrister Vertr\u00E4ge und Fluktuation",
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
    cy.get("span").contains("Auswirkungen auf Anteil befrister Verträge und Fluktuation");
    cy.get('em[data-test="mldt-tooltip"').click();
    cy.get("a").should("have.class", "link").click();
    cy.get("div").contains("Historical Data");
    cy.get("div").contains("Reporting");
    cy.get("div").contains("Prognosis Data");

    cy.get("td").contains("Anzahl der befristeten Verträge");
    cy.get("td").contains("Fluktuation");
    cy.get("td").contains("77,212.02 %");
    cy.get("td").contains("20,588.11 %");
    cy.get("td").contains("41,615.79 %");
    for (let i = 2026; i < 2031; i++) {
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
});
