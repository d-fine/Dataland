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
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { type Activity } from '@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model';
import { activityApiNameToHumanizedName } from '@/components/resources/frameworkDataSearch/EuTaxonomyActivityNames';
import { formatAmountWithCurrency, formatPercentageNumberAsString } from '@/utils/Formatter';

const euTaxonomyObjectives = [
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

type ActivitiesDataTableConfigurationContext = {
  humanizeHeaderName: (key: string) => string;
  findMaxColspanForGroup: (groupName: string) => number;
  makeGroupColumns: (
    groupName: string,
    prefix: string,
    shouldAppendInPercentSuffix?: boolean
  ) => MainColumnDefinition[];
  createBaseMainColumnDataForRow: (activity: Record<string, unknown>) => ActivityFieldValueObject[];
  createActivityGroupData: <T>(
    activityName: string,
    groupName: string,
    fields: { [key: string]: T } | undefined,
    valueFormatter: (value: T) => string
  ) => ActivityFieldValueObject[];
  createSingleFieldGroupData: (
    activity: Record<string, unknown>,
    groupName: string,
    fieldName: string
  ) => ActivityFieldValueObject[];
  getEnvironmentalObjectivesLength: () => number;
};

type ActivitiesDataTableConfiguration = {
  createAdditionalMainColumnDefinitions: (context: ActivitiesDataTableConfigurationContext) => MainColumnDefinition[];
  createAdditionalMainColumnGroups: (
    context: ActivitiesDataTableConfigurationContext
  ) => Array<{ key: string; label: string; colspan: number }>;
  getAdditionalGroupColspans: (context: ActivitiesDataTableConfigurationContext) => { [groupName: string]: number };
  createMainColumnDataForRow: (
    activity: Record<string, unknown>,
    context: ActivitiesDataTableConfigurationContext
  ) => ActivityFieldValueObject[];
};

const defaultActivitiesDataTableConfiguration: ActivitiesDataTableConfiguration = {
  createAdditionalMainColumnDefinitions() {
    return [];
  },
  createAdditionalMainColumnGroups() {
    return [];
  },
  getAdditionalGroupColspans() {
    return {};
  },
  createMainColumnDataForRow(activity, context) {
    return context.createBaseMainColumnDataForRow(activity);
  },
};

export default defineComponent({
  inject: ['dialogRef'],
  name: 'BaseActivitiesDataTable',
  components: { DataTable, Column, ColumnGroup, Row },
  data() {
    return {
      listOfRowContents: [] as Array<Record<string, unknown>>,
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
      activitiesDataTableConfiguration: defaultActivitiesDataTableConfiguration,
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
      this.listOfRowContents = dialogRefData.listOfRowContents as Array<Record<string, unknown>>;
    }

    this.frozenColumnDefinitions = this.createFrozenColumnDefinitions();
    this.mainColumnDefinitions = this.createMainColumnDefinitions();
    this.frozenColumnData = this.createFrozenColumnData();
    this.mainColumnData = this.listOfRowContents.flatMap((activity) => this.createMainColumnDataForRow(activity));
    this.mainColumnGroups = this.createMainColumnGroups();
  },
  methods: {
    activityApiNameToHumanizedName,
    /**
     * @returns the shared table configuration hooks for the current component instance
     */
    getActivitiesDataTableConfigurationContext(): ActivitiesDataTableConfigurationContext {
      return {
        humanizeHeaderName: (key: string) => this.humanizeHeaderName(key),
        findMaxColspanForGroup: (groupName: string) => this.findMaxColspanForGroup(groupName),
        makeGroupColumns: (groupName: string, prefix: string, shouldAppendInPercentSuffix = true) =>
          this.makeGroupColumns(groupName, prefix, shouldAppendInPercentSuffix),
        createBaseMainColumnDataForRow: (activity: Record<string, unknown>) =>
          this.createBaseMainColumnDataForRow(activity),
        createActivityGroupData: <T,>(
          activityName: string,
          groupName: string,
          fields: { [key: string]: T } | undefined,
          valueFormatter: (value: T) => string
        ) => this.createActivityGroupData(activityName, groupName, fields, valueFormatter),
        createSingleFieldGroupData: (activity: Record<string, unknown>, groupName: string, fieldName: string) =>
          this.createSingleFieldGroupData(activity, groupName, fieldName),
        getEnvironmentalObjectivesLength: () => this.getEnvironmentalObjectivesLength(),
      };
    },
    /**
     * @returns the frozen column definitions displayed on the left-hand side of the table
     */
    createFrozenColumnDefinitions() {
      return [
        { field: 'activity', header: this.humanizeHeaderName('activity'), frozen: true, group: '_frozen' },
        { field: 'naceCodes', header: this.humanizeHeaderName('naceCodes'), frozen: true, group: '_frozen' },
      ];
    },
    /**
     * @returns the full list of main column definitions including configuration-specific additions
     */
    createMainColumnDefinitions() {
      const kpiField = this.kpiKeyOfTable;
      const kpiPercentField = `${kpiField}Percent`;

      return [
        {
          field: kpiField,
          header: this.humanizeHeaderName(kpiField),
          group: '_kpi',
          groupIndex: 0,
        },
        {
          field: kpiPercentField,
          header: this.humanizeHeaderName(kpiPercentField),
          group: '_kpi',
          groupIndex: 1,
        },
        ...this.activitiesDataTableConfiguration.createAdditionalMainColumnDefinitions(
          this.getActivitiesDataTableConfigurationContext()
        ),
      ];
    },
    /**
     * @returns the frozen column row data for activities and NACE codes
     */
    createFrozenColumnData() {
      return this.listOfRowContents.map((activity) => ({
        activity: activity.activityName,
        naceCodes: activity.naceCodes,
      }));
    },
    /**
     * @param activity one activity row from the dialog payload
     * @returns the main column data entries for that row according to the active configuration
     */
    createMainColumnDataForRow(activity: Record<string, unknown>) {
      return this.activitiesDataTableConfiguration.createMainColumnDataForRow(
        activity,
        this.getActivitiesDataTableConfigurationContext()
      );
    },
    /**
     * @param activity one activity row from the dialog payload
     * @returns the KPI-related data entries shared by all activity table variants
     */
    createBaseMainColumnDataForRow(activity: Record<string, unknown>) {
      return [...this.createKpiGroupData(activity, this.kpiKeyOfTable)];
    },
    /**
     * @returns the grouped table header metadata including configuration-specific groups
     */
    createMainColumnGroups() {
      return [
        { key: '_kpi', label: '', colspan: this.findMaxColspanForGroup('_kpi') },
        ...this.activitiesDataTableConfiguration.createAdditionalMainColumnGroups(
          this.getActivitiesDataTableConfigurationContext()
        ),
      ];
    },
    /**
     * @returns the number of configured EU taxonomy environmental objectives
     */
    getEnvironmentalObjectivesLength() {
      return euTaxonomyObjectives.length;
    },
    /**
     * @param groupName name of the group to count number of fields
     * @returns the maximum value of fields per activity and group
     */
    findMaxColspanForGroup(groupName: string): number {
      const colspans: { [groupName: string]: number } = {
        _kpi: 2,
        ...this.activitiesDataTableConfiguration.getAdditionalGroupColspans(
          this.getActivitiesDataTableConfigurationContext()
        ),
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
     * @param shouldAppendInPercentSuffix whether fields should end with InPercent
     * @returns column definitions for group
     */
    makeGroupColumns(groupName: string, prefix: string, shouldAppendInPercentSuffix = true) {
      const environmentalObjectiveKeys = euTaxonomyObjectives.map((suffix) => {
        const extendedKey = `${prefix}To${suffix}`;
        if (!shouldAppendInPercentSuffix) {
          return extendedKey;
        }
        return `${extendedKey}InPercent`;
      });
      return environmentalObjectiveKeys.map((environmentalObjectiveKey: string, index: number) => ({
        field: environmentalObjectiveKey,
        header: this.humanizeHeaderName(environmentalObjectiveKey),
        group: groupName,
        groupIndex: index,
      }));
    },

    /**
     * @param activity targeted activity object
     * @param kpiKey key of displayed kpi
     * @returns list of kpi data items
     */
    createKpiGroupData(activity: Record<string, unknown>, kpiKey: 'revenue' | 'capex' | 'opex') {
      const value = activity.share?.absoluteShare;
      const percent = activity.share?.relativeShareInPercent;

      return [
        {
          activity: activity.activityName,
          group: '_kpi',
          field: kpiKey,
          content: formatAmountWithCurrency(value),
        },
        {
          activity: activity.activityName,
          group: '_kpi',
          field: `${kpiKey}Percent`,
          content: formatPercentageNumberAsString(percent),
        },
      ];
    },
    /**
     * @param activityName name of the activity
     * @param groupName the name of the group to which the fields will be assigned to
     * @param fields collection of fields and their values
     * @param valueFormatter function which formats the final look of the value
     * @returns grouped list of data items
     */
    createActivityGroupData<T>(
      activityName: string,
      groupName: string,
      fields: { [key: string]: T } | undefined,
      valueFormatter: (value: T) => string
    ) {
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
    },
    /**
     * @param activity targeted activity object
     * @param groupName the group name of the data item
     * @param fieldName the source field name on the activity object
     * @returns list with a single data item
     */
    createSingleFieldGroupData(activity: Record<string, unknown>, groupName: string, fieldName: string) {
      return [
        {
          activity: activity.activityName as string,
          group: groupName,
          field: fieldName,
          content: activity[fieldName] ?? '',
        },
      ];
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
</script>
<style scoped>
ul.unstyled-ul-list {
  padding: 0;
  margin: 0;

  li {
    padding: 0;
    margin: 0;
    list-style: none;
  }
}
</style>
