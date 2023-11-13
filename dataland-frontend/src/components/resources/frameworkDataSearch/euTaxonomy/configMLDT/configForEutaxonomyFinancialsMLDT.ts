import { euTaxonomyKpiNameMappings } from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
/*
import {formatPercentageNumberAsString} from "@/utils/Formatter";
import {getDataPointGetterFactory} from "@/components/resources/dataTable/conversion/Utils";
import {
  singleSelectValueGetterFactory
} from "@/components/resources/dataTable/conversion/SingleSelectValueGetterFactory";
import {plainStringValueGetterFactory} from "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory";
import {yesNoValueGetterFactory} from "@/components/resources/dataTable/conversion/YesNoValueGetterFactory";
import {numberValueGetterFactory} from "@/components/resources/dataTable/conversion/NumberValueGetterFactory";
import {type Field} from "@/utils/GenericFrameworkTypes";
import {multiSelectValueGetterFactory} from "@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory";
*/
import {
  type AssuranceDataPoint,
  type EligibilityKpis,
  type EuTaxonomyDataForFinancials,
  type ExtendedDataPointBigDecimal,
  type FiscalYearDeviation,
  type YesNo,
  type YesNoNa,
} from "@clients/backend";
import { MLDTDisplayComponentName } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";

/**
const sampleField: Field = {
  showIf: () => true,
  name: "",
  label: "",
  description: "",
  unit: "",
  component: "",
};
 
const sampleFormatter = function (dataPoint){
  if (dataPoint) {
    return formatPercentageNumberAsString(dataPoint.value);
  }
};
 
const generateEligibilityKpis = function(name: Field, color = "yellow") {
  function sampleCell(fieldName:string) {
    const label = euTaxonomyKpiNameMappings[fieldName];
    return {
      type: "cell",
      explanation: euTaxonomyKpiInfoMappings[field],
      class: "indentation",
      label,
      shouldDisplay: () => true,
      valueGetter: getDataPointGetterFactory(
        `eligibilityKpis.${name}.${field}`,
        { ...sampleField, label },
        sampleFormatter,
      ),
    };
  }
 
  return {
    type: "section",
    label: "Eligibility Kpis",
    expandOnPageLoad: true,
    shouldDisplay: (): boolean => true,
    children: [
      sampleCell("taxonomyEligibleActivityInPercent"),
      sampleCell("taxonomyNonEligibleActivityInPercent"),
      sampleCell("derivativesInPercent"),
      sampleCell("banksAndIssuersInPercent"),
      sampleCell("investmentNonNfrdInPercent"),
    ],
  };
};
 
export const oldconfigForEutaxonomyFinancialsMLDT = [
  {
    type: "section",
    label: "General",
    labelBadgeColor: "orange",
    expandOnPageLoad: true,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "section",
        label: "General",
        expandOnPageLoad: true,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.financialServicesTypes,
            explanation: euTaxonomyKpiInfoMappings.financialServicesTypes,
            shouldDisplay: (): boolean => true,
            valueGetter: multiSelectValueGetterFactory("financialServicesTypes", {
              ...sampleField,
              label: euTaxonomyKpiNameMappings.financialServicesTypes,
              options: [
                {
                  value: "CreditInstitution",
                  label: "CreditInstitution",
                },
                {
                  value: "InsuranceOrReinsurance",
                  label: "InsuranceOrReinsurance",
                },
                {
                  value: "AssetManagement",
                  label: "AssetManagement",
                },
                {
                  value: "InvestmentFirm",
                  label: "InvestmentFirm",
                },
              ],
            }),
          },
          {
            label: "Fiscal Year Deviation",
            explanation: "Fiscal Year (Deviation/ No Deviation)",
            type: "cell",
            shouldDisplay: (): boolean => true,
            valueGetter: singleSelectValueGetterFactory("fiscalYearDeviation", {
              ...sampleField,
              options: [
                { label: "Deviation", value: "Deviation" },
                { label: "No Deviation", value: "NoDeviation" },
              ],
            }),
          },
          {
            label: euTaxonomyKpiNameMappings.fiscalYearEnd,
            explanation: euTaxonomyKpiInfoMappings.fiscalYearEnd,
            type: "cell",
            shouldDisplay: (): boolean => true,
            valueGetter: plainStringValueGetterFactory("fiscalYearEnd"),
          },
          {
            label: "Scope Of Entities",
            explanation:
              "Does a list of legal entities covered by Sust./Annual/Integrated/ESEF report match with a list of legal entities covered by Audited Consolidated Financial Statement ",
            type: "cell",
            shouldDisplay: (): boolean => true,
            valueGetter: yesNoValueGetterFactory("scopeOfEntities"),
          },
          {
            label: "EU Taxonomy Activity Level Reporting",
            explanation: "Activity Level disclosure",
            type: "cell",
            shouldDisplay: (): boolean => true,
            valueGetter: yesNoValueGetterFactory("euTaxonomyActivityLevelReporting"),
          },
          {
            label: "Number Of Employees",
            explanation: "Total number of employees (including temporary workers)",
            type: "cell",
            shouldDisplay: (): boolean => true,
            valueGetter: numberValueGetterFactory("numberOfEmployees", sampleField),
          },
          {
            label: "NFRD Mandatory",
            explanation: "The reporting obligation for companies whose number of employees is greater or equal to 500",
            type: "cell",
            shouldDisplay: (): boolean => true,
            valueGetter: yesNoValueGetterFactory("nfrdMandatory"),
          },
        ],
      },
    ],
  },
 
  {
    type: "section",
    label: euTaxonomyKpiNameMappings.assurance,
    labelBadgeColor: "orange",
    expandOnPageLoad: false,
    shouldDisplay: (): boolean => true,
    children: [
      {
        label: "Level of Assurance",
        explanation: euTaxonomyKpiInfoMappings.assurance,
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: plainStringValueGetterFactory("assurance.value"),
      },
      {
        label: euTaxonomyKpiNameMappings.provider,
        explanation: euTaxonomyKpiInfoMappings.provider,
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: plainStringValueGetterFactory("assurance.provider"),
      },
    ],
  },
  {
    type: "section",
    label: euTaxonomyKpiNameMappings.CreditInstitution,
    name: "CreditInstitution",
    labelBadgeColor: "green",
    expandOnPageLoad: false,
    shouldDisplay: (dataset) => dataset.financialServicesTypes.includes("CreditInstitution"),
    children: [
      generateEligibilityKpis("CreditInstitution", "green"),
 
      {
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        explanation: euTaxonomyKpiInfoMappings.greenAssetRatioInPercent,
        type: "cell",
        name: "greenAssetRatioCreditInstitution",
        shouldDisplay: (): boolean => true,
        valueGetter: getDataPointGetterFactory(
          "creditInstitutionKpis.greenAssetRatioInPercent",
          { ...sampleField, label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent },
          sampleFormatter,
        ),
      },
      {
        label: euTaxonomyKpiNameMappings.tradingPortfolioAndInterbankLoansInPercent,
        explanation: euTaxonomyKpiInfoMappings.tradingPortfolioAndInterbankLoansInPercent,
        type: "cell",
        name: "tradingPortfolioAndInterbankLoansInPercent",
        shouldDisplay: (): boolean => true,
        valueGetter: getDataPointGetterFactory(
          "creditInstitutionKpis.tradingPortfolioAndInterbankLoansInPercent",
          { ...sampleField, label: euTaxonomyKpiNameMappings.tradingPortfolioAndInterbankLoansInPercent },
          sampleFormatter,
        ),
      },
      {
        label: euTaxonomyKpiNameMappings.tradingPortfolioInPercent,
        explanation: euTaxonomyKpiInfoMappings.tradingPortfolioInPercent,
        type: "cell",
        name: "tradingPortfolioCreditInstitution",
        shouldDisplay: (): boolean => true,
        valueGetter: getDataPointGetterFactory(
          "creditInstitutionKpis.tradingPortfolioInPercent",
          { ...sampleField, label: euTaxonomyKpiNameMappings.tradingPortfolioInPercent },
          sampleFormatter,
        ),
      },
      {
        label: euTaxonomyKpiNameMappings.interbankLoansInPercent,
        explanation: euTaxonomyKpiInfoMappings.interbankLoansInPercent,
        type: "cell",
        name: "interbankLoansCreditInstitution",
        shouldDisplay: (): boolean => true,
        valueGetter: getDataPointGetterFactory(
          "creditInstitutionKpis.interbankLoansInPercent",
          { ...sampleField, label: euTaxonomyKpiNameMappings.interbankLoansInPercent },
          sampleFormatter,
        ),
      },
    ],
  },
 
  {
    type: "section",
    label: euTaxonomyKpiNameMappings.InsuranceOrReinsurance,
    name: "InsuranceOrReinsurance",
    labelBadgeColor: "red",
    expandOnPageLoad: false,
    shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean | undefined =>
      dataset.financialServicesTypes?.includes("InsuranceOrReinsurance"),
    children: [
      generateEligibilityKpis("InsuranceOrReinsurance", "red"),
 
      {
        label: euTaxonomyKpiNameMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        type: "cell",
        name: "taxonomyEligibleNonLifeInsuranceActivities",
        shouldDisplay: (): boolean => true,
        valueGetter: getDataPointGetterFactory(
          "insuranceKpis.taxonomyEligibleNonLifeInsuranceActivitiesInPercent",
          { ...sampleField, label: euTaxonomyKpiNameMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent },
          sampleFormatter,
        ),
      },
    ],
  },
 
  {
    type: "section",
    label: euTaxonomyKpiNameMappings.InvestmentFirm,
    name: "InvestmentFirm",
    labelBadgeColor: "purple",
    expandOnPageLoad: false,
    shouldDisplay: (dataset) => dataset.financialServicesTypes.includes("InvestmentFirm"),
    children: [
      generateEligibilityKpis("InvestmentFirm", "purple"),
 
      {
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        explanation: euTaxonomyKpiInfoMappings.greenAssetRatioInPercent,
        type: "cell",
        name: "greenAssetRatioInvestmentFirm",
        shouldDisplay: (): boolean => true,
        valueGetter: getDataPointGetterFactory(
          "InvestmentFirm.greenAssetRatioInPercent",
          { ...sampleField, label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent },
          sampleFormatter,
        ),
      },
    ],
},
 
  {
    type: "section",
    label: euTaxonomyKpiNameMappings.AssetManagement,
    name: "AssetManagement",
    labelBadgeColor: "blue",
    expandOnPageLoad: false,
    shouldDisplay: (dataset: EuTaxonomyDataForFinancials) => dataset.financialServicesTypes.includes("AssetManagement"),
    children: [generateEligibilityKpis("AssetManagement", "blue")],
  },
];
 */

