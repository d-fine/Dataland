<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    :input-class="inputClass"
    :check-value-validity="hasDataPointProperValue"
  >
    <FormListFormField
      name="value"
      :required="required"
      :validation="validation"
      :validation-label="validationLabel"
      sub-form-component="AlignedActivitiesFormElements"
      data-test-add-button="addNewProductButton"
      label-add-button="ADD NEW Activity"
      data-test-sub-form="alignedActivitiesSection"
    />
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import FormListFormField from '@/components/forms/parts/fields/FormListFormField.vue';
import ExtendedDataPointFormField from '@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue';
import { hasDataPointProperValue } from '@/utils/DataPoint';

export default defineComponent({
  name: 'AlignedActivitiesFormField',
  methods: { hasDataPointProperValue },
  props: {
    ...BaseFormFieldProps,
  },
  components: {
    ExtendedDataPointFormField,
    FormListFormField,
  },
});
</script>
