<template>
  <FormKit
    type="select"
    :validation-label="displayName!"
    :validation="validation!"
    :placeholder="placeholder"
    :name="name"
    :options="options"
    :inner-class="innerClass"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { Option } from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";

export default defineComponent({
  name: "SingleSelectFormElement",
  components: { FormKit },
  props: {
    name: {
      type: String,
      required: true,
    },
    displayName: {
      type: String,
      default: "",
    },
    validation: {
      type: String,
      default: "",
    },
    placeholder: {
      type: String,
      default: "",
    },
    options: {
      type: Array as () => Array<Option>,
      required: true,
    },
    innerClass: {
      type: String,
      default: "",
    },
  },
});
</script>
