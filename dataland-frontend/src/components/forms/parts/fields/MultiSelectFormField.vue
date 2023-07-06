<template>
  <UploadFormHeader :label="label" :description="description" :is-required="required" />
  <MultiSelectFormElement
    :name="name"
    v-model="internalSelections"
    :validation="validation"
    :validation-label="validationLabel ?? label"
    :placeholder="placeholder"
    :options="options"
    :inner-class="innerClass"
    :optionLabel="optionLabel"
    :ignore="ignore"
    @selectedValuesChanged="selectedValuesChanged"
  />
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";

import MultiSelectFormElement from "@/components/forms/parts/elements/basic/MultiSelectFormElement.vue";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "MultiSelectFormField",
  emits: ["selectedValuesChanged"],
  components: { MultiSelectFormElement, UploadFormHeader },
  data() {
    return {
      internalSelections: this.modelValue,
    };
  },
  props: {
    ...DropdownOptionFormFieldProps,
    optionLabel: {
      type: String,
    },
    modelValue: {
      type: Array,
      default: () => [],
    },
    ignore: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    /**
     * handle changes in selected Countries
     * @param newVal - selected Countries new Value
     */
    selectedValuesChanged(newVal) {
      this.$emit("selectedValuesChanged", newVal);
    },
  },
});
</script>
