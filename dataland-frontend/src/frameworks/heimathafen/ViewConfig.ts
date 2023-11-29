import { type HeimathafenData } from "@clients/backend";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { formatYesNoValueForDatatable } from "@/components/resources/dataTable/conversion/YesNoValueGetterFactory";
import { formatStringForDatatable } from "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory";
export const HeimathafenViewConfiguration: MLDTConfig<HeimathafenData> = [
  {
    type: "section",
    label: "General",
    expandOnPageLoad: true,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "section",
        label: "Datenanbieter",
        expandOnPageLoad: true,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Unternehmenseigentum und Eigentümerstruktur",
            explanation:
              "Bitte geben Sie eine kurze Auskunft über die Besitzverhältnisse und Eigentümerstruktur des Unternehmens.",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.datenanbieter?.unternehmenseigentumUndEigentuemerstruktur),
          },
        ],
      },
      {
        type: "section",
        label: "Methodik",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Verständnis von Nachhaltigkeit als Teil der Bewertung",
            explanation:
              "Bitte führen Sie Ihr Verständnis von Nachhaltigkeit im Rahmen der Bewertung aus.\nBitte machen Sie Angaben zu den Komponenten, die Sie bei der Bewertung des Grades der Nachhaltigkeit von Unternehmen berücksichtigen.",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.verstaendnisVonNachhaltigkeitAlsTeilDerBewertung),
          },
          {
            type: "cell",
            label: "Qualitätssicherungsprozess",
            explanation: "Gibt es einen Qualitätssicherungsprozess?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.methodik?.qualitaetssicherungsprozess),
          },
        ],
      },
    ],
  },
];
