<template>
  <template v-if="_isModal()">
    <component :is="_findModalComponent()" :data="data"></component>
  </template>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DetailsCompanyDataTableModal, {
  type DetailsCompanyDataTableRequiredData,
} from "@/components/general/DetailsCompanyDataTableModal.vue";
// import AlignedActivitiesDataTable from "@/components/general/AlignedActivitiesDataTable.vue";
// import NonAlignedActivitiesDataTable from "@/components/general/NonAlignedActivitiesDataTable.vue";

export type AvailableModals =
  // | typeof AlignedActivitiesDataTable
  // | typeof NonAlignedActivitiesDataTable
   typeof DetailsCompanyDataTableModal;

export type AvailableModalsDataTypes = typeof Object | DetailsCompanyDataTableRequiredData;

export const FieldsWithModalsMap: { [fieldName: keyof AvailableModals]: AvailableModals } = {
  // AlignedActivitiesDataTable,
  // NonAlignedActivitiesDataTable,
  NaceCodeFormField: DetailsCompanyDataTableModal,
  MostImportantProductsFormField: DetailsCompanyDataTableModal,
} as const;

export type GenericModalData = {
  modalTitle?: string;
  dataType?: AvailableModalsDataTypes;
};

/**
 * @param componentName component name to find
 * @returns defined modal
 */
export function findModalComponent(componentName: string): AvailableModals | undefined {
  const found = Object.entries(FieldsWithModalsMap).find(([key]) => key === componentName);
  return found ? found[1] : undefined;
}
/**
 * @param componentName name of component to be checked
 * @returns whether passed component is a modal
 */
export function isModal(componentName: string): boolean {
  return !!findModalComponent(componentName);
}

export default defineComponent({
  name: "ModalsComponent",
  components: {
    // AlignedActivitiesDataTable,
    // NonAlignedActivitiesDataTable,
    DetailsCompanyDataTableModal,
  },
  inheritAttrs: false,
  inject: {},
  props: {
    componentName: {
      type: String,
      default: "componentName",
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
    _findModalComponent(): AvailableModals {
      return findModalComponent(this.componentName) as AvailableModals;
    },
  },
});
</script>
