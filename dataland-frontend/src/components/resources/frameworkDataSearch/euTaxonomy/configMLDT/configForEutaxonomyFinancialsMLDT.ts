import { euTaxonomyKpiNameMappings } from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
import { type EuTaxonomyDataForFinancials, type YesNo } from "@clients/backend";
import { MLDTDisplayComponentName } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";

export const configForEutaxonomyFinancialsMLDT: MLDTConfig<EuTaxonomyDataForFinancials> = [
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
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
          displayValue: dataset.fiscalYearDeviation,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.fiscalYearEnd,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
          displayValue: dataset.fiscalYearEnd,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.scopeOfEntities,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.scopeOfEntities,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.nfrdMandatory,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.nfrdMandatory,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.euTaxonomyActivityLevelReporting,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): { displayComponentName: MLDTDisplayComponentName; displayValue: YesNo | null | undefined } => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.euTaxonomyActivityLevelReporting,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.assurance,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.assurance,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.numberOfEmployees,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent, //TODO check if coorect display component
          displayValue: dataset.numberOfEmployees,
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
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.eligibilityKpis?.taxonomyEligibleActivityInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.eligibilityKpis?.taxonomyNonEligibleActivityInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.derivativesInPercent,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.eligibilityKpis?.derivativesInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.eligibilityKpis?.banksAndIssuersInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.eligibilityKpis?.investmentNonNfrdInPercent,
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
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.creditInstitutionKpis?.tradingPortfolioInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.interbankLoansInPercent,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.creditInstitutionKpis?.interbankLoansInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.tradingPortfolioAndInterbankLoansInPercent,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.creditInstitutionKpis?.tradingPortfolioAndInterbankLoansInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.eligibilityKpis?.greenAssetRatioInPercent,
        }),
      },
    ],
  },
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
];
