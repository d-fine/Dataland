import { generateFixtureDataset } from '@e2e/fixtures/FixtureUtils';
import { type FixtureData } from '@sharedUtils/Fixtures';
import {
  type EutaxonomyNonFinancialsData as EuTaxonomyNonFinancialsQaReport,
  ExtendedDataPointBigDecimalQualityEnum,
  QaReportDataPointVerdict,
} from '@clients/qaservice';

/**
 * Generates eu taxonomy non-financials qa report prepared fixtures by generating random
 * eutaxonomy-non-financials-qa-reports and afterwards manipulating some fields via manipulator-functions to set
 * specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEuTaxonomyNonFinancialsQaReportPreparedFixtures(): Array<
  FixtureData<EuTaxonomyNonFinancialsQaReport>
> {
  const preparedFixtures = [];

  preparedFixtures.push(
    manipulateFixtureForEuTaxonomyNonFinancialsDatasetWithOneCorrection(
      generateFixtureDataset<EuTaxonomyNonFinancialsQaReport>(
        generateEuTaxonomyNonFinancialsQaReportWithCorrectionForRelativeShareInPercent,
        1
      )[0]
    )
  );
  return preparedFixtures;
}

/**
 * Modifies the passed fixture to contain a specific company name to identify it amongst all fixtures.
 * @param input generated prepared fixture to modify
 * @returns the modified fixture dataset
 */
function manipulateFixtureForEuTaxonomyNonFinancialsDatasetWithOneCorrection(
  input: FixtureData<EuTaxonomyNonFinancialsQaReport>
): FixtureData<EuTaxonomyNonFinancialsQaReport> {
  input.companyInformation.companyName = 'eutaxonomy-non-financials-qa-report-with-one-correction';
  return input;
}

/**
 * Generates a qa-report for eu taxonomy non-financials with a correction for one specific field.
 * @returns the generated eu taxonomy non-financials qa report
 */
function generateEuTaxonomyNonFinancialsQaReportWithCorrectionForRelativeShareInPercent(): EuTaxonomyNonFinancialsQaReport {
  return {
    revenue: {
      nonEligibleShare: {
        relativeShareInPercent: {
          comment: 'some comment',
          verdict: QaReportDataPointVerdict.QaInconclusive,
          correctedData: {
            value: 15.3472,
            quality: ExtendedDataPointBigDecimalQualityEnum.Audited,
          },
        },
      },
    },
  };
}
