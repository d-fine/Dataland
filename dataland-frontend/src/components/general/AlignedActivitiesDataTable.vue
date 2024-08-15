<template>
  <DataTable data-test="dataTableTest" scrollable :value="frozenColumnData" class="activities-data-table">
    <ColumnGroup data-test="columnGroupTest" type="header">
      <Row>
        <Column
          header=""
          :frozen="true"
          alignFrozen="left"
          :colspan="2"
          class="frozen-row-header border-right"
          style="background-color: #fff"
        />
        <Column
          data-test="mainColumnTest"
          v-for="group of mainColumnGroups"
          :key="group.key"
          :header="group.label"
          :colspan="group.colspan"
          class="group-row-header"
        />
      </Row>
      <Row>
        <Column
          data-test="headerActivity"
          header="Activity"
          :frozen="true"
          alignFrozen="left"
          class="frozen-row-header"
        />
        <Column header="NACE Code(s)" :frozen="true" alignFrozen="left" class="frozen-row-header border-right" />
        <Column
          v-for="col of mainColumnDefinitions"
          :key="col.field"
          :header="col.header"
          :field="col.field"
          :class="groupColumnCssClasses(col, 'non-frozen-header')"
        />
      </Row>
    </ColumnGroup>
    <Column
      v-for="col of frozenColumnDefinitions"
      :field="col.field"
      :key="col.field"
      :header="col.header"
      :frozen="col.frozen"
      :class="columnCss(col.field)"
      bodyClass="headers-bg"
      headerClass="horizontal-headers-size"
    >
      <template #body="{ data }">
        <template v-if="col.field === 'activity'">{{ activityApiNameToHumanizedName(data.activity) }}</template>
        <template v-else>
          <ul class="unstyled-ul-list grid4">
            <li v-for="code of data.naceCodes" :key="code">{{ code }}</li>
          </ul>
        </template>
      </template>
    </Column>
    <Column
      v-for="col of mainColumnDefinitions"
      :field="col.field"
      :key="col.field"
      :header="col.header"
      bodyClass="col-value"
      headerClass="horizontal-headers-size"
      :class="groupColumnCssClasses(col)"
    >
      <template #body="{ data }">
        {{ findContentFromActivityGroupAndField(data.activity, col.group, col.field) }}
      </template>
    </Column>
  </DataTable>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import ColumnGroup from 'primevue/columngroup';
import Row from 'primevue/row';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import {
  type YesNo,
  type Activity,
  type EuTaxonomyAlignedActivity,
} from '@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model';
import { activityApiNameToHumanizedName } from '@/components/resources/frameworkDataSearch/EuTaxonomyActivityNames';
import { formatAmountWithCurrency, formatPercentageNumberAsString } from '@/utils/Formatter';

export const euTaxonomyObjectives = [
  'ClimateChangeMitigation',
  'ClimateChangeAdaptation',
  'SustainableUseAndProtectionOfWaterAndMarineResources',
  'TransitionToACircularEconomy',
  'PollutionPreventionAndControl',
  'ProtectionAndRestorationOfBiodiversityAndEcosystems',
];

type ActivityFieldValueObject = {
  activity: string;
  group: string;
  field: string;
  content: string;
};

type MainColumnDefinition = {
  field: string;
  header: string;
  frozen?: boolean;
  group: string;
  groupIndex: number;
};

