<template>
  <FormKit type="group" :name="name">
    <div class="mb-3 form-field">
      <UploadFormHeader :label="label" :description="description" :is-required="required" />
      <div class="next-to-each-other">
        <FormKit
          type="text"
          name="relativeShareInPercent"
          validation-label="Relative Value"
          validation="number|between:0,100"
          placeholder="Relative Value in %"
          outer-class="short"
          data-test="relativeShareInPercent"
        />
        <FormKit type="group" name="absoluteShare">
          <FormKit
            type="text"
            name="amount"
            validation-label="Absolute Value"
            validation="number"
            placeholder="Absolute Value"
            outer-class="short"
            data-test="absoluteShareAmount"
          />
          <SingleSelectFormElement
            name="currency"
            validation-label="Currency"
            placeholder="Currency"
            :options="countryCodeOptions"
            data-test="absoluteShareCurrency"
          />
        </FormKit>
      </div>
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { FormKit } from '@formkit/vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets';
import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';

export default defineComponent({
  name: 'FinancialShareFormField',
  components: { SingleSelectFormElement, FormKit, UploadFormHeader },
  data() {
    return {
      countryCodeOptions: getDataset(DropdownDatasetIdentifier.CurrencyCodes),
    };
  },
  props: {
    ...BaseFormFieldProps,
  },
});
</script>
