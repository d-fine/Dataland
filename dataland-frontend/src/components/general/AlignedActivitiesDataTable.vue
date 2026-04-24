<script lang="ts">
import { defineComponent } from 'vue';
import { type YesNo } from '@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model';
import BaseActivitiesDataTable from '@/components/general/BaseActivitiesDataTable.vue';
import { formatPercentageNumberAsString } from '@/utils/Formatter';

type Self = InstanceType<typeof BaseActivitiesDataTable>;

type AlignedActivityRow = Record<string, unknown> & {
  activityName?: string;
  substantialContributionToClimateChangeMitigationInPercent?: number;
  substantialContributionToClimateChangeAdaptationInPercent?: number;
  substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent?: number;
  substantialContributionToTransitionToACircularEconomyInPercent?: number;
  substantialContributionToPollutionPreventionAndControlInPercent?: number;
  substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent?: number;
  dnshToClimateChangeMitigation?: YesNo;
  dnshToClimateChangeAdaptation?: YesNo;
  dnshToSustainableUseAndProtectionOfWaterAndMarineResources?: YesNo;
  dnshToTransitionToACircularEconomy?: YesNo;
  dnshToPollutionPreventionAndControl?: YesNo;
  dnshToProtectionAndRestorationOfBiodiversityAndEcosystems?: YesNo;
};

const alignedActivitiesDataTableConfiguration: Self['activitiesDataTableConfiguration'] = {
  createAdditionalMainColumnDefinitions(this: Self) {
    return [
      ...this.makeGroupColumns('substantialContributionCriteria', 'substantialContribution'),
      ...this.makeGroupColumns('dnshCriteria', 'dnsh', false),
      {
        field: 'minimumSafeguards',
        header: this.humanizeHeaderName('minimumSafeguards'),
        group: '_minimumSafeguards',
        groupIndex: 0,
      },
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
      {
        key: 'dnshCriteria',
        label: this.humanizeHeaderName('dnshCriteria'),
        colspan: this.findMaxColspanForGroup('dnshCriteria'),
      },
      { key: '_minimumSafeguards', label: '', colspan: this.findMaxColspanForGroup('_minimumSafeguards') },
      { key: '_enablingActivity', label: '', colspan: this.findMaxColspanForGroup('_enablingActivity') },
      { key: '_transitionalActivity', label: '', colspan: this.findMaxColspanForGroup('_transitionalActivity') },
    ];
  },
  getAdditionalGroupColspans(this: Self): { [groupName: string]: number } {
    const environmentalObjectivesLength = this.getEnvironmentalObjectivesLength();
    return {
      substantialContributionCriteria: environmentalObjectivesLength,
      dnshCriteria: environmentalObjectivesLength,
      _minimumSafeguards: 1,
      _enablingActivity: 1,
      _transitionalActivity: 1,
    };
  },
  createMainColumnDataForRow(this: Self, activity: Record<string, unknown>) {
    const typedActivity = activity as AlignedActivityRow;
    return [
      ...this.createBaseMainColumnDataForRow(activity),
      ...this.createActivityGroupData<number | undefined>(
        typedActivity.activityName ?? '',
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
      ...this.createActivityGroupData<YesNo | undefined>(
        typedActivity.activityName ?? '',
        'dnshCriteria',
        {
          dnshToClimateChangeMitigation: typedActivity.dnshToClimateChangeMitigation,
          dnshToClimateChangeAdaptation: typedActivity.dnshToClimateChangeAdaptation,
          dnshToSustainableUseAndProtectionOfWaterAndMarineResources:
            typedActivity.dnshToSustainableUseAndProtectionOfWaterAndMarineResources,
          dnshToTransitionToACircularEconomy: typedActivity.dnshToTransitionToACircularEconomy,
          dnshToPollutionPreventionAndControl: typedActivity.dnshToPollutionPreventionAndControl,
          dnshToProtectionAndRestorationOfBiodiversityAndEcosystems:
            typedActivity.dnshToProtectionAndRestorationOfBiodiversityAndEcosystems,
        },
        (value: YesNo | undefined) => (value ? `${value}` : '')
      ),
      ...this.createSingleFieldGroupData(activity, '_minimumSafeguards', 'minimumSafeguards'),
      ...this.createSingleFieldGroupData(activity, '_enablingActivity', 'enablingActivity'),
      ...this.createSingleFieldGroupData(activity, '_transitionalActivity', 'transitionalActivity'),
    ] as ReturnType<Self['createBaseMainColumnDataForRow']>;
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
