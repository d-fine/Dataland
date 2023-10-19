<template>
  <div class="form-field" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <MultiSelectFormElementBindData
      :name="name"
      v-model:selectedItemsBind="internalSelections"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :placeholder="placeholder"
      :options="options"
      :inner-class="innerClass"
      :optionLabel="optionLabel"
      :optionValue="optionValue"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { type ComponentPropsOptions, defineComponent } from "vue";
import { type DropdownOption } from "@/utils/PremadeDropdownDatasets";
import MultiSelectFormElementBindData from "@/components/forms/parts/elements/basic/MultiSelectFormElementBindData.vue";
import { MultiSelectFormProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "MultiSelectFormFieldBindData",
  components: { MultiSelectFormElementBindData, UploadFormHeader },
  emits: ["update:selectedItemsBindInternal"],
  computed: {
    internalSelections: {
      get(): Array<DropdownOption> {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-return
        return this.selectedItemsBindInternal;
      },
      set(newValue: Array<DropdownOption>) {
        this.$emit("update:selectedItemsBindInternal", newValue);
      },
    },
  },
  props: {
    ...MultiSelectFormProps,
    selectedItemsBindInternal: {
      type: Array,
      default: () => [],
    },
  } as Readonly<ComponentPropsOptions>,
});
</script>
