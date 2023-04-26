<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName!" :explanation="info!" :is-required="required" />
    <RadioButtonsFormElement :name="name" :display-name="displayName" :validation="validation" :options="options" />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import { Option } from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";

export default defineComponent({
  name: "RadioButtonsFormField",
  components: { RadioButtonsFormElement, UploadFormHeader },
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
    options: {
      type: Array as () => Array<Option>,
      required: true,
    },
    required: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
