import {
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
import { formatNumberToReadableFormat } from "@/utils/Formatter";
import { yesNoValueGetterFactory } from "@/components/resources/dataTable/conversion/YesNoValueGetterFactory";
import { plainStringValueGetterFactory } from "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory";
import MultiSelectModal from "@/components/resources/dataTable/modals/MultiSelectModal.vue";

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
        label: euTaxonomyKpiNameMappings.financialServicesTypes,
        explanation: euTaxonomyKpiInfoMappings.financialServicesTypes,
        shouldDisplay: (): boolean => true,
        valueGetter: (): MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
          displayValue: {
            label: "Financial Service Types",
            modalComponent: MultiSelectModal,
            modalOptions: {
              props: {
                header: "Financial Service Types",
                modal: true,
                dismissableMask: true,
              },
              data: [
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
            },
          },
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
            explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution]
                      ?.taxonomyEligibleActivityInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution]
                    ?.taxonomyEligibleActivityInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
            explanation: euTaxonomyKpiInfoMappings.taxonomyNonEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution]
                      ?.taxonomyNonEligibleActivityInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution]
                    ?.taxonomyNonEligibleActivityInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.derivativesInPercent,
            explanation: euTaxonomyKpiInfoMappings.derivativesInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.derivativesInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution]
                      ?.derivativesInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution]
                    ?.derivativesInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
            explanation: euTaxonomyKpiInfoMappings.banksAndIssuersInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution]
                      ?.banksAndIssuersInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution]
                    ?.banksAndIssuersInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
            explanation: euTaxonomyKpiInfoMappings.investmentNonNfrdInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution]
                      ?.investmentNonNfrdInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution]
                    ?.investmentNonNfrdInPercent?.dataSource ?? undefined,
              },
            }),
          },
        ],
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.tradingPortfolioInPercent,
        explanation: euTaxonomyKpiInfoMappings.tradingPortfolioInPercent,
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
        explanation: euTaxonomyKpiInfoMappings.interbankLoansInPercent,
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
        explanation: euTaxonomyKpiInfoMappings.tradingPortfolioAndInterbankLoansInPercent,
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
        explanation: euTaxonomyKpiInfoMappings.greenAssetRatioInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
            value: formatNumberToReadableFormat(dataset.creditInstitutionKpis?.greenAssetRatioInPercent?.value) ?? "",
            dataSource: dataset.creditInstitutionKpis?.greenAssetRatioInPercent?.dataSource ?? undefined,
          },
        }),
      },
    ],
  },
  {
    type: "section",
    label: "Insurance or Reinsurance",
    labelBadgeColor: "blue",
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
            explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[
                      EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance
                    ]?.taxonomyEligibleActivityInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[
                    EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance
                  ]?.taxonomyEligibleActivityInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
            explanation: euTaxonomyKpiInfoMappings.taxonomyNonEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[
                      EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance
                    ]?.taxonomyNonEligibleActivityInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[
                    EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance
                  ]?.taxonomyNonEligibleActivityInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.derivativesInPercent,
            explanation: euTaxonomyKpiInfoMappings.derivativesInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.derivativesInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[
                      EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance
                    ]?.derivativesInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[
                    EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance
                  ]?.derivativesInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
            explanation: euTaxonomyKpiInfoMappings.banksAndIssuersInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[
                      EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance
                    ]?.banksAndIssuersInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[
                    EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance
                  ]?.banksAndIssuersInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
            explanation: euTaxonomyKpiInfoMappings.investmentNonNfrdInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[
                      EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance
                    ]?.investmentNonNfrdInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[
                    EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance
                  ]?.investmentNonNfrdInPercent?.dataSource ?? undefined,
              },
            }),
          },
        ],
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
            value:
              formatNumberToReadableFormat(
                dataset.insuranceKpis?.taxonomyEligibleNonLifeInsuranceActivitiesInPercent?.value,
              ) ?? "",
            dataSource:
              dataset.insuranceKpis?.taxonomyEligibleNonLifeInsuranceActivitiesInPercent?.dataSource ?? undefined,
          },
        }),
      },
    ],
  },
  {
    type: "section",
    label: "Asset Management",
    labelBadgeColor: "blue",
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
            explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement]
                      ?.taxonomyEligibleActivityInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement]
                    ?.taxonomyEligibleActivityInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
            explanation: euTaxonomyKpiInfoMappings.taxonomyNonEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement]
                      ?.taxonomyNonEligibleActivityInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement]
                    ?.taxonomyNonEligibleActivityInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.derivativesInPercent,
            explanation: euTaxonomyKpiInfoMappings.derivativesInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.derivativesInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement]
                      ?.derivativesInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement]
                    ?.derivativesInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
            explanation: euTaxonomyKpiInfoMappings.banksAndIssuersInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement]
                      ?.banksAndIssuersInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement]
                    ?.banksAndIssuersInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
            explanation: euTaxonomyKpiInfoMappings.investmentNonNfrdInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement]
                      ?.investmentNonNfrdInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement]
                    ?.investmentNonNfrdInPercent?.dataSource ?? undefined,
              },
            }),
          },
        ],
      },
    ],
  },
  {
    type: "section",
    label: "Investment Firm",
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
            explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm]
                      ?.taxonomyEligibleActivityInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm]
                    ?.taxonomyEligibleActivityInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
            explanation: euTaxonomyKpiInfoMappings.taxonomyNonEligibleActivityInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm]
                      ?.taxonomyNonEligibleActivityInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm]
                    ?.taxonomyNonEligibleActivityInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.derivativesInPercent,
            explanation: euTaxonomyKpiInfoMappings.derivativesInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.derivativesInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm]
                      ?.derivativesInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm]
                    ?.derivativesInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
            explanation: euTaxonomyKpiInfoMappings.banksAndIssuersInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm]
                      ?.banksAndIssuersInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm]
                    ?.banksAndIssuersInPercent?.dataSource ?? undefined,
              },
            }),
          },
          {
            type: "cell",
            label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
            explanation: euTaxonomyKpiInfoMappings.investmentNonNfrdInPercent,
            shouldDisplay: (): boolean => true,
            valueGetter: (
              dataset: EuTaxonomyDataForFinancials,
            ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
              displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
              displayValue: {
                fieldLabel: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
                value:
                  formatNumberToReadableFormat(
                    dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm]
                      ?.investmentNonNfrdInPercent?.value,
                  ) ?? "",
                dataSource:
                  dataset.eligibilityKpis?.[EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm]
                    ?.investmentNonNfrdInPercent?.dataSource ?? undefined,
              },
            }),
          },
        ],
      },
      {
        type: "cell",
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        explanation: euTaxonomyKpiInfoMappings.greenAssetRatioInPercent,
        shouldDisplay: (): boolean => true,
        valueGetter: (
          dataset: EuTaxonomyDataForFinancials,
        ): MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent> => ({
          displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
          displayValue: {
            fieldLabel: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
            value: formatNumberToReadableFormat(dataset.investmentFirmKpis?.greenAssetRatioInPercent?.value) ?? "",
            dataSource: dataset.investmentFirmKpis?.greenAssetRatioInPercent?.dataSource ?? undefined,
          },
        }),
      },
    ],
  },
];
