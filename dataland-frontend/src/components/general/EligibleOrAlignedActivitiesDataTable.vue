<script lang="ts">
import { defineComponent } from 'vue';
import BaseActivitiesDataTable from '@/components/general/BaseActivitiesDataTable.vue';
import { formatPercentageNumberAsString } from '@/utils/Formatter';

type Self = InstanceType<typeof BaseActivitiesDataTable>;

type EligibleOrAlignedActivityRow = Record<string, unknown> & {
  activityName?: string;
  substantialContributionToClimateChangeMitigationInPercent?: number;
  substantialContributionToClimateChangeAdaptationInPercent?: number;
  substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent?: number;
  substantialContributionToTransitionToACircularEconomyInPercent?: number;
  substantialContributionToPollutionPreventionAndControlInPercent?: number;
  substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent?: number;
};

const eligibleOrAlignedActivitiesDataTableConfiguration: Self['activitiesDataTableConfiguration'] = {
  createAdditionalMainColumnDefinitions(self: Self) {
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
  createAdditionalMainColumnGroups(self: Self) {
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
  getAdditionalGroupColspans(self: Self): { [groupName: string]: number } {
    return {
      substantialContributionCriteria: self.getEnvironmentalObjectivesLength(),
      _enablingActivity: 1,
      _transitionalActivity: 1,
    };
  },
  createMainColumnDataForRow(activity: Record<string, unknown>, self: Self) {
    const typedActivity = activity as EligibleOrAlignedActivityRow;
    return [
      ...self.createBaseMainColumnDataForRow(activity),
      ...self.createActivityGroupData<number | undefined>(
        typedActivity.activityName as string,
        'substantialContributionCriteria',
        {
          substantialContributionToClimateChangeMitigationInPercent:
            typedActivity.substantialContributionToClimateChangeMitigationInPercent,
          substantialContributionToClimateChangeAdaptationInPercent:
            typedActivity.substantialContributionToClimateChangeAdaptationInPercent,
          substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent:
            typedActivity.substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent,
          substantialContributionToTransitionToACircularEconomyInPercent:
            typedActivity.substantialContributionToTransitionToACircularEconomyInPercent,
          substantialContributionToPollutionPreventionAndControlInPercent:
            typedActivity.substantialContributionToPollutionPreventionAndControlInPercent,
          substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent:
            typedActivity.substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent,
        },
        formatPercentageNumberAsString
      ),
      ...self.createSingleFieldGroupData(activity, '_enablingActivity', 'enablingActivity'),
      ...self.createSingleFieldGroupData(activity, '_transitionalActivity', 'transitionalActivity'),
    ] as ReturnType<Self['createBaseMainColumnDataForRow']>;
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
