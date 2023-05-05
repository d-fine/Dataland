<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName!" :explanation="info!" :is-required="required" />
    <DateFormElement
      :name="name"
      :display-name="displayName"
      :validation="validation"
      :validation-label="validationLabel ?? displayName"
      :placeholder="placeholder"
      :today-as-max="todayAsMax"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import DateFormElement from "@/components/forms/parts/elements/basic/DateFormElement.vue";

export default defineComponent({
  name: "DateFormField",
  components: { UploadFormHeader, DateFormElement },
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
    todayAsMax: {
      type: Boolean,
      default: false,
    },
    required: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
