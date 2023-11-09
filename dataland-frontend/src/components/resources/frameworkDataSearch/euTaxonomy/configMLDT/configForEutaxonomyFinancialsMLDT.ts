import {
  AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import MultiSelectModal from "@/components/resources/dataTable/modals/MultiSelectModal.vue";
import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
import {formatNumberToReadableFormat} from "@/utils/Formatter";

export const configForEutaxonomyFinancialsMLDT = [
  {
    type: "cell",
    label: euTaxonomyKpiNameMappings.financialServicesTypes,
    explanation: euTaxonomyKpiInfoMappings.financialServicesTypes,
    shouldDisplay: (): boolean => true,
    valueGetter: (dataset) => ({
      displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
      displayValue: {
        label: `Show ${dataset.financialServicesTypes.length} value${dataset.financialServicesTypes.length > 1 ? "s" : ""}`,
        modalComponent: MultiSelectModal,
        modalOptions: {
          props: {
            header: euTaxonomyKpiNameMappings.financialServicesTypes,
            modal: true,
            dismissableMask: true,
          },
          data: {
            label: euTaxonomyKpiNameMappings.financialServicesTypes,
            values: dataset.financialServicesTypes,
          },
        },
  },
    }),
  },
  {
    type: "section",
    label: "Eligibility Kpis",
    labelBadgeColor: "orange",
    expandOnPageLoad: true,
    shouldDisplay: () => true,
    children: [
      {
        type: "cell",
        label: "Credit Institution",
        shouldDisplay: (dataset) => (dataset.financialServicesTypes.includes("CreditInstitution")),
        valueGetter: (dataset) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.eligibilityKpis.CreditInstitution.taxonomyEligibleActivityInPercent,
        }),
      },
      {
        type: "cell",
        label: "Investment Firm",
        shouldDisplay: (dataset) => (dataset.financialServicesTypes.includes("InvestmentFirm")),
        valueGetter: (dataset) => {

          if (dataset.eligibilityKpis.InvestmentFirm.taxonomyEligibleActivityInPercent?.quality ||
              dataset.eligibilityKpis.InvestmentFirm.taxonomyEligibleActivityInPercent?.comment?.length ||
              dataset.eligibilityKpis.InvestmentFirm.taxonomyEligibleActivityInPercent?.dataSource?.page != null
          ) {
            return {
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
                value: dataset.eligibilityKpis.InvestmentFirm.taxonomyEligibleActivityInPercent.value,
                dataSource: dataset.eligibilityKpis.InvestmentFirm.taxonomyEligibleActivityInPercent.dataSource,
                quality: dataset.eligibilityKpis.InvestmentFirm.taxonomyEligibleActivityInPercent.quality,
                comment: dataset.eligibilityKpis.InvestmentFirm.taxonomyEligibleActivityInPercent.comment,
              },
            }
          } else if (!!dataset.eligibilityKpis.InvestmentFirm.taxonomyEligibleActivityInPercent?.dataSource?.fileReference?.trim().length) {
            return {
              displayComponentName: MLDTDisplayComponentName.DocumentLinkDisplayComponent,
              displayValue: {
                label: dataset.eligibilityKpis.InvestmentFirm.taxonomyEligibleActivityInPercent.value,
                dataSource: dataset.eligibilityKpis.InvestmentFirm.taxonomyEligibleActivityInPercent.dataSource,
              },
            }
          } else {
            return {
              displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
              displayValue: dataset.eligibilityKpis.InvestmentFirm.taxonomyEligibleActivityInPercent?.value,
            }
          }
        }},


      {
        type: "cell",
        label: "Investment Firm",
        shouldDisplay: (dataset) => (dataset.financialServicesTypes.includes("InvestmentFirm")),
        valueGetter: (dataset) => {

          if (dataset.eligibilityKpis.InvestmentFirm.taxonomyNonEligibleActivityInPercent?.quality ||
              dataset.eligibilityKpis.InvestmentFirm.taxonomyNonEligibleActivityInPercent?.comment?.length ||
              dataset.eligibilityKpis.InvestmentFirm.taxonomyNonEligibleActivityInPercent?.dataSource?.page != null
          ) {
            return {
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
                value: dataset.eligibilityKpis.InvestmentFirm.taxonomyNonEligibleActivityInPercent.value,
                dataSource: dataset.eligibilityKpis.InvestmentFirm.taxonomyNonEligibleActivityInPercent.dataSource,
                quality: dataset.eligibilityKpis.InvestmentFirm.taxonomyNonEligibleActivityInPercent.quality,
                comment: dataset.eligibilityKpis.InvestmentFirm.taxonomyNonEligibleActivityInPercent.comment,
              },
            }
          } else if (!!dataset.eligibilityKpis.InvestmentFirm.taxonomyNonEligibleActivityInPercent?.dataSource?.fileReference?.trim().length) {
            return {
              displayComponentName: MLDTDisplayComponentName.DocumentLinkDisplayComponent,
              displayValue: {
                label: dataset.eligibilityKpis.InvestmentFirm.taxonomyNonEligibleActivityInPercent.value,
                dataSource: dataset.eligibilityKpis.InvestmentFirm.taxonomyNonEligibleActivityInPercent.dataSource,
              },
            }
          } else {
            return {
              displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
              displayValue: dataset.eligibilityKpis.InvestmentFirm.taxonomyNonEligibleActivityInPercent?.value,
            }
          }
        }},


      {
        type: "cell",
        label: "Investment Firm",
        shouldDisplay: (dataset) => (dataset.financialServicesTypes.includes("InvestmentFirm")),
        valueGetter: (dataset) => {

          if (dataset.eligibilityKpis.InvestmentFirm.banksAndIssuersInPercent?.quality ||
              dataset.eligibilityKpis.InvestmentFirm.banksAndIssuersInPercent?.comment?.length ||
              dataset.eligibilityKpis.InvestmentFirm.banksAndIssuersInPercent?.dataSource?.page != null
          ) {
            return {
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
                value: dataset.eligibilityKpis.InvestmentFirm.banksAndIssuersInPercent.value,
                dataSource: dataset.eligibilityKpis.InvestmentFirm.banksAndIssuersInPercent.dataSource,
                quality: dataset.eligibilityKpis.InvestmentFirm.banksAndIssuersInPercent.quality,
                comment: dataset.eligibilityKpis.InvestmentFirm.banksAndIssuersInPercent.comment,
              },
            }
          } else if (!!dataset.eligibilityKpis.InvestmentFirm.banksAndIssuersInPercent?.dataSource?.fileReference?.trim().length) {
            return {
              displayComponentName: MLDTDisplayComponentName.DocumentLinkDisplayComponent,
              displayValue: {
                label: dataset.eligibilityKpis.InvestmentFirm.banksAndIssuersInPercent.value,
                dataSource: dataset.eligibilityKpis.InvestmentFirm.banksAndIssuersInPercent.dataSource,
              },
            }
          } else {
            return {
              displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
              displayValue: dataset.eligibilityKpis.InvestmentFirm.banksAndIssuersInPercent?.value,
            }
          }
        }},



      {
        type: "section",
        label: "Subsection 1",
        expandOnPageLoad: false,
        shouldDisplay: () => true,
        children: [
          {
            type: "cell",
            label: "Level 3 - String",
            shouldDisplay: () => true,
            valueGetter: (dataset) => ({
              displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
              displayValue: dataset.financialServicesTypes,
            }),
          },
        ],
      },
    ],
  },
];
