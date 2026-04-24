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
  createAdditionalMainColumnDefinitions(this: Self) {
    return [
      ...this.makeGroupColumns('substantialContributionCriteria', 'substantialContribution'),
      {
        field: 'enablingActivity',
        header: this.humanizeHeaderName('enablingActivity'),
        group: '_enablingActivity',
        groupIndex: 0,
      },
      {
        field: 'transitionalActivity',
        header: this.humanizeHeaderName('transitionalActivity'),
        group: '_transitionalActivity',
        groupIndex: 0,
      },
    ];
  },
  createAdditionalMainColumnGroups(this: Self) {
    return [
      {
        key: 'substantialContributionCriteria',
        label: this.humanizeHeaderName('substantialContributionCriteria'),
        colspan: this.findMaxColspanForGroup('substantialContributionCriteria'),
      },
      { key: '_enablingActivity', label: '', colspan: this.findMaxColspanForGroup('_enablingActivity') },
      { key: '_transitionalActivity', label: '', colspan: this.findMaxColspanForGroup('_transitionalActivity') },
    ];
  },
  getAdditionalGroupColspans(this: Self): { [groupName: string]: number } {
    return {
      substantialContributionCriteria: this.getEnvironmentalObjectivesLength(),
      _enablingActivity: 1,
      _transitionalActivity: 1,
    };
  },
  createMainColumnDataForRow(this: Self, activity: Record<string, unknown>) {
    const typedActivity = activity as EligibleOrAlignedActivityRow;
    return [
      ...this.createBaseMainColumnDataForRow(activity),
      ...this.createActivityGroupData<number | undefined>(
        typedActivity.activityName,
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
      ...this.createSingleFieldGroupData(activity, '_enablingActivity', 'enablingActivity'),
      ...this.createSingleFieldGroupData(activity, '_transitionalActivity', 'transitionalActivity'),
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
