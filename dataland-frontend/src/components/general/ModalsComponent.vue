<template>
  <template v-if="_isModal()">
    <component
      :is="_findModalComponent().triggerComponent"
      :displayComponent="_findModalComponent().dataComponent"
      :data="data"
    ></component>
  </template>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import GenericDataTableModalLink, {
  type GenericsDataTableRequiredData,
} from "@/components/general/GenericDataTableModalLink.vue";
import AlignedActivitiesDataTable from "@/components/general/AlignedActivitiesDataTable.vue";
import NonAlignedActivitiesDataTable from "@/components/general/NonAlignedActivitiesDataTable.vue";
import { assertDefined } from "@/utils/TypeScriptUtils";

type AvailableTriggerComponents = typeof GenericDataTableModalLink;
type AvailableDataComponents =
  | typeof AlignedActivitiesDataTable
  | typeof NonAlignedActivitiesDataTable
  | typeof DetailsCompanyDataTable;

type FieldTriggerAndDataComponents = {
  triggerComponent: AvailableTriggerComponents;
  dataComponent: AvailableDataComponents;
};

const genericFieldTriggerAndDataComponent = {
  triggerComponent: GenericDataTableModalLink,
  dataComponent: DetailsCompanyDataTable,
} as const;

const FieldsWithModalsMap: { [fieldName: string]: FieldTriggerAndDataComponents } = {
  AlignedActivitiesDataTable: {
    triggerComponent: GenericDataTableModalLink,
    dataComponent: AlignedActivitiesDataTable,
  },
  NonAlignedActivitiesDataTable: {
    triggerComponent: GenericDataTableModalLink,
    dataComponent: NonAlignedActivitiesDataTable,
  },
  MostImportantProductsFormField: genericFieldTriggerAndDataComponent,
  ProcurementCategoriesFormField: genericFieldTriggerAndDataComponent,
  ProductionSitesFormField: genericFieldTriggerAndDataComponent,
  MultiSelectFormField: genericFieldTriggerAndDataComponent,
  ComponentName: genericFieldTriggerAndDataComponent,
} as const;

export type GenericModalData = {
  modalTitle?: string;
  dataType?: GenericsDataTableRequiredData;
};

/**
 * @param componentName component name to find
 * @returns defined modal
 */
export function findModalComponent(componentName: string): FieldTriggerAndDataComponents | undefined {
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
    _findModalComponent(): FieldTriggerAndDataComponents {
      return assertDefined(findModalComponent(this.componentName));
    },
  },
});
</script>
