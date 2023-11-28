import { type HeimathafenData } from "@clients/backend";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
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
    ],
  },
];
