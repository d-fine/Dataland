import type { ActivityField, ActivityFieldGroup } from '@/utils/ActivityListHelpers.ts';

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
  header: label,
  type: 'percent',
}));

export const dnshYesNoFields: ActivityField[] = environmentalObjectives.map(([key, label]) => ({
  path: `dnshTo${key}`,
  header: label,
  type: 'yesno',
}));

export const substantialContributionGroup: ActivityFieldGroup = {
  key: 'substantialContributionCriteria',
  label: 'Substantial Contribution Criteria',
  fields: substantialContributionPercentFields,
};

export const dnshGroup: ActivityFieldGroup = {
  key: 'dnshCriteria',
  label: 'DNSH Criteria',
  fields: dnshYesNoFields,
};

export const enablingAndTransitionalGroups: ActivityFieldGroup[] = [
  {
    key: 'enablingActivity',
    label: '',
    fields: [{ path: 'enablingActivity', header: 'Enabling Activity', type: 'yesno' }],
  },
  {
    key: 'transitionalActivity',
    label: '',
    fields: [{ path: 'transitionalActivity', header: 'Transitional Activity', type: 'yesno' }],
  },
];
