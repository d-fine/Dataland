<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import BaseActivitiesDataTable from '@/components/general/BaseActivitiesDataTable.vue';
import { formatPercentageNumberAsString } from '@/utils/Formatter';

const eligibleOrAlignedActivitiesDataTableConfiguration = {
  createAdditionalMainColumnDefinitions(self: any): Array<Record<string, unknown>> {
    return [
      ...self.makeGroupColumns('substantialContributionCriteria', 'substantialContribution'),
      {
        field: 'enablingActivity',
        header: self.humanizeHeaderName('enablingActivity'),
        group: '_enablingActivity',
        groupIndex: 0,
      },
      {
        field: 'transitionalActivity',
        header: self.humanizeHeaderName('transitionalActivity'),
        group: '_transitionalActivity',
        groupIndex: 0,
      },
    ];
  },
  createAdditionalMainColumnGroups(self: any): Array<Record<string, unknown>> {
    return [
      {
        key: 'substantialContributionCriteria',
        label: self.humanizeHeaderName('substantialContributionCriteria'),
        colspan: self.findMaxColspanForGroup('substantialContributionCriteria'),
      },
      { key: '_enablingActivity', label: '', colspan: self.findMaxColspanForGroup('_enablingActivity') },
      { key: '_transitionalActivity', label: '', colspan: self.findMaxColspanForGroup('_transitionalActivity') },
    ];
  },
  getAdditionalGroupColspans(self: any): { [groupName: string]: number } {
    return {
      substantialContributionCriteria: self.getEnvironmentalObjectivesLength(),
      _enablingActivity: 1,
      _transitionalActivity: 1,
    };
  },
  createMainColumnDataForRow(activity: Record<string, unknown>, self: any): Array<Record<string, unknown>> {
    return [
      ...self.createBaseMainColumnDataForRow(activity),
      ...self.createActivityGroupData<number>(
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
      ...self.createSingleFieldGroupData(activity, '_enablingActivity', 'enablingActivity'),
      ...self.createSingleFieldGroupData(activity, '_transitionalActivity', 'transitionalActivity'),
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
