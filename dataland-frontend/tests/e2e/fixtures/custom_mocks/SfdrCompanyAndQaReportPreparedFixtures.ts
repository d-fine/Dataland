import {
  generateArray,
  generateFixtureDataset,
  pickOneElement,
  removeAllUnusedReferencedReports
} from '@e2e/fixtures/FixtureUtils';
import { type FixtureData } from '@sharedUtils/Fixtures';
import {
  ExtendedDataPointYesNoQualityEnum,
  ExtendedDataPointYesNoValueEnum,
  QaReportDataPointVerdict, QaReportDataPointString,
} from '@clients/qaservice';
import type {CompanyInformation, SfdrData} from '@clients/backend';
import { SfdrGenerator } from '@e2e/fixtures/frameworks/sfdr/SfdrGenerator.ts';
import { generateCompanyInformation } from "@e2e/fixtures/CompanyFixtures.ts";
import { QaReportFixtureData } from "@sharedUtils/QaReportFixtures.ts";




/**
 * Generates a pair of qa report and data for the Sfdr with various verdicts.
 */
export function generateSFDRCompanyAndQaReports(): Array<QaReportFixtureData<SfdrData>>
 {
  const dataGenerator = new SfdrGenerator(0);
  const companyInformation: CompanyInformation = generateCompanyInformation();
  const reportingPeriod = "2024";
  const t: SfdrData = {
    general: {
      general: {
        dataDate: "2025-03-01",
        fiscalYearDeviation: { value: "NoDeviation" },
        fiscalYearEnd: { value: "2024-12-31" },
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

  const qaReports: { [key: string]: QaReportDataPointString } =
    {
      extendedIntegerNumberOfReportedIncidentsOfHumanRightsViolations: {
        comment: 'data is wrong',
        verdict: QaReportDataPointVerdict.QaRejected,
        correctedData: "{ \"value\": 5, \"quality\": \"Estimated\"}"
      },
      extendedEnumYesNoPrimaryForestAndWoodedLandOfNativeSpeciesExposure: {
        comment: 'some comment',
        verdict: QaReportDataPointVerdict.QaInconclusive,
        correctedData: "{ \"value\": \"Yes\", \"quality\": \"Estimated\"}"
      },
      extendedEnumYesNoBiodiversityProtectionPolicy: {
       comment: 'data is correct',
       verdict: QaReportDataPointVerdict.QaInconclusive,
        // A schema to automate the creation of corrected data points.
       correctedData: "{ \"value\": \"" + ExtendedDataPointYesNoValueEnum.Yes.toString()
         + "\", \"quality\": \"" + ExtendedDataPointYesNoQualityEnum.Reported.toString() + "\"}"
     }
   };
  removeAllUnusedReferencedReports(t as unknown as Record<string, unknown>, dataGenerator.reports);
  const preparedFixture: FixtureData<SfdrData> = { companyInformation, t, reportingPeriod };
  const preparedQaReportFixture: QaReportFixtureData<SfdrData> = { preparedFixture, qaReports };
  return [preparedQaReportFixture];
}
