import {
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { euTaxonomyKpiNameMappings } from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
import { type EuTaxonomyDataForFinancials } from "@clients/backend";
import { formatNumberToReadableFormat } from "@/utils/Formatter";
import { yesNoValueGetterFactory } from "@/components/resources/dataTable/conversion/YesNoValueGetterFactory";

/**
export const configForEutaxonomyFinancialsMLDT= [
  {
    type:"cell",
    label:"GENERAL",
    labelBadgeColor: "yellow",
    expandOnPageLoad: true,
    shouldDisplay: (): boolean=> true,
    valueGetter: () : MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent> => ({
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: "5"
    })
  }
];
 */

export const configForEutaxonomyFinancialsMLDT = [
  {
    type: "section",
    label: "General",
    labelBadgeColor: "orange",
    expandOnPageLoad: true,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.fiscalYearDeviation,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
          displayValue: dataset.fiscalYearDeviation,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.fiscalYearEnd,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
          displayValue: dataset.fiscalYearEnd,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.scopeOfEntities,
        shouldDisplay: (): boolean => true,
        valueGetter: yesNoValueGetterFactory(euTaxonomyKpiNameMappings.scopeOfEntities),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.nfrdMandatory,
        shouldDisplay: (): boolean => true,
        valueGetter: yesNoValueGetterFactory(euTaxonomyKpiNameMappings.nfrdMandatory),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.euTaxonomyActivityLevelReporting,
        shouldDisplay: (): boolean => true,
        valueGetter: yesNoValueGetterFactory(euTaxonomyKpiNameMappings.euTaxonomyActivityLevelReporting),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.assurance,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.assurance,
            value: dataset.assurance?.value ?? "",
            dataSource: dataset.assurance?.dataSource ?? undefined,
          },
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.numberOfEmployees,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent, //TODO check if coorect display component
          displayValue: dataset.numberOfEmployees?.toString(),
        }),
      },
    ],
  },
  {
    type: "section",
    label: "Eligibility KPIs",
    labelBadgeColor: "green",
    expandOnPageLoad: false,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent, //TODO
            value:
              formatNumberToReadableFormat(
                dataset.eligibilityKpis?.["creditInstitution"]?.taxonomyEligibleActivityInPercent?.value,
              ) ?? "",
            dataSource:
              dataset.eligibilityKpis?.["creditInstitution"]?.taxonomyEligibleActivityInPercent?.dataSource ??
              undefined,
          },
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent, //TODO
            value:
              formatNumberToReadableFormat(
                dataset.eligibilityKpis?.["creditInstitution"]?.taxonomyNonEligibleActivityInPercent?.value,
              ) ?? "",
            dataSource:
              dataset.eligibilityKpis?.["creditInstitution"]?.taxonomyNonEligibleActivityInPercent?.dataSource ??
              undefined,
          },
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.derivativesInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.derivativesInPercent, //TODO
            value:
              formatNumberToReadableFormat(
                dataset.eligibilityKpis?.["creditInstitution"]?.derivativesInPercent?.value,
              ) ?? "",
            dataSource: dataset.eligibilityKpis?.["creditInstitution"]?.derivativesInPercent?.dataSource ?? undefined,
          },
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.banksAndIssuersInPercent, //TODO
            value:
              formatNumberToReadableFormat(
                dataset.eligibilityKpis?.["creditInstitution"]?.banksAndIssuersInPercent?.value,
              ) ?? "",
            dataSource:
              dataset.eligibilityKpis?.["creditInstitution"]?.banksAndIssuersInPercent?.dataSource ?? undefined,
          },
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.banksAndIssuersInPercent, //TODO
            value:
              formatNumberToReadableFormat(
                dataset.eligibilityKpis?.["creditInstitution"]?.banksAndIssuersInPercent?.value,
              ) ?? "",
            dataSource:
              dataset.eligibilityKpis?.["creditInstitution"]?.banksAndIssuersInPercent?.dataSource ?? undefined,
          },
        }),
      },
    ],
  },
  {
    type: "section",
    label: "Credit Institution KPIs",
    labelBadgeColor: "yellow",
    expandOnPageLoad: false,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.tradingPortfolioInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.tradingPortfolioInPercent,
            value: formatNumberToReadableFormat(dataset.creditInstitutionKpis?.tradingPortfolioInPercent?.value) ?? "",
            dataSource: dataset.creditInstitutionKpis?.tradingPortfolioInPercent?.dataSource ?? undefined,
          },
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.interbankLoansInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.interbankLoansInPercent,
            value: formatNumberToReadableFormat(dataset.creditInstitutionKpis?.interbankLoansInPercent?.value) ?? "",
            dataSource: dataset.creditInstitutionKpis?.interbankLoansInPercent?.dataSource ?? undefined,
          },
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.tradingPortfolioAndInterbankLoansInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.tradingPortfolioAndInterbankLoansInPercent,
            value:
              formatNumberToReadableFormat(
                dataset.creditInstitutionKpis?.tradingPortfolioAndInterbankLoansInPercent?.value,
              ) ?? "",
            dataSource:
              dataset.creditInstitutionKpis?.tradingPortfolioAndInterbankLoansInPercent?.dataSource ?? undefined,
          },
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.tradingPortfolioAndInterbankLoansInPercent,
            value: formatNumberToReadableFormat(dataset.creditInstitutionKpis?.greenAssetRatioInPercent?.value) ?? "",
            dataSource: dataset.creditInstitutionKpis?.greenAssetRatioInPercent?.dataSource ?? undefined,
          },
        }),
      },
    ],
  },
];
/**
  {
    type: "section",
    label: "Investment-Firm KPIs",
    labelBadgeColor: "blue",
    expandOnPageLoad: false,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.investmentFirmKpis?.greenAssetRatioInPercent,
        }),
      },
    ],
  },
  {
    type: "section",
    label: "Insurance KPIs",
    labelBadgeColor: "blue",
    expandOnPageLoad: false,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.insuranceKpis?.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        }),
      },
    ],
  },
 */
