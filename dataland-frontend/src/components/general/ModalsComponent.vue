<template>
  <template v-if="_isModal()">
    <component
      :is="_findModalComponent().triggerComponent"
      :displayComponent="_findModalComponent().displayComponent"
      :data="data"
    ></component>
  </template>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import GenericDataTableModalLink from "@/components/general/GenericDataTableModalLink.vue";
import AlignedActivitiesDataTable from "@/components/general/AlignedActivitiesDataTable.vue";
import NonAlignedActivitiesDataTable from "@/components/general/NonAlignedActivitiesDataTable.vue";
import { assertDefined } from "@/utils/TypeScriptUtils";

type AvailableTriggerComponents = typeof GenericDataTableModalLink;
type AvailableDisplayComponents =
  | typeof AlignedActivitiesDataTable
  | typeof NonAlignedActivitiesDataTable
  | typeof DetailsCompanyDataTable;

type FieldTriggerAndDisplayComponents = {
  triggerComponent: AvailableTriggerComponents;
  displayComponent: AvailableDisplayComponents;
};

const genericFieldTriggerAndDisplayComponent = {
  triggerComponent: GenericDataTableModalLink,
  displayComponent: DetailsCompanyDataTable,
} as const;

const FieldsWithModalsMap: { [fieldName: string]: FieldTriggerAndDisplayComponents } = {
  AlignedActivitiesDataTable: {
    triggerComponent: GenericDataTableModalLink,
    displayComponent: AlignedActivitiesDataTable,
  },
  NonAlignedActivitiesDataTable: {
    triggerComponent: GenericDataTableModalLink,
    displayComponent: NonAlignedActivitiesDataTable,
  },
  DriveMixFormField: genericFieldTriggerAndDisplayComponent,
  NaceCodeFormField: genericFieldTriggerAndDisplayComponent,
  MostImportantProductsFormField: genericFieldTriggerAndDisplayComponent,
  ProcurementCategoriesFormField: genericFieldTriggerAndDisplayComponent,
  ProductionSitesFormField: genericFieldTriggerAndDisplayComponent,
  MultiSelectFormField: genericFieldTriggerAndDisplayComponent,
  ComponentName: genericFieldTriggerAndDisplayComponent,
} as const;

/**
 * @param componentName component name to find
 * @returns defined modal
 */
export function findModalComponent(componentName: string): FieldTriggerAndDisplayComponents | undefined {
  const found = Object.entries(FieldsWithModalsMap).find(([key]) => key === componentName);
  return found ? found[1] : undefined;
}
/**
 * @param componentName name of component to be checked
 * @returns boolean whether passed component is a modal
 */
export function isModal(componentName: string): boolean {
  return !!findModalComponent(componentName);
}

export default defineComponent({
  name: "ModalsComponent",
  inheritAttrs: false,
  props: {
    componentName: {
      type: String,
      default: "ComponentName",
    },
    data: {},
  },
  methods: {
    /**
     * @private
     * @returns if passed component name is present in the modals map
     */
    _isModal() {
      return isModal(this.componentName);
    },
    /**
     * @private
     * @returns defined modal
     */
    _findModalComponent(): FieldTriggerAndDisplayComponents {
      return assertDefined(findModalComponent(this.componentName));
    },
  },
});
</script>
