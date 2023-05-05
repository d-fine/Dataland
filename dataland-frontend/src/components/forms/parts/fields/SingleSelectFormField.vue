<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName!" :explanation="info!" :is-required="required" />
    <SingleSelectFormElement
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
import SingleSelectFormElement from "@/components/forms/parts/elements/basic/SingleSelectFormElement.vue";

export default defineComponent({
  name: "SingleSelectFormField",
  components: { SingleSelectFormElement, UploadFormHeader },
  data() {
    return {
      selection: "",
    };
  },
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
