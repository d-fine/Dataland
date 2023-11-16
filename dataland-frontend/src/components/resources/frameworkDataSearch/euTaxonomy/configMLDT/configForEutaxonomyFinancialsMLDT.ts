import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
import {
  type EuTaxonomyDataForFinancials,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
} from "@clients/backend";
import { formatPercentageNumberAsString } from "@/utils/Formatter";
import { yesNoValueGetterFactory } from "@/components/resources/dataTable/conversion/YesNoValueGetterFactory";
import { plainStringValueGetterFactory } from "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory";
import { getDataPointGetterFactory } from "@/components/resources/dataTable/conversion/Utils";
import { type ExtendedDataPoint } from "@/utils/DataPoint";
import { type Field } from "@/utils/GenericFrameworkTypes";
import { multiSelectValueGetterFactory } from "@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";

const sampleField: Field = {
  showIf: () => true,
  name: "",
  label: "",
  description: "",
  unit: "",
  component: "",
};

const specifiedFormatter = function (dataPoint?: ExtendedDataPoint<EuTaxonomyDataForFinancials>): string | undefined {
  return formatPercentageNumberAsString(dataPoint?.value as number);
};

/**
 *
 * @param path gives the file path
 * @param fieldLabel sets the field label
 * @param formatter sets the correct formatter
 * @returns datapointGetterFactory function with label mapped onto the field parameter
 */
function getSpecifiedDataPointGetterFactory(
  path: string,
  fieldLabel: string,
  formatter: (dataPoint?: ExtendedDataPoint<EuTaxonomyDataForFinancials>) => string | undefined,
): (dataset: EuTaxonomyDataForFinancials) => AvailableMLDTDisplayObjectTypes {
  return getDataPointGetterFactory(path, { ...sampleField, label: fieldLabel }, formatter);
}

