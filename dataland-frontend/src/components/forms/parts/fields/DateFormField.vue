<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <DateFormElement
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :placeholder="placeholder"
      :today-as-max="todayAsMax"
    />
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { defineComponent } from 'vue';
import DateFormElement from '@/components/forms/parts/elements/basic/DateFormElement.vue';
import { DateFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'DateFormField',
  components: { UploadFormHeader, DateFormElement },
  props: DateFormFieldProps,
});
</script>
