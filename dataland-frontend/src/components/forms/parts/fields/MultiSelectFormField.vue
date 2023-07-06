<template>
  <div class="form-field" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <MultiSelectFormElement
      :name="name"
      :validation="validation"
      :validation-value="validationValue"
      :validation-label="validationLabel"
      :placeholder="placeholder"
      :options="options"
      :inner-class="innerClass"
      :optionLabel="optionLabel"
      @selectedValuesChanged="selectedValuesChanged"
    />
  </div>
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
  props: {
    ...DropdownOptionFormFieldProps,
    optionLabel: {
      type: String,
      default: "label",
    },
    optionValue: {
      type: String,
      default: "value",
    },
  },
  methods: {
    /**
     * handle changes in selected Countrys
     * @param newVal - selected countrys new Value
     */
    selectedValuesChanged(newVal) {
      this.$emit("selectedValuesChanged", newVal);
    },
  },
});
</script>
