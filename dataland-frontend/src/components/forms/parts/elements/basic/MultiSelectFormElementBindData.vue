<template>
  <MultiSelect
    v-model="selections"
    :options="options"
    :placeholder="placeholder"
    :show-toggle-all="false"
    :class="innerClass"
    :optionValue="optionValue"
    :optionLabel="optionLabel"
    :maxSelectedLabels="3"
  />
</template>

<script lang="ts">
import { ComponentPropsOptions, defineComponent } from "vue";
import MultiSelect from "primevue/multiselect";
import { MultiSelectFormProps } from "@/components/forms/parts/fields/FormFieldProps";
import { DropdownOption } from "@/utils/PremadeDropdownDatasets";

export default defineComponent({
  name: "MultiSelectFormElementBindData",
  components: { MultiSelect },
  emits: ["update:selectedItemsBind"],
  computed: {
    selections: {
      get(): Array<DropdownOption> {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-return
        return this.selectedItemsBind;
      },
      set(newValue: Array<DropdownOption>) {
        this.$emit("update:selectedItemsBind", newValue);
      },
    },
  },
  props: {
    ...MultiSelectFormProps,
    selectedItemsBind: {
      type: Array,
      default: () => [],
    },
  } as Readonly<ComponentPropsOptions>,
});
</script>
