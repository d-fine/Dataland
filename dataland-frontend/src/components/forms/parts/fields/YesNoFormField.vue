<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName!" :explanation="info!" />
    <YesNoFormElement
      :name="name"
      :display-name="displayName"
      :validation="validation"
      :radioButtonsOptions="radioButtonsOptions"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import YesNoFormElement from "@/components/forms/parts/elements/derived/YesNoFormElement.vue";

export default defineComponent({
  name: "YesNoFormField",
  components: { YesNoFormElement, UploadFormHeader },
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
    validation: {
      type: String,
      default: "",
    },
    radioButtonsOptions: {
      type: Array as () => Array<string>,
      default: () => ["Yes", "No"],
    },
  },
});
</script>
