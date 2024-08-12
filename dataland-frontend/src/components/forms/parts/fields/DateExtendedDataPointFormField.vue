<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    :input-class="inputClass"
    :check-value-validity="hasDataPointProperValue"
    :isDataPointToggleable="isDataPointToggleable"
  >
    <div class="mb-2">
      <DateFormField
        :name="'value'"
        :validation-label="validationLabel"
        :validation="validation"
        :placeholder="placeholder"
        :today-as-max="todayAsMax"
        :input-class="inputClass"
      />
    </div>
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { DateFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import ExtendedDataPointFormField from '@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue';
import DateFormField from '@/components/forms/parts/fields/DateFormField.vue';
import { hasDataPointProperValue } from '@/utils/DataPoint';

export default defineComponent({
  name: 'DateExtendedDataPointFormField',
  components: { DateFormField, ExtendedDataPointFormField },
  props: {
    ...DateFormFieldProps,
    isDataPointToggleable: {
      type: Boolean,
      default: false,
    },
  },
  methods: { hasDataPointProperValue },
});
</script>
