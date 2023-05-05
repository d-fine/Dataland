<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName!" :explanation="info!" :is-required="required" />
    <MultiSelectFormElement
      :name="name"
      :display-name="displayName"
      :validation="validation"
      :validation-label="validationLabel ?? displayName"
      :placeholder="placeholder"
      :options="options"
      inner-class="short"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import { DropdownOption } from "@/utils/PremadeDropdownDatasets";
import MultiSelectFormElement from "@/components/forms/parts/elements/basic/MultiSelectFormElement.vue";

export default defineComponent({
  name: "MultiSelectFormField",
  components: { MultiSelectFormElement, UploadFormHeader },
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
    validationLabel: {
      type: String,
    },
    placeholder: {
      type: String,
      default: "",
    },
    options: {
      type: Array as () => Array<DropdownOption>,
      required: true,
    },
    required: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
