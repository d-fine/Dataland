import type { ActivityField } from '@/utils/ActivityListHelpers.ts';

const environmentalObjectives: [string, string][] = [
  ['ClimateChangeMitigation', 'Climate change mitigation'],
  ['ClimateChangeAdaptation', 'Climate change adaptation'],
  ['SustainableUseAndProtectionOfWaterAndMarineResources', 'Water and marine resources'],
  ['TransitionToACircularEconomy', 'Circular economy'],
  ['PollutionPreventionAndControl', 'Pollution prevention'],
  ['ProtectionAndRestorationOfBiodiversityAndEcosystems', 'Biodiversity and ecosystems'],
];

export const substantialContributionPercentFields: ActivityField[] = environmentalObjectives.map(([key, label]) => ({
  path: `substantialContributionTo${key}InPercent`,
  header: `Substantial contribution: ${label} (%)`,
}));

export const dnshYesNoFields: ActivityField[] = environmentalObjectives.map(([key, label]) => ({
  path: `dnshTo${key}`,
  header: `DNSH: ${label}`,
}));
