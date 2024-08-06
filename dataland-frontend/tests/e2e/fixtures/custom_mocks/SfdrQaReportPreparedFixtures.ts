import { generateFixtureDataset } from '@e2e/fixtures/FixtureUtils';
import { type FixtureData } from '@sharedUtils/Fixtures';
import {
  ExtendedDataPointYesNoNoEvidenceFoundQualityEnum,
  ExtendedDataPointYesNoNoEvidenceFoundValueEnum,
  QaReportDataPointVerdict,
  type SfdrData as SfdrQaReport,
} from '@clients/qaservice';

/**
 * Generates sfdr qa report prepared fixtures by generating random sfdr-qa-reports and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateSfdrQaReportPreparedFixtures(): Array<FixtureData<SfdrQaReport>> {
  const preparedFixtures = [];

  preparedFixtures.push(
    manipulateFixtureForSfdrDatasetWithOneCorrection(
      generateFixtureDataset<SfdrQaReport>(generateSfdrQaReportWithCorrectionForPrimaryForestAndWoodedLand, 1)[0]
    )
  );
  return preparedFixtures;
}

/**
 * Modifies the passed fixture to contain a specific company name to identify it amongst all fixtures.
 * @param input generated prepared fixture to modify
 */
function manipulateFixtureForSfdrDatasetWithOneCorrection(input: FixtureData<SfdrQaReport>): FixtureData<SfdrQaReport> {
  input.companyInformation.companyName = 'sfdr-qa-report-with-one-correction';
  return input;
}

/**
 * Generates a qa-report for sfdr with a correction for one specific field.
 * @returns the generated sfdr qa report
 */
function generateSfdrQaReportWithCorrectionForPrimaryForestAndWoodedLand(): SfdrQaReport {
  return {
    environmental: {
      biodiversity: {
        primaryForestAndWoodedLandOfNativeSpeciesExposure: {
          comment: 'some comment',
          verdict: QaReportDataPointVerdict.QaInconclusive,
          correctedData: {
            value: ExtendedDataPointYesNoNoEvidenceFoundValueEnum.Yes,
            quality: ExtendedDataPointYesNoNoEvidenceFoundQualityEnum.Estimated,
          },
        },
      },
    },
  };
}
