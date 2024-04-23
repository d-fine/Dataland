<template>
  <Dropdown
    :options="options"
    v-bind:model-value="selectedOption"
    @update:model-value="handleInputChange($event)"
    :placeholder="placeholder"
    :name="name"
    :showClear="!isRequired"
    :option-label="optionLabel"
    :option-value="optionValue"
    :class="{
      'bottom-line': true,
      'input-class': true, // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      'no-selection': !selectedOption,
    }"
    :disabled="disabled"
  />
  <FormKit
    type="text"
    :disabled="disabled"
    :name="name"
    v-bind:model-value="selectedOption"
    @update:model-value="handleFormKitInputChange($event)"
    outer-class="hidden-input"
    :validation-label="validationLabel"
    :validation="validation"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import Dropdown from "primevue/dropdown";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "SingleSelectFormElement",
  components: { Dropdown },
  props: {
    ...DropdownOptionFormFieldProps,
    isRequired: { type: Boolean },
    disabled: {
      type: Boolean,
      default: false,
    },
    modelValue: String,
  },
  data() {
    return {
      selectedOption: null as null | string,
      optionLabel: "",
      optionValue: "",
    };
  },
  created() {
    this.setOptionLabelValue();
  },
  emits: ["valueSelected", "update:model-value"],
  watch: {
    selectedOption(newValue) {
      this.$emit("valueSelected", newValue);
    },
    modelValue(newValue: string) {
      this.selectedOption = newValue;
    },
  },
  methods: {
    /**
     * Sets the values of optionLabel and optionValue depending on whether the options are [{label: ... , value: ...}]
     * or a simple array
     */
    setOptionLabelValue(): void {
      if (Array.isArray(this.options) && (this.options as unknown[]).every((element) => typeof element == "object")) {
        this.optionLabel = "label";
        this.optionValue = "value";
      }
    },
    /**
     * Handler for changes in the dropdown component
     * @param newInput the new value in the dropdown
     */
    handleInputChange(newInput: string) {
      this.selectedOption = newInput;
      this.$emit("update:model-value", this.selectedOption);
    },
    /**
     * Handler for changes in the formkit component (e.g. called if data got loaded)
     * @param newInput the new value in the field
     */
    handleFormKitInputChange(newInput: string) {
      this.selectedOption = newInput;
      this.$emit("update:model-value", this.selectedOption);
    },
  },
});
</script>

<style>
.bottom-line {
  border-style: solid;
  border-width: 0 0 1px 0;
  border-color: #958d7c;
}

.no-selection {
  color: #767676;
}
</style>
