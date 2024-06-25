import { type AvailableMLDTDisplayObjectTypes } from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from '@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel';
import {
  type EuTaxonomyDataForFinancials,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
  type ExtendedDataPointBigDecimal,
} from '@clients/backend';
import { formatPercentageNumberAsString } from '@/utils/Formatter';
import { yesNoValueGetterFactory } from '@/components/resources/dataTable/conversion/YesNoValueGetterFactory';
import { plainStringValueGetterFactory } from '@/components/resources/dataTable/conversion/PlainStringValueGetterFactory';
import { getDataPointGetterFactory } from '@/components/resources/dataTable/conversion/DataPoints';
import { type ExtendedDataPoint } from '@/utils/DataPoint';
import { type Field } from '@/utils/GenericFrameworkTypes';
import { multiSelectValueGetterFactory } from '@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { numberValueGetterFactory } from '@/components/resources/dataTable/conversion/NumberValueGetterFactory';

const sampleField: Field = {
  showIf: () => true,
  name: '',
  label: '',
  description: '',
  unit: '',
  component: '',
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
  formatter: (dataPoint?: ExtendedDataPoint<EuTaxonomyDataForFinancials>) => string | undefined
): (dataset: EuTaxonomyDataForFinancials) => AvailableMLDTDisplayObjectTypes {
  return getDataPointGetterFactory(path, { ...sampleField, label: fieldLabel }, formatter);
}

/**
 * The function defines the trigger for the shouldDisplay property for field values
 * @param fieldValue value of the field which is checked
 * @returns boolean to trigger the shouldDisplay condition
 */
function shouldValueBeDisplayedForSimpleFields(fieldValue: undefined | null | string | number): boolean {
  return !!fieldValue || fieldValue === 0;
}

/**
 * Returns a boolean for the shouldDisplay function. Determines from the backend dataset if the value should be shown on the MLDT
 * @param dataPoint dataPoint from backend response
 * @returns boolean value for shouldDisplay
 */
function shouldValueBeDisplayedForDataPoint(dataPoint: ExtendedDataPointBigDecimal | null | undefined): boolean {
  return !!(
    dataPoint?.value != null ||
    dataPoint?.comment?.length ||
    (dataPoint?.quality != null && dataPoint?.dataSource?.fileReference.length)
  );
}

