<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import { type YesNo } from '@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model';
import BaseActivitiesDataTable from '@/components/general/BaseActivitiesDataTable.vue';
import { formatPercentageNumberAsString } from '@/utils/Formatter';

const alignedActivitiesDataTableConfiguration = {
  createAdditionalMainColumnDefinitions(context): Array<Record<string, unknown>> {
    return [
      ...context.makeGroupColumns('substantialContributionCriteria', 'substantialContribution'),
      ...context.makeGroupColumns('dnshCriteria', 'dnsh', false),
      {
        field: 'minimumSafeguards',
        header: context.humanizeHeaderName('minimumSafeguards'),
        group: '_minimumSafeguards',
        groupIndex: 0,
      },
      {
        field: 'enablingActivity',
        header: context.humanizeHeaderName('enablingActivity'),
        group: '_enablingActivity',
        groupIndex: 0,
      },
      {
        field: 'transitionalActivity',
        header: context.humanizeHeaderName('transitionalActivity'),
        group: '_transitionalActivity',
        groupIndex: 0,
      },
    ];
  },
  createAdditionalMainColumnGroups(context): Array<Record<string, unknown>> {
    return [
      {
        key: 'substantialContributionCriteria',
        label: context.humanizeHeaderName('substantialContributionCriteria'),
        colspan: context.findMaxColspanForGroup('substantialContributionCriteria'),
      },
      {
        key: 'dnshCriteria',
        label: context.humanizeHeaderName('dnshCriteria'),
        colspan: context.findMaxColspanForGroup('dnshCriteria'),
      },
      { key: '_minimumSafeguards', label: '', colspan: context.findMaxColspanForGroup('_minimumSafeguards') },
      { key: '_enablingActivity', label: '', colspan: context.findMaxColspanForGroup('_enablingActivity') },
      { key: '_transitionalActivity', label: '', colspan: context.findMaxColspanForGroup('_transitionalActivity') },
    ];
  },
  getAdditionalGroupColspans(context): { [groupName: string]: number } {
    const environmentalObjectivesLength = context.getEnvironmentalObjectivesLength();
    return {
      substantialContributionCriteria: environmentalObjectivesLength,
      dnshCriteria: environmentalObjectivesLength,
      _minimumSafeguards: 1,
      _enablingActivity: 1,
      _transitionalActivity: 1,
    };
  },
  createMainColumnDataForRow(activity: Record<string, unknown>, context): Array<Record<string, unknown>> {
    return [
      ...context.createBaseMainColumnDataForRow(activity),
      ...context.createActivityGroupData<number>(
        activity.activityName as string,
        'substantialContributionCriteria',
        {
          substantialContributionToClimateChangeMitigationInPercent:
            activity.substantialContributionToClimateChangeMitigationInPercent,
          substantialContributionToClimateChangeAdaptationInPercent:
            activity.substantialContributionToClimateChangeAdaptationInPercent,
          substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent:
            activity.substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent,
          substantialContributionToTransitionToACircularEconomyInPercent:
            activity.substantialContributionToTransitionToACircularEconomyInPercent,
          substantialContributionToPollutionPreventionAndControlInPercent:
            activity.substantialContributionToPollutionPreventionAndControlInPercent,
          substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent:
            activity.substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent,
        },
        formatPercentageNumberAsString
      ),
      ...context.createActivityGroupData<YesNo>(
        activity.activityName as string,
        'dnshCriteria',
        {
          dnshToClimateChangeMitigation: activity.dnshToClimateChangeMitigation,
          dnshToClimateChangeAdaptation: activity.dnshToClimateChangeAdaptation,
          dnshToSustainableUseAndProtectionOfWaterAndMarineResources:
            activity.dnshToSustainableUseAndProtectionOfWaterAndMarineResources,
          dnshToTransitionToACircularEconomy: activity.dnshToTransitionToACircularEconomy,
          dnshToPollutionPreventionAndControl: activity.dnshToPollutionPreventionAndControl,
          dnshToProtectionAndRestorationOfBiodiversityAndEcosystems:
            activity.dnshToProtectionAndRestorationOfBiodiversityAndEcosystems,
        },
        (value: YesNo) => (value ? `${value}` : '')
      ),
      ...context.createSingleFieldGroupData(activity, '_minimumSafeguards', 'minimumSafeguards'),
      ...context.createSingleFieldGroupData(activity, '_enablingActivity', 'enablingActivity'),
      ...context.createSingleFieldGroupData(activity, '_transitionalActivity', 'transitionalActivity'),
    ];
  },
};

export default defineComponent({
  name: 'AlignedActivitiesDataTable',
  extends: BaseActivitiesDataTable,
  created() {
    this.activitiesDataTableConfiguration = alignedActivitiesDataTableConfiguration;
  },
});
</script>
