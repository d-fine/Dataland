import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
import { formatPercentageNumberAsString } from "@/utils/Formatter";
import { getDataPointGetterFactory } from "@/components/resources/dataTable/conversion/Utils";
import { singleSelectValueGetterFactory } from "@/components/resources/dataTable/conversion/SingleSelectValueGetterFactory";
import { plainStringValueGetterFactory } from "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory";
import { yesNoValueGetterFactory } from "@/components/resources/dataTable/conversion/YesNoValueGetterFactory";
import { numberValueGetterFactory } from "@/components/resources/dataTable/conversion/NumberValueGetterFactory";
import { Field } from "@/utils/GenericFrameworkTypes";
import { multiSelectValueGetterFactory } from "@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory";

const sampleField: Field = {
  showIf: () => true,
  name: "",
  label: "",
  description: "",
  unit: "",
  component: "",
};

const sampleFormatter = function (dataPoint: any) {
  return formatPercentageNumberAsString(dataPoint?.value);
};

const generateEligibilityKpis = function (name, color = "yellow") {
  function sampleCell(field) {
    const label = euTaxonomyKpiNameMappings[field];
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
    shouldDisplay: () => true,
    children: [
      sampleCell("taxonomyEligibleActivityInPercent"),
      sampleCell("taxonomyNonEligibleActivityInPercent"),
      sampleCell("derivativesInPercent"),
      sampleCell("banksAndIssuersInPercent"),
      sampleCell("investmentNonNfrdInPercent"),
    ],
  };
};

export const configForEutaxonomyFinancialsMLDT = [
  {
    type: "section",
    label: "GENERAL",
    labelBadgeColor: "orange",
    expandOnPageLoad: true,
    shouldDisplay: () => true,
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
    shouldDisplay: () => true,
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

  // (dataset) => dataset.financialServicesTypes.includes(name),
  {
    type: "section",
    label: euTaxonomyKpiNameMappings.CreditInstitution,
    labelBadgeColor: "green",
    expandOnPageLoad: false,
    shouldDisplay: (dataset) => dataset.financialServicesTypes.includes("CreditInstitution"),
    children: [
      generateEligibilityKpis("CreditInstitution", "green"),

      {
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        explanation: euTaxonomyKpiInfoMappings.greenAssetRatioInPercent,
        type: "cell",
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
    labelBadgeColor: "red",
    expandOnPageLoad: false,
    shouldDisplay: (dataset) => dataset.financialServicesTypes.includes("InsuranceOrReinsurance"),
    children: [
      generateEligibilityKpis("InsuranceOrReinsurance", "red"),

      {
        label: euTaxonomyKpiNameMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        type: "cell",
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
    labelBadgeColor: "purple",
    expandOnPageLoad: false,
    shouldDisplay: (dataset) => dataset.financialServicesTypes.includes("InvestmentFirm"),
    children: [
      generateEligibilityKpis("InvestmentFirm", "purple"),

      {
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        explanation: euTaxonomyKpiInfoMappings.greenAssetRatioInPercent,
        type: "cell",
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
    labelBadgeColor: "blue",
    expandOnPageLoad: false,
    shouldDisplay: (dataset) => dataset.financialServicesTypes.includes("AssetManagement"),
    children: [generateEligibilityKpis("AssetManagement", "blue")],
  },
];
