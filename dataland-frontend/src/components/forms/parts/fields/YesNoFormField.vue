<template>
  <div :class="classes" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <CheckboxesListFormElement
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :validation-messages="validationMessages"
      :options="HumanizedYesNo"
      @update-checkbox-value="(newValue) => $emit('updateYesNoValue', newValue)"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import CheckboxesListFormElement from '@/components/forms/parts/elements/basic/CheckboxesListFormElement.vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { HumanizedYesNo } from '@/utils/YesNoNa';

export default defineComponent({
  name: 'YesNoFormField',
  computed: {
    HumanizedYesNo() {
      return HumanizedYesNo;
    },
  },
  components: { CheckboxesListFormElement, UploadFormHeader },
  props: {
    ...BaseFormFieldProps,
    validationMessages: {
      type: Object as () => { is: string },
    },
    classes: {
      type: String,
      default: 'form-field',
    },
  },
  emits: ['updateYesNoValue'],
});
</script>
