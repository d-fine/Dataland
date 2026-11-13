import { QaReportDataPointVerdict, AcceptedDataPointSource } from '@clients/qaservice';
import { admin_userId, reviewer_userId } from '@e2e/utils/Cypress.ts';
import { DATA_POINT_TYPES, type QaScenarioConfig } from '@e2e/utils/CheckJudgementJson.ts';
import { QualityOptions, type EutaxonomyFinancialsData } from '@clients/backend';
import { getFieldValueFromFrameworkDataset } from '@/components/resources/dataTable/conversion/Utils';

export const QA_SCENARIO_CONFIG: QaScenarioConfig[] = [
  {
    dataPointType: DATA_POINT_TYPES.fiscalYearEnd,
    qaReports: [
      { role: 'reviewer', verdict: QaReportDataPointVerdict.QaAccepted },
      { role: 'admin', verdict: QaReportDataPointVerdict.QaAccepted },
    ],
    judgement: {
      acceptedSource: AcceptedDataPointSource.Original,
    },
  },
  {
    dataPointType: DATA_POINT_TYPES.greenAssetRatioTotal,
    qaReports: [
      {
        role: 'reviewer',
        verdict: QaReportDataPointVerdict.QaRejected,
        correctedValue: JSON.stringify({ value: '5453445343', currency: 'EUR', quality: QualityOptions.Reported }),
      },
      {
        role: 'admin',
        verdict: QaReportDataPointVerdict.QaRejected,
        correctedValue: JSON.stringify({ value: '74568964325', currency: 'EUR', quality: QualityOptions.Reported }),
      },
    ],
    judgement: {
      acceptedSource: AcceptedDataPointSource.Custom,
      customValue: '400400400.23',
      customCurrency: 'EUR',
      reviewerComment: 'Both the original and QA suggestion values were inconsistent with other data points.',
    },
  },
  {
    dataPointType: DATA_POINT_TYPES.isNfrdMandatory,
    qaReports: [
      {
        role: 'reviewer',
        verdict: QaReportDataPointVerdict.QaRejected,
        correctedValue: JSON.stringify({ value: 'No', quality: QualityOptions.Reported }),
      },
      { role: 'admin', verdict: QaReportDataPointVerdict.QaAccepted },
    ],
    judgement: {
      acceptedSource: AcceptedDataPointSource.Qa,
      reporterUserIdOfAcceptedQaReport: reviewer_userId,
      reporterUserNameOfAcceptedQaReport: 'Data Reviewer',
    },
  },
  {
    dataPointType: DATA_POINT_TYPES.numberOfEmployees,
    qaReports: [
      { role: 'reviewer', verdict: QaReportDataPointVerdict.QaAccepted },
      {
        role: 'admin',
        verdict: QaReportDataPointVerdict.QaRejected,
        correctedValue: JSON.stringify({ value: '2409600.75', quality: QualityOptions.Reported }),
      },
    ],
    judgement: {
      acceptedSource: AcceptedDataPointSource.Qa,
      reporterUserIdOfAcceptedQaReport: admin_userId,
      reporterUserNameOfAcceptedQaReport: 'Data Admin',
    },
  },
];

/**
 * A map of EU taxonomy financials data point type IDs to their dot-notation field paths.
 */
export const DATA_POINT_PATH_MAP = {
  extendedDateFiscalYearEnd: 'general.general.fiscalYearEnd',
  extendedEnumYesNoIsNfrdMandatory: 'general.general.isNfrdMandatory',
  extendedDecimalNumberOfEmployees: 'general.general.numberOfEmployees',
  extendedEnumYesNoAreAllGroupEntitiesCoveredByEuTaxonomyReports: 'general.general.areAllGroupEntitiesCovered',
  extendedEnumFiscalYearDeviation: 'general.general.fiscalYearDeviation',
  customEnumEuTaxonomyReportingAssurance: 'general.general.assurance',

  extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalGrossCarryingAmount:
    'creditInstitution.assetsForCalculationOfGreenAssetRatio.totalGrossCarryingAmount',

  extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfAssetsTowardsTaxonomyRelevantSectorsTaxonomyEligible:
    'creditInstitution.assetsForCalculationOfGreenAssetRatio.totalAmountOfAssetsTowardsTaxonomyRelevantSectorsTaxonomyEligible',

  extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfAssetsWhichAreEnvironmentallySustainableTaxonomyAligned:
    'creditInstitution.assetsForCalculationOfGreenAssetRatio.totalAmountOfAssetsWhichAreEnvironmentallySustainableTaxonomyAligned',

  extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfEnvironmentallySustainableAssetsWhichAreUseOfProceeds:
    'creditInstitution.assetsForCalculationOfGreenAssetRatio.totalAmountOfEnvironmentallySustainableAssetsWhichAreUseOfProceeds',

  extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfEnvironmentallySustainableAssetsWhichAreTransitional:
    'creditInstitution.assetsForCalculationOfGreenAssetRatio.totalAmountOfEnvironmentallySustainableAssetsWhichAreTransitional',

  extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfEnvironmentallySustainableAssetsWhichAreEnabling:
    'creditInstitution.assetsForCalculationOfGreenAssetRatio.totalAmountOfEnvironmentallySustainableAssetsWhichAreEnabling',
} as const;

/**
 * Extracts the scalar value for a given data point type from an EU taxonomy financials dataset.
 */
export function extractValueForType(dataPointType: string, data: EutaxonomyFinancialsData): string {
  const path = (DATA_POINT_PATH_MAP as Record<string, string>)[dataPointType];
  if (!path) return '';
  return String(getFieldValueFromFrameworkDataset(`${path}.value`, data) ?? '');
}
