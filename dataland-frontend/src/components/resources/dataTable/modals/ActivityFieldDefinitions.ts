import type { ActivityField, ActivityFieldGroup } from '@/utils/ActivityListHelpers.ts';
import { euTaxonomyNonFinancialsModalColumnHeaders } from '@/components/resources/dataTable/conversion/EutaxonomyNonAlignedActivitiesValueGetterFactory.ts';

const viewHeaders: Record<string, string> = euTaxonomyNonFinancialsModalColumnHeaders.alignedActivities;

const environmentalObjectives = [
  'ClimateChangeMitigation',
  'ClimateChangeAdaptation',
  'SustainableUseAndProtectionOfWaterAndMarineResources',
  'TransitionToACircularEconomy',
  'PollutionPreventionAndControl',
  'ProtectionAndRestorationOfBiodiversityAndEcosystems',
];

/**
 * @param path the field path of the activity
 * @returns the header that the view modal displays for this field
 */
function viewHeader(path: string): string {
  return viewHeaders[path] ?? path;
}

export const substantialContributionPercentFields: ActivityField[] = environmentalObjectives.map((key) => {
  const path = `substantialContributionTo${key}InPercent`;
  return { path, header: viewHeader(path), type: 'percent' };
});

export const dnshYesNoFields: ActivityField[] = environmentalObjectives.map((key) => {
  const path = `dnshTo${key}`;
  return { path, header: viewHeader(path), type: 'yesno' };
});

/**
 * @param amountHeader header of the absolute amount column
 * @param percentHeader header of the relative share column
 * @param additionalFields further fields shown within the KPI group
 * @returns the KPI group with the amount and the share column
 */
function kpiGroup(
  amountHeader: string,
  percentHeader: string,
  additionalFields: ActivityField[] = []
): ActivityFieldGroup {
  return {
    key: '_kpi',
    label: '',
    fields: [
      { path: 'share.absoluteShare', header: amountHeader, type: 'amount' },
      { path: 'share.relativeShareInPercent', header: percentHeader, type: 'percent' },
      ...additionalFields,
    ],
  };
}

export const kpiGroupForAlignedOrNonAligned: ActivityFieldGroup = kpiGroup('{kpi}', '{kpi} (%)');

export const kpiGroupForEligibleOrAligned: ActivityFieldGroup = kpiGroup('Aligned {kpi}', 'Aligned {kpi} (%)', [
  { path: 'relativeEligibleShareInPercent', header: 'Eligible {kpi} (%)', type: 'percent' },
]);

export const substantialContributionGroup: ActivityFieldGroup = {
  key: 'substantialContributionCriteria',
  label: viewHeader('substantialContributionCriteria'),
  fields: substantialContributionPercentFields,
};

export const dnshGroup: ActivityFieldGroup = {
  key: 'dnshCriteria',
  label: viewHeader('dnshCriteria'),
  fields: dnshYesNoFields,
};

/**
 * @param path the field path of the yes/no field
 * @returns a group without label consisting of a single yes/no column
 */
function singleYesNoGroup(path: string): ActivityFieldGroup {
  return { key: `_${path}`, label: '', fields: [{ path, header: viewHeader(path), type: 'yesno' }] };
}

export const minimumSafeguardsGroup: ActivityFieldGroup = singleYesNoGroup('minimumSafeguards');

export const enablingAndTransitionalGroups: ActivityFieldGroup[] = [
  singleYesNoGroup('enablingActivity'),
  singleYesNoGroup('transitionalActivity'),
];
