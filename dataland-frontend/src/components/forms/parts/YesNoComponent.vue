<template>
  <RadioButtonsForm
    :name="name"
    :info="info"
    :display-name="displayName"
    :radio-buttons-options="radioButtonsOptions!.map((value) => ({ value: value, label: value } as Option))"
    :validation="validation"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import RadioButtonsForm from "@/components/forms/parts/RadioButtonsForm.vue";

export default defineComponent({
  name: "YesNoComponent",
  data() {
    return {
      Option,
    };
  },
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
