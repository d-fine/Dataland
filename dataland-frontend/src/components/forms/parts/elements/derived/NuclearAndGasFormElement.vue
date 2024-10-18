<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    :input-class="inputClass"
  >
    <FormKit type="group" name="value">
      <div v-for="activity in activities" :key="activity.key">
        <div v-if="isNonEligible">
          <div class="grid grid-nogutter">
            <PercentageFormField
              :is-required="false"
              :name="activity.key"
              :label="activity.label"
              :description="activity.description"
              validation-label="Relative Value"
              validation="number|between:0,100"
              class="col"
              input-class="col-10"
            />
          </div>
        </div>
        <NuclearAndGasActivityField
          v-else
          :name="activity.key"
          :label="activity.label"
          :description="activity.description"
        />
      </div>
    </FormKit>
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import NuclearAndGasActivityField from '@/components/forms/parts/fields/NuclearAndGasActivityField.vue';
import ExtendedDataPointFormField from '@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import { nuclearAndGasTooltipMapping } from '@/components/resources/frameworkDataSearch/nuclearAndGas/NuclearAndGasTooltipMapping';
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';

export default defineComponent({
  name: 'NuclearAndGasFormElement',
  components: { PercentageFormField, ExtendedDataPointFormField, NuclearAndGasActivityField },
  props: {
    ...BaseFormFieldProps,
  },
  computed: {
    activities() {
      if (this.name?.toLowerCase().includes('denominator')) {
        return nuclearAndGasTooltipMapping.alignedDenominator;
      } else if (this.name?.toLowerCase().includes('numerator')) {
        return nuclearAndGasTooltipMapping.alignedNumerator;
      } else if (this.name?.toLowerCase().includes('aligned')) {
        return nuclearAndGasTooltipMapping.eligibleButNotAligned;
      } else {
        return nuclearAndGasTooltipMapping.nonEligible;
      }
    },
    isNonEligible() {
      return this.activities === nuclearAndGasTooltipMapping.nonEligible;
    },
  },
});
</script>
