import { generateFixtureDataset, pickOneElement, removeAllUnusedReferencedReports } from '@e2e/fixtures/FixtureUtils';
import { type FixtureData } from '@sharedUtils/Fixtures';
import {
  type SfdrData as SfdrQaReport,
  ExtendedDataPointBigDecimalQualityEnum,
  ExtendedDataPointYesNoQualityEnum,
  ExtendedDataPointYesNoValueEnum,
  QaReportDataPointVerdict,
} from '@clients/qaservice';
import type { SfdrData } from '@clients/backend';
import { SfdrGenerator } from '@e2e/fixtures/frameworks/sfdr/SfdrGenerator.ts';
import { SfdrGeneralGeneralFiscalYearDeviationOptions } from '@clients/backend';

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
 * @returns the modified fixture dataset
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
            value: ExtendedDataPointYesNoValueEnum.Yes,
            quality: ExtendedDataPointYesNoQualityEnum.Estimated,
          },
        },
      },
    },
  };
}

/**
 * Generates a pair of qa report and data for the Sfdr with various verdicts.
 */
export function generateSfdrLinkedQaReports(): {
  data: SfdrData;
  qaReport: SfdrQaReport;
} {
  const dataGenerator = new SfdrGenerator(0);
  const data: SfdrData = {
    general: {
      general: {
        dataDate: dataGenerator.guaranteedFutureDate(),
        fiscalYearDeviation: pickOneElement(Object.values(SfdrGeneralGeneralFiscalYearDeviationOptions)),
        fiscalYearEnd: dataGenerator.guaranteedFutureDate(),
        referencedReports: dataGenerator.reports,
      },
    },
    social: {
      humanRights: {
        numberOfReportedIncidentsOfHumanRightsViolations: dataGenerator.randomExtendedDataPoint(
          dataGenerator.randomInt(0)
        ),
      },
    },
    environmental: {
      biodiversity: {
        primaryForestAndWoodedLandOfNativeSpeciesExposure: dataGenerator.randomExtendedDataPoint(
          dataGenerator.randomYesNo()
        ),
        biodiversityProtectionPolicy: dataGenerator.randomExtendedDataPoint(dataGenerator.randomYesNo()),
      },
    },
  };

  const qaReport: SfdrQaReport = {
    social: {
      humanRights: {
        numberOfReportedIncidentsOfHumanRightsViolations: {
          comment: 'data is wrong',
          verdict: QaReportDataPointVerdict.QaRejected,
          correctedData: {
            value: 5,
            quality: ExtendedDataPointBigDecimalQualityEnum.Estimated,
          },
        },
      },
    },
    environmental: {
      biodiversity: {
        primaryForestAndWoodedLandOfNativeSpeciesExposure: {
          comment: 'some comment',
          verdict: QaReportDataPointVerdict.QaInconclusive,
          correctedData: {
            value: ExtendedDataPointYesNoValueEnum.Yes,
            quality: ExtendedDataPointYesNoQualityEnum.Estimated,
          },
        },
        biodiversityProtectionPolicy: {
          comment: 'data is correct',
          verdict: QaReportDataPointVerdict.QaAccepted,
          correctedData: {},
        },
      },
    },
  };
  removeAllUnusedReferencedReports(data as unknown as Record<string, unknown>, dataGenerator.reports);
  return { data, qaReport };
}
