<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName!" :explanation="info!" />
    <Dropdown
      v-model="selection"
      :options="options"
      :placeholder="placeholder"
      option-label="label"
      option-value="value"
      class="short"
    />
    <FormKit
      type="text"
      :validation-label="displayName!"
      :validation="validation!"
      :name="name"
      v-model="selection"
      outer-class="hidden-input"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import Dropdown from "primevue/dropdown";
import { Option } from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";

export default defineComponent({
  name: "SingleSelectFormElement",
  components: { UploadFormHeader, FormKit, Dropdown },
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
    placeholder: {
      type: String,
      default: "",
    },
    options: {
      type: Array as () => Array<Option>,
      required: true,
    },
  },
});
</script>
