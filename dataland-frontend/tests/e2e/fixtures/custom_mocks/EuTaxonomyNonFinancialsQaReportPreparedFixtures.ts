import { generateFixtureDataset } from '@e2e/fixtures/FixtureUtils';
import { type FixtureData } from '@sharedUtils/Fixtures';
import {
  type EutaxonomyNonFinancialsData as EuTaxonomyNonFinancialsQaReport,
  ExtendedDataPointListEuTaxonomyAlignedActivityQualityEnum,
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
        generateEuTaxonomyNonFinancialsQaReportWithCorrectionForAlignedActivities,
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
function generateEuTaxonomyNonFinancialsQaReportWithCorrectionForAlignedActivities(): EuTaxonomyNonFinancialsQaReport {
  return {
    revenue: {
      alignedActivities: {
        comment: 'some comment',
        verdict: QaReportDataPointVerdict.QaInconclusive,
        correctedData: {
          value: [
            {
              activityName: 'ManufactureOfCement',
              naceCodes: ['C23.51'],
              share: {
                relativeShareInPercent: 15.34,
                absoluteShare: {
                  amount: 2633017767.22,
                  currency: 'SDG',
                },
              },
              substantialContributionToClimateChangeMitigationInPercent: 2.13,
              substantialContributionToClimateChangeAdaptationInPercent: 18.5,
              substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent: 72.75,
              substantialContributionToTransitionToACircularEconomyInPercent: 3.48,
              substantialContributionToPollutionPreventionAndControlInPercent: 60.5,
              substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent: 62.68,
              dnshToClimateChangeMitigation: 'No',
              dnshToClimateChangeAdaptation: 'Yes',
              dnshToSustainableUseAndProtectionOfWaterAndMarineResources: 'Yes',
              dnshToTransitionToACircularEconomy: 'No',
              dnshToPollutionPreventionAndControl: 'No',
              dnshToProtectionAndRestorationOfBiodiversityAndEcosystems: 'No',
              minimumSafeguards: 'No',
            },
            {
              activityName: 'InfrastructureEnablingRoadTransportAndPublicTransport',
              naceCodes: ['M71.20'],
              share: {
                relativeShareInPercent: 91.47,
                absoluteShare: {
                  amount: 1473627865.78,
                  currency: 'UYU',
                },
              },
              substantialContributionToClimateChangeMitigationInPercent: 82.36,
              substantialContributionToClimateChangeAdaptationInPercent: 55.11,
              substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent: 31.84,
              substantialContributionToTransitionToACircularEconomyInPercent: 76.04,
              substantialContributionToPollutionPreventionAndControlInPercent: 29.82,
              substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent: 46.25,
              dnshToClimateChangeMitigation: 'No',
              dnshToClimateChangeAdaptation: 'No',
              dnshToSustainableUseAndProtectionOfWaterAndMarineResources: 'No',
              dnshToTransitionToACircularEconomy: 'Yes',
              dnshToPollutionPreventionAndControl: 'No',
              dnshToProtectionAndRestorationOfBiodiversityAndEcosystems: 'No',
              minimumSafeguards: 'No',
            },
          ],
          quality: ExtendedDataPointListEuTaxonomyAlignedActivityQualityEnum.Audited,
        },
      },
    },
  };
}