export default defineComponent({
  inject: ['dialogRef'],
  name: 'AlignedActivitiesDataTable',
  components: { DataTable, Column, ColumnGroup, Row },
  data() {
    return {
      listOfRowContents: [] as Array<EuTaxonomyAlignedActivity>,
      kpiKeyOfTable: '' as string,
      columnHeaders: {} as { [kpiKeyOfTable: string]: { [columnName: string]: string } },
      frozenColumnDefinitions: [] as Array<{ field: string; header: string; frozen?: boolean; group: string }>,
      mainColumnGroups: [] as Array<{ key: string; label: string; colspan: number }>,
      mainColumnDefinitions: [] as Array<MainColumnDefinition>,
      frozenColumnData: [] as Array<{
        activity: Activity | undefined;
        naceCodes: string[] | undefined;
      }>,
      mainColumnData: [] as Array<ActivityFieldValueObject>,
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      listOfRowContents: Array<object | string>;
      kpiKeyOfTable: string;
      columnHeaders: { [kpiKeyOfTable: string]: { [columnName: string]: string } };
    };
    this.kpiKeyOfTable = dialogRefData.kpiKeyOfTable;
    this.columnHeaders = dialogRefData.columnHeaders;

    if (typeof dialogRefData.listOfRowContents[0] === 'string') {
      this.listOfRowContents = dialogRefData.listOfRowContents.map((o) => ({ [this.kpiKeyOfTable]: o }));
    } else {
      this.listOfRowContents = dialogRefData.listOfRowContents as Array<EuTaxonomyAlignedActivity>;
    }

    this.frozenColumnDefinitions = [
      { field: 'activity', header: this.humanizeHeaderName('activity'), frozen: true, group: '_frozen' },
      { field: 'naceCodes', header: this.humanizeHeaderName('naceCodes'), frozen: true, group: '_frozen' },
    ];

    this.mainColumnDefinitions = [
      { field: 'revenue', header: this.humanizeHeaderName('revenue'), group: '_revenue', groupIndex: 0 },
      { field: 'revenuePercent', header: this.humanizeHeaderName('revenuePercent'), group: '_revenue', groupIndex: 1 },

      ...this.makeGroupColumns('substantialContributionCriteria', 'substantialContribution'),
      ...this.makeGroupColumns('dnshCriteria', 'dnsh'),

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

    this.frozenColumnData = this.listOfRowContents.map((activity) => ({
      activity: activity.activityName,
      naceCodes: activity.naceCodes,
    }));

    this.mainColumnData = this.listOfRowContents
      .map((col) => [
        ...createRevenueGroupData(col),
        ...createActivityGroupData<number>(
          col.activityName as string,
          'substantialContributionCriteria',
          {
            substantialContributionToClimateChangeMitigationInPercent:
              col.substantialContributionToClimateChangeMitigationInPercent,
            substantialContributionToClimateChangeAdaptationInPercent:
              col.substantialContributionToClimateChangeAdaptationInPercent,
            substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent:
              col.substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent,
            substantialContributionToTransitionToACircularEconomyInPercent:
              col.substantialContributionToTransitionToACircularEconomyInPercent,
            substantialContributionToPollutionPreventionAndControlInPercent:
              col.substantialContributionToPollutionPreventionAndControlInPercent,
            substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent:
              col.substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent,
          },
          formatPercentageNumberAsString
        ),
        ...createActivityGroupData<YesNo>(
          col.activityName as string,
          'dnshCriteria',
          {
            dnshToClimateChangeMitigation: col.dnshToClimateChangeMitigation,
            dnshToClimateChangeAdaptation: col.dnshToClimateChangeAdaptation,
            dnshToSustainableUseAndProtectionOfWaterAndMarineResources:
              col.dnshToSustainableUseAndProtectionOfWaterAndMarineResources,
            dnshToTransitionToACircularEconomy: col.dnshToTransitionToACircularEconomy,
            dnshToPollutionPreventionAndControl: col.dnshToPollutionPreventionAndControl,
            dnshToProtectionAndRestorationOfBiodiversityAndEcosystems:
              col.dnshToProtectionAndRestorationOfBiodiversityAndEcosystems,
          },
          (value: YesNo) => (value ? `${value}` : '')
        ),
        ...createMinimumSafeguardsGroupData(col),
        ...createEnablingActivityGroupData(col),
        ...createTransitionalActivityGroupData(col),
      ])
      .flat();

    this.mainColumnGroups = [
      { key: '_revenue', label: '', colspan: this.findMaxColspanForGroup('_revenue') },
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
  methods: {
    activityApiNameToHumanizedName,
    /**
     * @param groupName name of the group to count number of fields
     * @returns the maximum value of fields per activity and group
     */
    findMaxColspanForGroup(groupName: string): number {
      const environmentalObjectivesLength = euTaxonomyObjectives.length;
      const colspans: { [groupName: string]: number } = {
        _revenue: 2,
        substantialContributionCriteria: environmentalObjectivesLength,
        dnshCriteria: environmentalObjectivesLength,
        _minimumSafeguards: 1,
        _enablingActivity: 1,
        _transitionalActivity: 1,
      };
      return colspans[groupName];
    },
    /**
     * Search mainColumnData for specific item and return it's value
     * @param activityName name of the targeted activity
     * @param groupName name of the targeted group
     * @param fieldName name of the targeted field
     * @returns string value from main data
     */
    findContentFromActivityGroupAndField(activityName: string, groupName: string, fieldName: string) {
      const value = this.mainColumnData.find(
        (item) => item.activity === activityName && item.group === groupName && item.field === fieldName
      );
      return value ? value.content : '';
    },

    /**
     * @param groupName the name of the group to which columns will be assigned
     * @param prefix prefix
     * @returns column definitions for group
     */
    makeGroupColumns(groupName: string, prefix: string) {
      const environmentalObjectiveKeys = euTaxonomyObjectives.map((suffix) => {
        const extendedKey = `${prefix}To${suffix}`;
        if (prefix === 'dnsh') {
          return extendedKey;
        } else {
          return `${extendedKey}InPercent`;
        }
      });
      return environmentalObjectiveKeys.map((environmentalObjectiveKey: string, index: number) => ({
        field: environmentalObjectiveKey,
        header: this.humanizeHeaderName(environmentalObjectiveKey),
        group: groupName,
        groupIndex: index,
      }));
    },

    /**
     * @param key Define the column's CSS class
     * @returns CSS class name
     */
    columnCss(key: string): string {
      switch (key) {
        case 'activity':
          return 'col-activity';
        case 'naceCodes':
          return 'col-nace-codes';
        default:
          return '';
      }
    },
    /**
     * @param columnDefinition column definition we check against
     * @param additionalClasses (optional) any additional classes to be added
     * @returns classes for specific columns
     */
    groupColumnCssClasses(columnDefinition: MainColumnDefinition, additionalClasses = ''): string {
      if (columnDefinition.groupIndex === 0) return `first-group-column ${additionalClasses}`;
      return additionalClasses;
    },
    /**
     * @param key the item to lookup
     * @returns the display version of the column header
     */
    humanizeHeaderName(key: string) {
      return this.columnHeaders[this.kpiKeyOfTable][key];
    },
  },
});

/**
 * @param activity targeted activity object
 * @returns list of revenue data items
 */
function createRevenueGroupData(activity: EuTaxonomyAlignedActivity): ActivityFieldValueObject[] {
  return [
    {
      activity: activity.activityName,
      group: '_revenue',
      field: 'revenue',
      content: formatAmountWithCurrency(activity.share?.absoluteShare),
    },
    {
      activity: activity.activityName,
      group: '_revenue',
      field: 'revenuePercent',
      content: formatPercentageNumberAsString(activity.share?.relativeShareInPercent),
    },
  ];
}

/**
 * @param activityName name of the activity
 * @param groupName the name of the group to which the fields will be assigned to
 * @param fields collection of fields and their values
 * @param valueFormatter function which formats the final look of the value
 * @returns grouped list of data items
 */
function createActivityGroupData<T>(
  activityName: string,
  groupName: string,
  fields: { [key: string]: T } | undefined,
  valueFormatter: (value: T) => string
): ActivityFieldValueObject[] {
  const fieldsEntries = Object.entries(fields ?? {});
  return fieldsEntries
    .filter(([, value]) => value != null)
    .map(([field, value]) => {
      return {
        activity: activityName,
        group: groupName,
        field,
        content: valueFormatter(value) ?? '',
      };
    });
}

/**
 * @param activity targeted activity object
 * @returns list of minimum safeguards data items
 */
function createMinimumSafeguardsGroupData(activity: EuTaxonomyAlignedActivity): ActivityFieldValueObject[] {
  return [
    {
      activity: activity.activityName as string,
      group: '_minimumSafeguards',
      field: 'minimumSafeguards',
      content: activity.minimumSafeguards ?? '',
    },
  ];
}

/**
 * @param activity targeted activity object
 * @returns list of minimum safeguards data items
 */
function createEnablingActivityGroupData(activity: EuTaxonomyAlignedActivity): ActivityFieldValueObject[] {
  return [
    {
      activity: activity.activityName as string,
      group: '_enablingActivity',
      field: 'enablingActivity',
      content: activity.enablingActivity ?? '',
    },
  ];
}

/**
 * @param activity targeted activity object
 * @returns list of minimum safeguards data items
 */
function createTransitionalActivityGroupData(activity: EuTaxonomyAlignedActivity): ActivityFieldValueObject[] {
  return [
    {
      activity: activity.activityName as string,
      group: '_transitionalActivity',
      field: 'transitionalActivity',
      content: activity.transitionalActivity ?? '',
    },
  ];
}
</script>
