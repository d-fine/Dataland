<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import { type YesNo } from '@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model';
import BaseActivitiesDataTable from '@/components/general/BaseActivitiesDataTable.vue';
import { formatPercentageNumberAsString } from '@/utils/Formatter';

const alignedActivitiesDataTableConfiguration = {
  createAdditionalMainColumnDefinitions(self: any): Array<Record<string, unknown>> {
    return [
      ...self.makeGroupColumns('substantialContributionCriteria', 'substantialContribution'),
      ...self.makeGroupColumns('dnshCriteria', 'dnsh', false),
      {
        field: 'minimumSafeguards',
        header: self.humanizeHeaderName('minimumSafeguards'),
        group: '_minimumSafeguards',
        groupIndex: 0,
      },
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
      {
        key: 'dnshCriteria',
        label: self.humanizeHeaderName('dnshCriteria'),
        colspan: self.findMaxColspanForGroup('dnshCriteria'),
      },
      { key: '_minimumSafeguards', label: '', colspan: self.findMaxColspanForGroup('_minimumSafeguards') },
      { key: '_enablingActivity', label: '', colspan: self.findMaxColspanForGroup('_enablingActivity') },
      { key: '_transitionalActivity', label: '', colspan: self.findMaxColspanForGroup('_transitionalActivity') },
    ];
  },
  getAdditionalGroupColspans(self: any): { [groupName: string]: number } {
    const environmentalObjectivesLength = self.getEnvironmentalObjectivesLength();
    return {
      substantialContributionCriteria: environmentalObjectivesLength,
      dnshCriteria: environmentalObjectivesLength,
      _minimumSafeguards: 1,
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
      ...self.createActivityGroupData<YesNo>(
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
      ...self.createSingleFieldGroupData(activity, '_minimumSafeguards', 'minimumSafeguards'),
      ...self.createSingleFieldGroupData(activity, '_enablingActivity', 'enablingActivity'),
      ...self.createSingleFieldGroupData(activity, '_transitionalActivity', 'transitionalActivity'),
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
