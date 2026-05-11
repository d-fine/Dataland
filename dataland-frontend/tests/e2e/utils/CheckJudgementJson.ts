import { QaReportDataPointVerdict, AcceptedDataPointSource } from '@clients/qaservice';
import { admin_userId, reviewer_userId } from '@e2e/utils/Cypress.ts';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { type EutaxonomyFinancialsData } from '@clients/backend';
import { getFieldValueFromFrameworkDataset } from '@/components/resources/dataTable/conversion/Utils';

export const DATA_POINT_TYPES = {
  fiscalYearEnd: 'extendedDateFiscalYearEnd',
  greenAssetRatioTotal:
    'extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalGrossCarryingAmount',
  isNfrdMandatory: 'extendedEnumYesNoIsNfrdMandatory',
  numberOfEmployees: 'extendedDecimalNumberOfEmployees',
} as const;

export type DataPointTypeKey = keyof typeof DATA_POINT_TYPES;
export type DataPointType = (typeof DATA_POINT_TYPES)[DataPointTypeKey];

export type QaRole = 'reviewer' | 'admin';

export interface QaReport {
  role: QaRole;
  verdict: QaReportDataPointVerdict;
  correctedValue?: string;
}

export interface QaJudgement {
  acceptedSource?: AcceptedDataPointSource;
  reporterUserIdOfAcceptedQaReport?: string;
  reporterUserNameOfAcceptedQaReport?: string;
  customValue?: string;
}

export interface QaScenarioConfig {
  dataPointType: DataPointType;
  qaReports: QaReport[];
  judgement: QaJudgement;
}

export interface DataPointOverview {
  dataPointsWithQaReports: Record<string, string>;
  dataPointsWithoutQaReports: Record<string, string>;
  amountOfDataPointsToReview: number;
}

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
 *
 * Behavior:
 * - never mutates the input fixture (deep clone via JSON serialization)
 * - removes `t.general.general.assurance` if present
 * - removes `t.general.assurance` if present
 * - if the fixture shape differs unexpectedly, it fails gracefully and returns the clone unchanged
 *
 * @param fixture Source fixture to sanitize before upload/use in tests.
 * @returns A sanitized deep clone of the fixture without assurance fields (when found).
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
 * Safely parses a JSON string and extracts the 'value' property if it exists.
 * If the value is an object, it is stringified via JSON to avoid [object Object].
 *
 * @param raw The raw JSON string to be parsed.
 * @returns The extracted value as a string, or the original string if parsing fails.
 */
export function parseJsonValue(raw?: string): string | undefined {
  if (raw == null) return undefined;

  try {
    const parsed: unknown = JSON.parse(raw);

    if (typeof parsed !== 'object' || parsed === null || !('value' in parsed)) {
      return raw;
    }

    const val = (parsed as { value?: unknown }).value;

    if (val == null) {
      return raw;
    }

    if (typeof val === 'string') {
      return val;
    }

    return JSON.stringify(val);
  } catch {
    return raw;
  }
}

/**
 * Extracts the scalar value for a given data point type from an EU taxonomy financials dataset.
 *
 * @param dataPointType Data point type key used to look up the field path in the path map.
 * @param data          EU taxonomy financials dataset to extract the value from.
 * @returns             The extracted value as a string, or an empty string if the path is not mapped.
 */
export function extractValueForType(dataPointType: string, data: EutaxonomyFinancialsData): string {
  const path = (DATA_POINT_PATH_MAP as Record<string, string>)[dataPointType];
  if (!path) return '';
  return String(getFieldValueFromFrameworkDataset(`${path}.value`, data) ?? '');
}
