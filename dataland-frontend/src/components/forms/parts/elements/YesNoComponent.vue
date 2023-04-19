<template>
  <RadioButtonsFormElement
    :name="name"
    :info="info"
    :display-name="displayName"
    :radio-buttons-options="radioButtonsOptions!.map((value) => ({ value: value, label: value }))"
    :validation="validation"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import RadioButtonsFormElement from "@/components/forms/parts/elements/RadioButtonsFormElement.vue";

export default defineComponent({
  name: "YesNoComponent",
  components: { RadioButtonsFormElement },
  props: {
    name: {
      type: String,
      default: "",
    },
    info: {
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
