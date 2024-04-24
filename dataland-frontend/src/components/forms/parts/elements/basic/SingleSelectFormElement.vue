<template>
  <Dropdown
    :options="displayOptions"
    v-bind:model-value="selectedOption"
    @update:model-value="handleInputChange($event)"
    :placeholder="placeholder"
    :name="name"
    :showClear="!isRequired"
    :option-label="optionLabel"
    :option-value="optionValue"
    :class="'bottom-line ' + inputClass + ' ' + (!selectedOption ? ' no-selection' : '')"
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
    :ignore="ignore"
  />
</template>

<script lang="ts">
import { type ComponentPropsOptions, defineComponent } from "vue";
import Dropdown from "primevue/dropdown";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { deepCopyObject, type ObjectType } from "@/utils/UpdateObjectUtils";
import {DropdownOption} from "@/utils/PremadeDropdownDatasets";

export default defineComponent({
  name: "SingleSelectFormElement",
  components: { Dropdown },
  props: Object.assign(deepCopyObject(DropdownOptionFormFieldProps as ObjectType), {
    inputClass: { type: String, default: "long" },
    isRequired: { type: Boolean },
    disabled: {
      type: Boolean,
      default: false,
    },
    modelValue: String,
    ignore: {
      type: Boolean,
      default: false,
    },
  }) as Readonly<ComponentPropsOptions>,
  data() {
    return {
      selectedOption: null as null | string,
      optionLabel: "label",
      optionValue: "value",
    };
  },
  emits: ["update:model-value"],
  watch: {
    modelValue(newValue: string) {
      this.selectedOption = newValue;
    },
    options() {
      if(!(this.displayOptions as (DropdownOption | undefined)[])
          .map((entry) => entry?.value).includes(this.selectedOption ?? undefined)) {
        this.selectedOption = null;
      }
    }
  },
  computed: {
    displayOptions() {
      if(Array.isArray(this.options)) {
        if((this.options as unknown[]).every((element) => typeof element == "object")) { return this.options; }
        else { return this.options.map((entry) => ({value: entry, label: entry})); }
      }
      return Object.entries(this.options).map((entry) => ({value: entry[0], label: entry[1]}));
    }
  },
  methods: {
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
</style>
