<template>
  <div :data-test="dataTest">
    <Dropdown
      :options="displayOptions"
      v-bind:model-value="selectedOption"
      @update:model-value="handleInputChange($event)"
      :placeholder="placeholder"
      :name="name"
      :showClear="!isRequired"
      option-label="label"
      option-value="value"
      :class="'bottom-line ' + inputClass + ' ' + (!selectedOption ? ' no-selection' : '')"
      :disabled="disabled"
    />
    <!--  FormKit component only used to parse the selected value in nested FormKits  -->
    <FormKit
      type="text"
      :disabled="disabled"
      :name="name"
      v-bind:model-value="selectedOption"
      @update:model-value="handleFormKitInputChange($event)"
      :outer-class="{ 'hidden-input': true, 'formkit-outer': false }"
      :validation-label="validationLabel"
      :validation="validation"
      :ignore="ignore"
    />
  </div>
</template>

<script lang="ts">
import { type ComponentPropsOptions, defineComponent } from "vue";
import Dropdown from "primevue/dropdown";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { deepCopyObject, type ObjectType } from "@/utils/UpdateObjectUtils";
import { type DropdownOption } from "@/utils/PremadeDropdownDatasets";
import { isStringArray } from "@/utils/TypeScriptUtils";

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
    dataTest: String,
  }) as Readonly<ComponentPropsOptions>,
  data() {
    return {
      selectedOption: this.modelValue as string | null,
    };
  },
  emits: ["update:modelValue"],
  watch: {
    modelValue(newValue: string) {
      this.selectedOption = newValue;
    },
    options() {
      if (
        !(this.displayOptions as (DropdownOption | undefined)[])
          .map((entry) => entry?.value)
          .includes(this.selectedOption ?? undefined)
      ) {
        // TODO: CLEANUP
        console.log(`Unknown option: '${this.selectedOption}'. Allowed: ${JSON.stringify(this.displayOptions)}  `);
      }
    },
  },
  computed: {
    displayOptions(): DropdownOption[] {
      const inputOptions = this.options as string[] | DropdownOption[] | Record<string, string>;
      if (Array.isArray(inputOptions)) {
        if (isStringArray(inputOptions)) {
          return inputOptions.map((entry) => ({ value: entry, label: entry }));
        } else {
          return inputOptions;
        }
      } else {
        return Object.entries(inputOptions).map((pair) => ({ value: pair[0], label: pair[1] }));
      }
    },
  },
  methods: {
    /**
     * Handler for changes in the dropdown component
     * @param newInput the new value in the dropdown
     */
    handleInputChange(newInput: string) {
      this.selectedOption = newInput;
      this.$emit("update:modelValue", this.selectedOption);
    },
    /**
     * Handler for changes in the formkit component (e.g. called if data got loaded)
     * @param newInput the new value in the field
     */
    handleFormKitInputChange(newInput: string) {
      this.selectedOption = newInput;
      this.$emit("update:modelValue", this.selectedOption);
    },
  },
});
</script>

<style lang="scss" scoped>
.bottom-line {
  border-style: solid;
  border-width: 0 0 1px 0;
  border-color: #958d7c;
}
</style>
