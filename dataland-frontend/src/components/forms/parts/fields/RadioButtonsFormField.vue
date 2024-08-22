<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <CheckboxesListFormElement
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :options="options"
      :field-name="name"
    />
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { defineComponent } from 'vue';
import CheckboxesListFormElement from '@/components/forms/parts/elements/basic/CheckboxesListFormElement.vue';
import { OptionsFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'RadioButtonsFormField',
  components: { CheckboxesListFormElement, UploadFormHeader },
  props: OptionsFormFieldProps,
});
</script>
