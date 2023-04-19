<template>
  <Dropdown
    v-model="selection"
    :options="options"
    :placeholder="placeholder"
    option-label="label"
    option-value="value"
    :class="innerClass"
  />
  <FormKit
    type="text"
    :validation-label="displayName!"
    :validation="validation!"
    :name="name"
    v-model="selection"
    outer-class="hidden-input"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import Dropdown from "primevue/dropdown";
import { Option } from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";

export default defineComponent({
  name: "SingleSelectFormElement",
  components: { FormKit, Dropdown },
  data() {
    return {
      selection: "",
    };
  },
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
