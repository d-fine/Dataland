<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName!" :explanation="info!" />
    <MultiSelect
      v-model="selections"
      :options="options"
      :placeholder="placeholder"
      option-label="label"
      option-value="value"
      :show-toggle-all="false"
      class="short"
    />
    <FormKit
      type="list"
      :validation-label="displayName!"
      :validation="validation!"
      :name="name"
      v-model="selections"
      outer-class="hidden-input"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import MultiSelect from "primevue/multiselect";
import { Option } from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";

export default defineComponent({
  name: "MultiSelectFormElement",
  components: { UploadFormHeader, FormKit, MultiSelect },
  data() {
    return {
      selections: [] as string[],
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
