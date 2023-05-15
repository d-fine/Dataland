<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName" :explanation="info" :is-required="required" />
    <DateFormElement
      :name="name"
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
import { DateFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "DateFormField",
  components: { UploadFormHeader, DateFormElement },
  props: DateFormFieldProps,
});
</script>
