<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import BaseActivitiesDataTable from '@/components/general/BaseActivitiesDataTable.vue';
import { formatPercentageNumberAsString } from '@/utils/Formatter';

const eligibleOrAlignedActivitiesDataTableConfiguration = {
  createAdditionalMainColumnDefinitions(context): Array<Record<string, unknown>> {
    return [
      ...context.makeGroupColumns('substantialContributionCriteria', 'substantialContribution'),
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
      { key: '_enablingActivity', label: '', colspan: context.findMaxColspanForGroup('_enablingActivity') },
      { key: '_transitionalActivity', label: '', colspan: context.findMaxColspanForGroup('_transitionalActivity') },
    ];
  },
  getAdditionalGroupColspans(context): { [groupName: string]: number } {
    return {
      substantialContributionCriteria: context.getEnvironmentalObjectivesLength(),
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
      ...context.createSingleFieldGroupData(activity, '_enablingActivity', 'enablingActivity'),
      ...context.createSingleFieldGroupData(activity, '_transitionalActivity', 'transitionalActivity'),
    ];
  },
};

export default defineComponent({
  name: 'EligibleOrAlignedActivitiesDataTable',
  extends: BaseActivitiesDataTable,
  created() {
    this.activitiesDataTableConfiguration = eligibleOrAlignedActivitiesDataTableConfiguration;
  },
});
</script>
