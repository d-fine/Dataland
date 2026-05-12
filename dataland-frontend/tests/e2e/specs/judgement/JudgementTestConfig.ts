import { QaReportDataPointVerdict, AcceptedDataPointSource } from '@clients/qaservice';
import { admin_userId, reviewer_userId } from '@e2e/utils/Cypress.ts';
import { DATA_POINT_TYPES, type QaScenarioConfig } from '@e2e/utils/CheckJudgementJson.ts';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { type EutaxonomyFinancialsData } from '@clients/backend';
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
        correctedValue: '{"value":"5453445343", "currency":"EUR"}',
      },
      {
        role: 'admin',
        verdict: QaReportDataPointVerdict.QaRejected,
        correctedValue: '{"value":"74568964325", "currency":"EUR"}',
      },
    ],
    judgement: {
      acceptedSource: AcceptedDataPointSource.Custom,
      customValue: '400400400.23',
    },
  },
  {
    dataPointType: DATA_POINT_TYPES.isNfrdMandatory,
    qaReports: [
      { role: 'reviewer', verdict: QaReportDataPointVerdict.QaRejected, correctedValue: '{"value":"No"}' },
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
      { role: 'admin', verdict: QaReportDataPointVerdict.QaRejected, correctedValue: '{"value":"2409600.75"}' },
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
 * Returns a deep-cloned fixture with the EU taxonomy "assurance" data point removed.
 *
 * This helper is used for E2E stability: the assurance field is known to have a
 * non-standard structure that can interfere with the judge modal flow.
 */
export function stripAssuranceFromFixture(
  fixture: FixtureData<EutaxonomyFinancialsData>
): FixtureData<EutaxonomyFinancialsData> {
  const clone = structuredClone(fixture);

  try {
    const t = clone.t;

    if (t?.general?.general && Object.hasOwn(t.general.general, 'assurance')) {
      delete (t.general.general as Record<string, unknown>)['assurance'];
    }

    if (t?.general && Object.hasOwn(t.general, 'assurance')) {
      delete (t.general as Record<string, unknown>)['assurance'];
    }
  } catch {}

  return clone;
}

/**
 * Extracts the scalar value for a given data point type from an EU taxonomy financials dataset.
 */
export function extractValueForType(dataPointType: string, data: EutaxonomyFinancialsData): string {
  const path = (DATA_POINT_PATH_MAP as Record<string, string>)[dataPointType];
  if (!path) return '';
  return String(getFieldValueFromFrameworkDataset(`${path}.value`, data) ?? '');
}
