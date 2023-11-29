import { type GdvData } from "@clients/backend";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { formatYesNoValueForDatatable } from "@/components/resources/dataTable/conversion/YesNoValueGetterFactory";
import { formatStringForDatatable } from "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory";
export const GdvViewConfiguration: MLDTConfig<GdvData> = [    {
      type: "section",
      label: "General",
      expandOnPageLoad: false,
      shouldDisplay: (): boolean => true
    ,
      children: [    {
          type: "section",
          label: "Master Data",
          expandOnPageLoad: false,
          shouldDisplay: (): boolean => true
        ,
          children: [    {
              type: "cell",
              label: "Berichts-Pflicht",
              explanation: "Ist das Unternehmen berichtspflichtig?",
              shouldDisplay: (): boolean => true
            ,
              valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes => formatYesNoValueForDatatable(dataset.general?.masterData?.berichtsPflicht)
            ,
            },
            ],
    
        },
        ],
    
    },
    {
      type: "section",
      label: "Allgemein",
      expandOnPageLoad: true,
      shouldDisplay: (): boolean => true
    ,
      children: [    {
          type: "cell",
          label: "ESG-Ziele",
          explanation: "Hat das Unternehmen spezifische ESG-Ziele/Engagements? Werden bspw. spezifische Ziele / Maßnahmen ergriffen, um das 1,5 Grad Ziel zu erreichen?",
          shouldDisplay: (): boolean => true
        ,
          valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes => formatYesNoValueForDatatable(dataset.allgemein?.esgZiele)
        ,
        },
        {
          type: "cell",
          label: "Ziele",
          explanation: "Bitte geben Sie eine genaue Beschreibung der ESG-Ziele.",
          shouldDisplay: (): boolean => true
        ,
          valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes => formatStringForDatatable(dataset.allgemein?.ziele)
        ,
        },
        {
          type: "cell",
          label: "Investitionen",
          explanation: "Bitte geben Sie an wieviele Budgets/Vollzeitäquivalente für das Erreichen der ESG-Ziele zugewiesen wurden.",
          shouldDisplay: (): boolean => true
        ,
          valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes => formatStringForDatatable(dataset.allgemein?.investitionen)
        ,
        },
        {
          type: "cell",
          label: "Sektor mit hohen Klimaauswirkungen",
          explanation: "Kann das Unternehmen einem oder mehreren Sektoren mit hohen Klimaauswirkungen zugeordnet werden?",
          shouldDisplay: (): boolean => true
        ,
          valueGetter: (dataset: GdvData): AvailableMLDTDisplayObjectTypes => formatYesNoValueForDatatable(dataset.allgemein?.sektorMitHohenKlimaauswirkungen)
        ,
        },
        ],
    
    },
    ];