export const configForEuTaxonomyFinancialsMLDT = [
  {
    type: "section",
    label: "General",
    labelBadgeColor: "orange",
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
              value: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution,
              label: humanizeStringOrNumber(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution),
            },
            {
              value: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance,
              label: humanizeStringOrNumber(
                EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance,
              ),
            },
            {
              value: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement,
              label: humanizeStringOrNumber(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement),
            },
            {
              value: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm,
              label: humanizeStringOrNumber(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm),
            },
          ],
        }),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.fiscalYearDeviation,
        explanation: euTaxonomyKpiInfoMappings.fiscalYearDeviation,
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
        explanation: euTaxonomyKpiInfoMappings.fiscalYearEnd,
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
        explanation: euTaxonomyKpiInfoMappings.scopeOfEntities,
        shouldDisplay: (): boolean => true,
        valueGetter: yesNoValueGetterFactory(euTaxonomyKpiNameMappings.scopeOfEntities),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.nfrdMandatory,
        explanation: euTaxonomyKpiInfoMappings.nfrdMandatory,
        shouldDisplay: (): boolean => true,
        valueGetter: yesNoValueGetterFactory(euTaxonomyKpiNameMappings.nfrdMandatory),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.euTaxonomyActivityLevelReporting,
        explanation: euTaxonomyKpiInfoMappings.euTaxonomyActivityLevelReporting,
        shouldDisplay: (): boolean => true,
        valueGetter: yesNoValueGetterFactory(euTaxonomyKpiNameMappings.euTaxonomyActivityLevelReporting),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.numberOfEmployees,
        explanation: euTaxonomyKpiInfoMappings.numberOfEmployees,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
          displayValue: dataset.numberOfEmployees?.toString(),
        }),
      },
    ],
  },
  {
    type: "section",
    label: "Assurance",
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
    label: "Credit Institution",
    name: "CreditInstitution",
    labelBadgeColor: "red",
    expandOnPageLoad: false,
    shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
      dataset.financialServicesTypes?.includes(
        EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution,
      ) ?? false,
    children: [
      {
        type: "section",
        label: "Eligibility KPIs",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
            name: "taxonomyEligibleActivityInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.CreditInstitution.taxonomyEligibleActivityInPercent",
              euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
            name: "taxonomyNonEligibleActivityInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.taxonomyNonEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.CreditInstitution.taxonomyNonEligibleActivityInPercent",
              euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.derivativesInPercent,
            name: "derivativesInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.derivativesInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.CreditInstitution.derivativesInPercent",
              euTaxonomyKpiNameMappings.derivativesInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
            name: "banksAndIssuersInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.banksAndIssuersInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.CreditInstitution.banksAndIssuersInPercent",
              euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
            name: "investmentNonNfrdInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.investmentNonNfrdInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.CreditInstitution.investmentNonNfrdInPercent",
              euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
              specifiedFormatter,
            ),
          },
        ],
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.tradingPortfolioInPercent,
        name: "tradingPortfolioCreditInstitution",
        explanation: euTaxonomyKpiInfoMappings.tradingPortfolioInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: getSpecifiedDataPointGetterFactory(
          "creditInstitutionKpis.tradingPortfolioInPercent",
          euTaxonomyKpiNameMappings.tradingPortfolioInPercent,
          specifiedFormatter,
        ),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.interbankLoansInPercent,
        name: "interbankLoansCreditInstitution",
        explanation: euTaxonomyKpiInfoMappings.interbankLoansInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: getSpecifiedDataPointGetterFactory(
          "creditInstitutionKpis.interbankLoansInPercent",
          euTaxonomyKpiNameMappings.interbankLoansInPercent,
          specifiedFormatter,
        ),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.tradingPortfolioAndInterbankLoansInPercent,
        explanation: euTaxonomyKpiInfoMappings.tradingPortfolioAndInterbankLoansInPercent,
        name: "tradingPortfolioAndInterbankLoansInPercent",
        shouldDisplay: (): boolean => true,
        valueGetter: getSpecifiedDataPointGetterFactory(
          "creditInstitutionKpis.tradingPortfolioAndInterbankLoansInPercent",
          euTaxonomyKpiNameMappings.tradingPortfolioAndInterbankLoansInPercent,
          specifiedFormatter,
        ),
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        name: "greenAssetRatioCreditInstitution",
        explanation: euTaxonomyKpiInfoMappings.greenAssetRatioInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: getSpecifiedDataPointGetterFactory(
          "creditInstitutionKpis.greenAssetRatioInPercent",
          euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
          specifiedFormatter,
        ),
      },
    ],
  },
  {
    type: "section",
    label: "Insurance or Reinsurance",
    name: "InsuranceOrReinsurance",
    labelBadgeColor: "green",
    expandOnPageLoad: false,
    shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
      dataset.financialServicesTypes?.includes(
        EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance,
      ) ?? false,
    children: [
      {
        type: "section",
        label: "Eligibility KPIs",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
            name: "taxonomyEligibleActivityInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.InsuranceOrReinsurance.taxonomyEligibleActivityInPercent",
              euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
            name: "taxonomyNonEligibleActivityInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.taxonomyNonEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.InsuranceOrReinsurance.taxonomyNonEligibleActivityInPercent",
              euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.derivativesInPercent,
            name: "derivativesInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.derivativesInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.InsuranceOrReinsurance.derivativesInPercent",
              euTaxonomyKpiNameMappings.derivativesInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
            name: "banksAndIssuersInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.banksAndIssuersInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.InsuranceOrReinsurance.banksAndIssuersInPercent",
              euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
            name: "investmentNonNfrdInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.investmentNonNfrdInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.InsuranceOrReinsurance.investmentNonNfrdInPercent",
              euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
              specifiedFormatter,
            ),
          },
        ],
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        name: "taxonomyEligibleNonLifeInsuranceActivities",
        explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: getSpecifiedDataPointGetterFactory(
          "insuranceKpis.taxonomyEligibleNonLifeInsuranceActivitiesInPercent",
          euTaxonomyKpiNameMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
          specifiedFormatter,
        ),
      },
    ],
  },
  {
    type: "section",
    label: "Asset Management",
    name: "AssetManagement",
    labelBadgeColor: "green",
    expandOnPageLoad: false,
    shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
      dataset.financialServicesTypes?.includes(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement) ??
      false,
    children: [
      {
        type: "section",
        label: "Eligibility KPIs",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
            name: "taxonomyEligibleActivityInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.AssetManagement.taxonomyEligibleActivityInPercent",
              euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
            name: "taxonomyNonEligibleActivityInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.taxonomyNonEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.AssetManagement.taxonomyNonEligibleActivityInPercent",
              euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.derivativesInPercent,
            name: "derivativesInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.derivativesInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.AssetManagement.derivativesInPercent",
              euTaxonomyKpiNameMappings.derivativesInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
            name: "banksAndIssuersInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.banksAndIssuersInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.AssetManagement.banksAndIssuersInPercent",
              euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
            name: "investmentNonNfrdInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.investmentNonNfrdInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.AssetManagement.investmentNonNfrdInPercent",
              euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
              specifiedFormatter,
            ),
          },
        ],
      },
    ],
  },
  {
    type: "section",
    label: "Investment Firm",
    name: "InvestmentFirm",
    labelBadgeColor: "blue",
    expandOnPageLoad: false,
    shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
      dataset.financialServicesTypes?.includes(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm) ??
      false,
    children: [
      {
        type: "section",
        label: "Eligibility KPIs",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
            name: "taxonomyEligibleActivityInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.InvestmentFirm.taxonomyEligibleActivityInPercent",
              euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
            name: "taxonomyNonEligibleActivityInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.taxonomyNonEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.InvestmentFirm.taxonomyNonEligibleActivityInPercent",
              euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.derivativesInPercent,
            name: "derivativesInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.derivativesInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.InvestmentFirm.derivativesInPercent",
              euTaxonomyKpiNameMappings.derivativesInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
            name: "banksAndIssuersInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.banksAndIssuersInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.InvestmentFirm.banksAndIssuersInPercent",
              euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
              specifiedFormatter,
            ),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
            name: "investmentNonNfrdInPercent",
            class: "indentation",
            explanation: euTaxonomyKpiInfoMappings.investmentNonNfrdInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: getSpecifiedDataPointGetterFactory(
              "eligibilityKpis.InvestmentFirm.investmentNonNfrdInPercent",
              euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
              specifiedFormatter,
            ),
          },
        ],
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        name: "greenAssetRatioInvestmentFirm",
        explanation: euTaxonomyKpiInfoMappings.greenAssetRatioInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: getSpecifiedDataPointGetterFactory(
          "investmentFirmKpis.greenAssetRatioInPercent",
          euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
          specifiedFormatter,
        ),
      },
    ],
  },
];
