<template>
  <Dropdown
    :options="options"
    v-model="selectedOption"
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
  />
  <FormKit type="hidden" :name="name" v-model="selectedOption" />
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
  },
  data() {
    return {
      selectedOption: null,
      optionLabel: "",
      optionValue: "",
    };
  },
  created() {
    this.setOptionLabelValue();
  },
  emits: ["valueSelected"],
  watch: {
    selectedOption(newValue) {
      this.$emit("valueSelected", newValue);
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
