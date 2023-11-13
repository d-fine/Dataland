import {
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { euTaxonomyKpiNameMappings } from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
import { type EuTaxonomyDataForFinancials } from "@clients/backend";

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
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
          displayValue: dataset.scopeOfEntities,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.nfrdMandatory,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
          displayValue: dataset.nfrdMandatory,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.euTaxonomyActivityLevelReporting,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
          displayValue: dataset.euTaxonomyActivityLevelReporting,
        }),
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
            value: dataset.assurance?.value ? dataset.assurance.value : "",
            dataSource: dataset.assurance?.dataSource ? dataset.assurance.dataSource : undefined,
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
  /**
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
        shouldDisplay: () : boolean => true,
        valueGetter: (dataset: EuTaxonomyDataForFinancials) :
            MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent, //TODO
            value: dataset.eligibilityKpis?['taxonomyEligibleActivityInPercent']  ?
                dataset.eligibilityKpis?["taxonomyEligibleActivityInPercent"] ?
                    dataset.eligibilityKpis?["taxonomyEligibleActivityInPercent"].value : undefined,
            dataSource: dataset.eligibilityKpis?.taxonomyEligibleActivityInPercent?.dataSource ? dataset.eligibilityKpis.taxonomyEligibleActivityInPercent.dataSource : undefined,
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
   */

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
            value: dataset.creditInstitutionKpis?.tradingPortfolioInPercent?.value
              ? dataset.creditInstitutionKpis?.tradingPortfolioInPercent?.value.toString()
              : "",
            dataSource: dataset.creditInstitutionKpis?.tradingPortfolioInPercent?.dataSource
              ? dataset.creditInstitutionKpis?.tradingPortfolioInPercent?.dataSource
              : undefined,
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
            value: dataset.creditInstitutionKpis?.interbankLoansInPercent?.value
              ? dataset.creditInstitutionKpis?.interbankLoansInPercent?.value.toString()
              : "",
            dataSource: dataset.creditInstitutionKpis?.interbankLoansInPercent?.dataSource
              ? dataset.creditInstitutionKpis?.interbankLoansInPercent?.dataSource
              : undefined,
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
            value: dataset.creditInstitutionKpis?.tradingPortfolioAndInterbankLoansInPercent?.value
              ? dataset.creditInstitutionKpis?.tradingPortfolioAndInterbankLoansInPercent?.value.toString()
              : "",
            dataSource: dataset.creditInstitutionKpis?.tradingPortfolioAndInterbankLoansInPercent?.dataSource
              ? dataset.creditInstitutionKpis?.tradingPortfolioAndInterbankLoansInPercent?.dataSource
              : undefined,
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
            value: dataset.creditInstitutionKpis?.greenAssetRatioInPercent?.value
              ? dataset.creditInstitutionKpis?.greenAssetRatioInPercent?.value.toString()
              : "",
            dataSource: dataset.creditInstitutionKpis?.greenAssetRatioInPercent?.dataSource
              ? dataset.creditInstitutionKpis?.greenAssetRatioInPercent?.dataSource
              : undefined,
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
