<template>
  <div :class="classes" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <CheckboxesListFormElement
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :options="HumanizedYesNoNa"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import CheckboxesListFormElement from '@/components/forms/parts/elements/basic/CheckboxesListFormElement.vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { HumanizedYesNoNa } from '@/utils/YesNoNa';

export default defineComponent({
  name: 'YesNoNaFormField',
  computed: {
    HumanizedYesNoNa() {
      return HumanizedYesNoNa;
    },
  },
  components: { CheckboxesListFormElement, UploadFormHeader },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
    classes: {
      type: String,
      default: 'form-field',
    },
  },
});
</script>
