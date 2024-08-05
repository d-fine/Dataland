<template>
  <FormKit type="group" :name="name">
    <div class="mb-3 form-field">
      <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
      <div>
        <PercentageExtendedDataPointFormField
          name="relativeShareInPercent"
          label="Relative Share"
          validation-label="Relative Value"
          validation="number|between:0,100"
          placeholder="Relative Value in %"
          outerClass="short"
          data-test="relativeShareInPercent"
        />
        <CurrencyDataPointFormField
          name="absoluteShare"
          label="Absolute Share"
          data-test="absoluteValue"
          :isDataPointToggleable="isDataPointToggleable"
          :placeholder="placeholder"
        />
      </div>
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { FormKit } from '@formkit/vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import PercentageExtendedDataPointFormField from '@/components/forms/parts/fields/PercentageExtendedDataPointFormField.vue';
import CurrencyDataPointFormField from '@/components/forms/parts/fields/CurrencyDataPointFormField.vue';

export default defineComponent({
  name: 'FinancialShareExtendedDataPointFormField',
  components: { CurrencyDataPointFormField, PercentageExtendedDataPointFormField, FormKit, UploadFormHeader },
  props: {
    ...BaseFormFieldProps,
    isDataPointToggleable: {
      type: Boolean,
      default: false,
    },
    placeholder: {
      type: String,
      default: 'Absolute Value',
    },
  },
});
</script>
