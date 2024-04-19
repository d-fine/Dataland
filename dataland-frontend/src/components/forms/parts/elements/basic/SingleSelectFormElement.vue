<template>
  <Dropdown
    :options="options"
    v-model="selectedOption"
    :placeholder="placeholder"
    :name="name"
    class="w-full md:w-14rem short"
    showClear
    option-label="label"
    option-value="value"
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
  },
  data() {
    return {
      selectedOption: null,
    };
  },
});
</script>