export const configForEuTaxonomyFinancialsMLDT = [
  {
    type: 'section',
    label: 'General',
    labelBadgeColor: 'orange',
    expandOnPageLoad: true,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: 'cell',
        label: euTaxonomyKpiNameMappings.financialServicesTypes,
        explanation: euTaxonomyKpiInfoMappings.financialServicesTypes,
        shouldDisplay: (): boolean => true,
        valueGetter: multiSelectValueGetterFactory('financialServicesTypes', {
          ...sampleField,
          label: euTaxonomyKpiNameMappings.financialServicesTypes,
          options: Object.values(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum).map((financialServiceType) => ({
            value: financialServiceType,
            label: humanizeStringOrNumber(financialServiceType),
          })),
        }),
      },
      {
        type: 'cell',
        label: euTaxonomyKpiNameMappings.fiscalYearDeviation,
        explanation: euTaxonomyKpiInfoMappings.fiscalYearDeviation,
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForSimpleFields(dataset.fiscalYearDeviation),
        valueGetter: plainStringValueGetterFactory('fiscalYearDeviation'),
      },
      {
        type: 'cell',
        label: euTaxonomyKpiNameMappings.fiscalYearEnd,
        explanation: euTaxonomyKpiInfoMappings.fiscalYearEnd,
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForSimpleFields(dataset.fiscalYearEnd),
        valueGetter: plainStringValueGetterFactory('fiscalYearEnd'),
      },
      {
        type: 'cell',
        label: euTaxonomyKpiNameMappings.scopeOfEntities,
        explanation: euTaxonomyKpiInfoMappings.scopeOfEntities,
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForSimpleFields(dataset.scopeOfEntities),
        valueGetter: yesNoValueGetterFactory('scopeOfEntities'),
      },
      {
        type: 'cell',
        label: euTaxonomyKpiNameMappings.nfrdMandatory,
        explanation: euTaxonomyKpiInfoMappings.nfrdMandatory,
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForSimpleFields(dataset.nfrdMandatory),
        valueGetter: yesNoValueGetterFactory('nfrdMandatory'),
      },
      {
        type: 'cell',
        label: euTaxonomyKpiNameMappings.euTaxonomyActivityLevelReporting,
        explanation: euTaxonomyKpiInfoMappings.euTaxonomyActivityLevelReporting,
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForSimpleFields(dataset.euTaxonomyActivityLevelReporting),
        valueGetter: yesNoValueGetterFactory('euTaxonomyActivityLevelReporting'),
      },
      {
        type: 'cell',
        label: euTaxonomyKpiNameMappings.numberOfEmployees,
        explanation: euTaxonomyKpiInfoMappings.numberOfEmployees,
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForSimpleFields(dataset.numberOfEmployees),
        valueGetter: numberValueGetterFactory('numberOfEmployees', sampleField),
      },
    ],
  },
  {
    type: 'section',
    label: 'Assurance',
    labelBadgeColor: 'orange',
    expandOnPageLoad: true,
    shouldDisplay: (): boolean => true,
    children: [
      {
        label: 'Level of Assurance',
        explanation: euTaxonomyKpiInfoMappings.assurance,
        type: 'cell',
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForSimpleFields(dataset.assurance?.value),
        valueGetter: plainStringValueGetterFactory('assurance.value'),
      },
      {
        label: euTaxonomyKpiNameMappings.provider,
        explanation: euTaxonomyKpiInfoMappings.provider,
        type: 'cell',
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForSimpleFields(dataset.assurance?.provider),
        valueGetter: plainStringValueGetterFactory('assurance.provider'),
      },
    ],
  },
  {
    type: 'section',
    label: 'Credit Institution',
    name: 'CreditInstitution',
    labelBadgeColor: 'red',
    expandOnPageLoad: true,
    shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
      dataset.financialServicesTypes?.includes(
        EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution
      ) ?? false,
    children: [
      {
        type: 'section',
        label: 'Eligibility KPIs',
        expandOnPageLoad: true,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
            name: 'taxonomyEligibleActivityInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleActivityInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(
                dataset.eligibilityKpis?.CreditInstitution?.taxonomyEligibleActivityInPercent
              ),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.CreditInstitution.taxonomyEligibleActivityInPercent',
              euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
            name: 'taxonomyNonEligibleActivityInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.taxonomyNonEligibleActivityInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(
                dataset.eligibilityKpis?.CreditInstitution?.taxonomyNonEligibleActivityInPercent
              ),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.CreditInstitution.taxonomyNonEligibleActivityInPercent',
              euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.derivativesInPercent,
            name: 'derivativesInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.derivativesInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(dataset.eligibilityKpis?.CreditInstitution?.derivativesInPercent),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.CreditInstitution.derivativesInPercent',
              euTaxonomyKpiNameMappings.derivativesInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
            name: 'banksAndIssuersInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.banksAndIssuersInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(dataset.eligibilityKpis?.CreditInstitution?.banksAndIssuersInPercent),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.CreditInstitution.banksAndIssuersInPercent',
              euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
            name: 'investmentNonNfrdInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.investmentNonNfrdInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(
                dataset.eligibilityKpis?.CreditInstitution?.investmentNonNfrdInPercent
              ),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.CreditInstitution.investmentNonNfrdInPercent',
              euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
              specifiedFormatter
            ),
          },
        ],
      },
      {
        type: 'cell',
        label: euTaxonomyKpiNameMappings.tradingPortfolioInPercent,
        name: 'tradingPortfolioCreditInstitution',
        explanation: euTaxonomyKpiInfoMappings.tradingPortfolioInPercent,
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForDataPoint(dataset.creditInstitutionKpis?.tradingPortfolioInPercent),
        valueGetter: getSpecifiedDataPointGetterFactory(
          'creditInstitutionKpis.tradingPortfolioInPercent',
          euTaxonomyKpiNameMappings.tradingPortfolioInPercent,
          specifiedFormatter
        ),
      },
      {
        type: 'cell',
        label: euTaxonomyKpiNameMappings.interbankLoansInPercent,
        name: 'interbankLoansCreditInstitution',
        explanation: euTaxonomyKpiInfoMappings.interbankLoansInPercent,
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForDataPoint(dataset.creditInstitutionKpis?.interbankLoansInPercent),
        valueGetter: getSpecifiedDataPointGetterFactory(
          'creditInstitutionKpis.interbankLoansInPercent',
          euTaxonomyKpiNameMappings.interbankLoansInPercent,
          specifiedFormatter
        ),
      },
      {
        type: 'cell',
        label: euTaxonomyKpiNameMappings.tradingPortfolioAndInterbankLoansInPercent,
        explanation: euTaxonomyKpiInfoMappings.tradingPortfolioAndInterbankLoansInPercent,
        name: 'tradingPortfolioAndInterbankLoansInPercent',
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForDataPoint(dataset.creditInstitutionKpis?.tradingPortfolioAndInterbankLoansInPercent),
        valueGetter: getSpecifiedDataPointGetterFactory(
          'creditInstitutionKpis.tradingPortfolioAndInterbankLoansInPercent',
          euTaxonomyKpiNameMappings.tradingPortfolioAndInterbankLoansInPercent,
          specifiedFormatter
        ),
      },
      {
        type: 'cell',
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        name: 'greenAssetRatioCreditInstitution',
        explanation: euTaxonomyKpiInfoMappings.greenAssetRatioInPercent,
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForDataPoint(dataset.creditInstitutionKpis?.greenAssetRatioInPercent),
        valueGetter: getSpecifiedDataPointGetterFactory(
          'creditInstitutionKpis.greenAssetRatioInPercent',
          euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
          specifiedFormatter
        ),
      },
    ],
  },
  {
    type: 'section',
    label: 'Insurance or Reinsurance',
    name: 'InsuranceOrReinsurance',
    labelBadgeColor: 'green',
    expandOnPageLoad: true,
    shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
      dataset.financialServicesTypes?.includes(
        EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance
      ) ?? false,
    children: [
      {
        type: 'section',
        label: 'Eligibility KPIs',
        expandOnPageLoad: true,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
            name: 'taxonomyEligibleActivityInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleActivityInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(
                dataset.eligibilityKpis?.InsuranceOrReinsurance?.taxonomyEligibleActivityInPercent
              ),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.InsuranceOrReinsurance.taxonomyEligibleActivityInPercent',
              euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
            name: 'taxonomyNonEligibleActivityInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.taxonomyNonEligibleActivityInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(
                dataset.eligibilityKpis?.InsuranceOrReinsurance?.taxonomyNonEligibleActivityInPercent
              ),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.InsuranceOrReinsurance.taxonomyNonEligibleActivityInPercent',
              euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.derivativesInPercent,
            name: 'derivativesInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.derivativesInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(dataset.eligibilityKpis?.InsuranceOrReinsurance?.derivativesInPercent),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.InsuranceOrReinsurance.derivativesInPercent',
              euTaxonomyKpiNameMappings.derivativesInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
            name: 'banksAndIssuersInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.banksAndIssuersInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(
                dataset.eligibilityKpis?.InsuranceOrReinsurance?.banksAndIssuersInPercent
              ),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.InsuranceOrReinsurance.banksAndIssuersInPercent',
              euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
            name: 'investmentNonNfrdInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.investmentNonNfrdInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(
                dataset.eligibilityKpis?.InsuranceOrReinsurance?.investmentNonNfrdInPercent
              ),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.InsuranceOrReinsurance.investmentNonNfrdInPercent',
              euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
              specifiedFormatter
            ),
          },
        ],
      },
      {
        type: 'cell',
        label: euTaxonomyKpiNameMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        name: 'taxonomyEligibleNonLifeInsuranceActivities',
        explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForDataPoint(
            dataset.insuranceKpis?.taxonomyEligibleNonLifeInsuranceActivitiesInPercent
          ),
        valueGetter: getSpecifiedDataPointGetterFactory(
          'insuranceKpis.taxonomyEligibleNonLifeInsuranceActivitiesInPercent',
          euTaxonomyKpiNameMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
          specifiedFormatter
        ),
      },
    ],
  },
  {
    type: 'section',
    label: 'Asset Management',
    name: 'AssetManagement',
    labelBadgeColor: 'green',
    expandOnPageLoad: true,
    shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
      dataset.financialServicesTypes?.includes(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement) ??
      false,
    children: [
      {
        type: 'section',
        label: 'Eligibility KPIs',
        expandOnPageLoad: true,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
            name: 'taxonomyEligibleActivityInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleActivityInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(
                dataset.eligibilityKpis?.AssetManagement?.taxonomyEligibleActivityInPercent
              ),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.AssetManagement.taxonomyEligibleActivityInPercent',
              euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
            name: 'taxonomyNonEligibleActivityInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.taxonomyNonEligibleActivityInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(
                dataset.eligibilityKpis?.AssetManagement?.taxonomyNonEligibleActivityInPercent
              ),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.AssetManagement.taxonomyNonEligibleActivityInPercent',
              euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.derivativesInPercent,
            name: 'derivativesInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.derivativesInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(dataset.eligibilityKpis?.AssetManagement?.derivativesInPercent),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.AssetManagement.derivativesInPercent',
              euTaxonomyKpiNameMappings.derivativesInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
            name: 'banksAndIssuersInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.banksAndIssuersInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(dataset.eligibilityKpis?.AssetManagement?.banksAndIssuersInPercent),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.AssetManagement.banksAndIssuersInPercent',
              euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
            name: 'investmentNonNfrdInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.investmentNonNfrdInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(dataset.eligibilityKpis?.AssetManagement?.investmentNonNfrdInPercent),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.AssetManagement.investmentNonNfrdInPercent',
              euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
              specifiedFormatter
            ),
          },
        ],
      },
    ],
  },
  {
    type: 'section',
    label: 'Investment Firm',
    name: 'InvestmentFirm',
    labelBadgeColor: 'blue',
    expandOnPageLoad: true,
    shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
      dataset.financialServicesTypes?.includes(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm) ??
      false,
    children: [
      {
        type: 'section',
        label: 'Eligibility KPIs',
        expandOnPageLoad: true,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
            name: 'taxonomyEligibleActivityInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.taxonomyEligibleActivityInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(
                dataset.eligibilityKpis?.InvestmentFirm?.taxonomyEligibleActivityInPercent
              ),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.InvestmentFirm.taxonomyEligibleActivityInPercent',
              euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
            name: 'taxonomyNonEligibleActivityInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.taxonomyNonEligibleActivityInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(
                dataset.eligibilityKpis?.InvestmentFirm?.taxonomyNonEligibleActivityInPercent
              ),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.InvestmentFirm.taxonomyNonEligibleActivityInPercent',
              euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.derivativesInPercent,
            name: 'derivativesInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.derivativesInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(dataset.eligibilityKpis?.InvestmentFirm?.derivativesInPercent),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.InvestmentFirm.derivativesInPercent',
              euTaxonomyKpiNameMappings.derivativesInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
            name: 'banksAndIssuersInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.banksAndIssuersInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(dataset.eligibilityKpis?.InvestmentFirm?.banksAndIssuersInPercent),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.InvestmentFirm.banksAndIssuersInPercent',
              euTaxonomyKpiNameMappings.banksAndIssuersInPercent,
              specifiedFormatter
            ),
          },
          {
            type: 'cell',
            label: euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
            name: 'investmentNonNfrdInPercent',
            class: 'indentation',
            explanation: euTaxonomyKpiInfoMappings.investmentNonNfrdInPercent,
            shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
              shouldValueBeDisplayedForDataPoint(dataset.eligibilityKpis?.InvestmentFirm?.investmentNonNfrdInPercent),
            valueGetter: getSpecifiedDataPointGetterFactory(
              'eligibilityKpis.InvestmentFirm.investmentNonNfrdInPercent',
              euTaxonomyKpiNameMappings.investmentNonNfrdInPercent,
              specifiedFormatter
            ),
          },
        ],
      },
      {
        type: 'cell',
        label: euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
        name: 'greenAssetRatioInvestmentFirm',
        explanation: euTaxonomyKpiInfoMappings.greenAssetRatioInPercent,
        shouldDisplay: (dataset: EuTaxonomyDataForFinancials): boolean =>
          shouldValueBeDisplayedForDataPoint(dataset.investmentFirmKpis?.greenAssetRatioInPercent),
        valueGetter: getSpecifiedDataPointGetterFactory(
          'investmentFirmKpis.greenAssetRatioInPercent',
          euTaxonomyKpiNameMappings.greenAssetRatioInPercent,
          specifiedFormatter
        ),
      },
    ],
  },
];
