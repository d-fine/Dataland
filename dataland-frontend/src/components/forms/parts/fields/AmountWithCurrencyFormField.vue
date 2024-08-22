<template>
  <div :class="classes" :data-test="name">
    <FormKit type="group" :name="name">
      <div class="grid">
        <div class="col-12">
          <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
        </div>
        <div class="col-4">
          <NumberFormField
            :name="'amount'"
            :validation-label="validationLabel"
            :validation="validation"
            :unit="unit"
            input-class="col-12"
          />
        </div>
        <div class="col-4">
          <SingleSelectFormElement
            name="currency"
            placeholder="Currency"
            :options="getDataset(DropdownDatasetIdentifier.CurrencyCodes)"
            data-test="AmountWithCurrencyFormField-currency"
          />
        </div>
      </div>
    </FormKit>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { FormKit } from '@formkit/vue';
import { FormFieldPropsWithPlaceholder } from '@/components/forms/parts/fields/FormFieldProps';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';
import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';

export default defineComponent({
  name: 'AmountWithCurrencyFormField',
  computed: {
    DropdownDatasetIdentifier() {
      return DropdownDatasetIdentifier;
    },
  },
  components: { SingleSelectFormElement, NumberFormField, UploadFormHeader, FormKit },
  props: {
    ...FormFieldPropsWithPlaceholder,
    unit: {
      type: String,
    },
    classes: {
      type: String,
      default: 'form-field',
    },
  },
  methods: {
    getDataset,
  },
});
</script>
