<template>
  <FormKit type="group" :name="name">
    <div>
      <UploadFormHeader
        :label="label !== '' ? `${label} (%)` : ''"
        :description="description"
        :is-required="required"
      />
      <div class="grid grid-nogutter">
        <PercentageFormField
          :is-required="false"
          name="mitigationAndAdaptation"
          validation-label="Relative Value"
          validation="number|between:0,100"
          placeholder="CCM and CCA"
          class="col"
          input-class="col-10"
        />
        <PercentageFormField
          :is-required="false"
          name="mitigation"
          validation-label="Relative Value"
          validation="number|between:0,100"
          placeholder="Climate Change Mitigation"
          class="col"
          input-class="col-10"
        />
        <PercentageFormField
          :is-required="false"
          name="adaptation"
          validation-label="Relative Value"
          validation="number|between:0,100"
          placeholder="Climate Change Adaptation"
          class="col"
          input-class="col-10"
        />
      </div>
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import PercentageFormField from './PercentageFormField.vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';

export default defineComponent({
  name: 'NuclearAndGasActivityField',
  components: { UploadFormHeader, PercentageFormField },
  props: {
    ...BaseFormFieldProps,
    name: {
      type: String,
      required: true,
    },
  },
});
</script>
