<template>
  <RadioButtonsForm
    :name="name"
    :info="info"
    :display-name="displayName"
    :radio-buttons-options="radioButtonsOptions!.map((value) => ({ value: value, label: value }))"
    :validation="validation"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import RadioButtonsForm from "@/components/forms/parts/RadioButtonsFormElement.vue";

export default defineComponent({
  name: "YesNoComponent",
  components: { RadioButtonsForm },
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
