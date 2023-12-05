import { type GdvData } from "@clients/backend";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { formatStringForDatatable } from "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory";
import { formatYesNoValueForDatatable } from "@/components/resources/dataTable/conversion/YesNoValueGetterFactory";
import { formatListOfStringsForDatatable } from "@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory";
export const GdvViewConfiguration: MLDTConfig<GdvData> = [
  {
    type: "section",
    label: "General",
    expandOnPageLoad: false,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "section",
        label: "Master Data",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Berichts-Pflicht",
            explanation: "Ist das Unternehmen berichtspflichtig?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.masterData?.berichtsPflicht),
          },
          {
            type: "cell",
            label: "(Gültigkeits) Datum",
            explanation: "Datum bis wann die Information gültig ist",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.masterData?.gueltigkeitsDatum),
          },
        ],
      },
    ],
  },
  {
    type: "section",
    label: "Allgemein",
    expandOnPageLoad: true,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "cell",
        label: "ESG-Ziele",
        explanation:
          "Hat das Unternehmen spezifische ESG-Ziele/Engagements? Werden bspw. spezifische Ziele / Maßnahmen ergriffen, um das 1,5 Grad Ziel zu erreichen?",
        shouldDisplay: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
        valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
          formatYesNoValueForDatatable(dataset.allgemein?.esgZiele),
      },
      {
        type: "cell",
        label: "Ziele",
        explanation: "Bitte geben Sie eine genaue Beschreibung der ESG-Ziele.",
        shouldDisplay: (dataset: GdvData): boolean => dataset.allgemein?.esgZiele == "Yes",
        valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
          formatStringForDatatable(dataset.allgemein?.ziele),
      },
      {
        type: "cell",
        label: "Investitionen",
        explanation:
          "Bitte geben Sie an wieviele Budgets/Vollzeitäquivalente für das Erreichen der ESG-Ziele zugewiesen wurden.",
        shouldDisplay: (dataset: GdvData): boolean => dataset.allgemein?.esgZiele == "Yes",
        valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
          formatStringForDatatable(dataset.allgemein?.investitionen),
      },
      {
        type: "cell",
        label: "Sektor mit hohen Klimaauswirkungen",
        explanation: "Kann das Unternehmen einem oder mehreren Sektoren mit hohen Klimaauswirkungen zugeordnet werden?",
        shouldDisplay: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
        valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
          formatYesNoValueForDatatable(dataset.allgemein?.sektorMitHohenKlimaauswirkungen),
      },
      {
        type: "cell",
        label: "Sektor",
        explanation:
          "Bitte geben Sie an, zu welchen Sektoren (mit hohen Klimaauswirkungen) das Unternehmen zugeordnet werden kann.",
        shouldDisplay: (dataset: GdvData): boolean => dataset.allgemein?.sektorMitHohenKlimaauswirkungen == "Yes",
        valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
          formatListOfStringsForDatatable(dataset.allgemein?.sektor, "Sektor"),
      },
      {
        type: "cell",
        label: "Nachhaltigkeitsbericht",
        explanation: "Erstellt das Unternehmen Nachhaltigkeits- oder ESG-Berichte?",
        shouldDisplay: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
        valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
          formatYesNoValueForDatatable(dataset.allgemein?.nachhaltigkeitsbericht),
      },
      {
        type: "cell",
        label: "Frequenz der Berichterstattung",

        shouldDisplay: (dataset: GdvData): boolean => dataset.allgemein?.nachhaltigkeitsbericht == "Yes",
        valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes =>
          formatStringForDatatable(dataset.allgemein?.frequenzDerBerichterstattung),
      },
    ],
  },
];