export const configForEutaxonomyFinancialsMLDT = [
  {
    type: "section",
    label: "General",
    labelBadgeColor: "yellow",
    expandOnPageLoad: true,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.fiscalYearDeviation,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): {
          displayComponentName: MLDTDisplayComponentName;
          displayValue: FiscalYearDeviation | null | undefined;
        } => ({
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
          displayValue: dataset.fiscalYearDeviation,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.fiscalYearEnd,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): { displayComponentName: MLDTDisplayComponentName; displayValue: string | null | undefined } => ({
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
          displayValue: dataset.fiscalYearEnd,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.scopeOfEntities,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): { displayComponentName: MLDTDisplayComponentName; displayValue: YesNoNa | null | undefined } => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.scopeOfEntities,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.nfrdMandatory,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): { displayComponentName: MLDTDisplayComponentName; displayValue: YesNo | null | undefined } => ({
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
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): { displayComponentName: MLDTDisplayComponentName; displayValue: AssuranceDataPoint | null | undefined } => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.assurance,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.numberOfEmployees,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): { displayComponentName: MLDTDisplayComponentName; displayValue: number | null | undefined } => ({
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent, //TODO check if coorect display component
          displayValue: dataset.numberOfEmployees,
        }),
      },
    ],
  },
  {
    type: "section",
    label: "Eligibility KPIs",
    labelBadgeColor: "blue",
    expandOnPageLoad: true,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): { displayComponentName: MLDTDisplayComponentName; displayValue: EligibilityKpis | null | undefined } => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.eligibilityKpis?.taxonomyEligibleActivityInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): { displayComponentName: MLDTDisplayComponentName; displayValue: EligibilityKpis | undefined } => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.eligibilityKpis?.taxonomyNonEligibleActivityInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.derivativesInPercent,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): { displayComponentName: MLDTDisplayComponentName; displayValue: EligibilityKpis | undefined } => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.eligibilityKpis?.derivativesInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): { displayComponentName: MLDTDisplayComponentName; displayValue: EligibilityKpis | undefined } => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.eligibilityKpis?.banksAndIssuersInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): { displayComponentName: MLDTDisplayComponentName; displayValue: EligibilityKpis | undefined } => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.eligibilityKpis?.investmentNonNfrdInPercent,
        }),
      },
    ],
  },
  {
    type: "section",
    label: "Credit Institution KPIs",
    labelBadgeColor: "blue",
    expandOnPageLoad: true,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.tradingPortfolioInPercent,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): {
          displayComponentName: MLDTDisplayComponentName;
          displayValue: ExtendedDataPointBigDecimal | null | undefined;
        } => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.creditInstitutionKpis?.tradingPortfolioInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.interbankLoansInPercent,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): {
          displayComponentName: MLDTDisplayComponentName;
          displayValue: ExtendedDataPointBigDecimal | null | undefined;
        } => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.creditInstitutionKpis?.interbankLoansInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.tradingPortfolioAndInterbankLoansInPercent,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): {
          displayComponentName: MLDTDisplayComponentName;
          displayValue: ExtendedDataPointBigDecimal | null | undefined;
        } => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.creditInstitutionKpis?.tradingPortfolioAndInterbankLoansInPercent,
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): {
          displayComponentName: MLDTDisplayComponentName;
          displayValue: ExtendedDataPointBigDecimal | null | undefined;
        } => ({
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
    expandOnPageLoad: true,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): {
          displayComponentName: MLDTDisplayComponentName;
          displayValue: ExtendedDataPointBigDecimal | null | undefined;
        } => ({
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
    expandOnPageLoad: true,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): {
          displayComponentName: MLDTDisplayComponentName;
          displayValue: ExtendedDataPointBigDecimal | null | undefined;
        } => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: dataset.insuranceKpis?.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        }),
      },
    ],
  },
];
