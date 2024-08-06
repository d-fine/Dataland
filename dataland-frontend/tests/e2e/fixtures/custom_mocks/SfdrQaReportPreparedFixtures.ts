import { generateFixtureDataset } from '@e2e/fixtures/FixtureUtils';
import { type FixtureData } from '@sharedUtils/Fixtures';
import {
  ExtendedDataPointYesNoNoEvidenceFoundQualityEnum,
  ExtendedDataPointYesNoNoEvidenceFoundValueEnum,
  QaReportDataPointVerdict,
  type SfdrData as SfdrQaReport,
} from '@clients/qaservice';

/**
 * TODO
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
 * TODO
 * @param input
 */
function manipulateFixtureForSfdrDatasetWithOneCorrection(input: FixtureData<SfdrQaReport>): FixtureData<SfdrQaReport> {
  input.companyInformation.companyName = 'sfdr-qa-report-with-one-correction';
  return input;
}

/**
 * TODO
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
