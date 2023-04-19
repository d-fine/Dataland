<template>
  <RadioButtonsFormElement
    :name="name"
    :display-name="displayName"
    :options="radioButtonsOptions!.map((value) => ({ value: value, label: value }))"
    :validation="validation"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";

export default defineComponent({
  name: "YesNoFormElement",
  components: { RadioButtonsFormElement },
  props: {
    name: {
      type: String,
      default: "",
    },
    displayName: {
      type: String,
      default: "",
    },
    radioButtonsOptions: {
      type: Array as () => Array<string>,
      default: () => ["Yes", "No"],
    },
    validation: {
      type: String,
      default: "",
    },
  },
});
</script>
