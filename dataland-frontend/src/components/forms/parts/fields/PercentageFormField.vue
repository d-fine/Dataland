<template>
  <div class="form-field">
    <UploadFormHeader :label="label !== '' ? `${label} (%)` : ''" :description="description" :is-required="required" />
    <div class="grid">
      <FormKit
        type="text"
        :name="name"
        :validation-label="validationLabel ?? label"
        :validation="`number|${validation}`"
        :placeholder="placeholder"
        v-model="percentageFieldValue"
        :outerClass="inputClass"
      >
      </FormKit>
      <div class="form-field-label pb-3">
        <span>%</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { defineComponent } from 'vue';
import { FormKit } from '@formkit/vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'PercentageFormField',
  components: { FormKit, UploadFormHeader },
  computed: {
    percentageFieldValue: {
      get(): string | undefined {
        return this.percentageFieldValueBind?.toString();
      },
      set(newValue: string) {
        this.$emit('update:percentageFieldValueBind', newValue);
      },
    },
  },
  created() {
      console.log(`PercentageFormField with ${this.name} created.`)
  },
  props: {
    ...BaseFormFieldProps,
    percentageFieldValueBind: {
      type: [String, Number],
    },
    placeholder: {
      type: String,
      default: 'Value in %',
    },
  },
});
</script>
