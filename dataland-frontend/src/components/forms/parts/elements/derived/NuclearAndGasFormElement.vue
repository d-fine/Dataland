<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    :input-class="inputClass"
  >
    <FormKit type="group" :name="name">
      <div v-for="activity in activities" :key="activity.key">
        <NuclearAndGasActivityField :name="name + '.value.' + activity.key" :label="activity.label" :description="activity.description"/>
      </div>
    </FormKit>
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import NuclearAndGasActivityField from "@/components/forms/parts/fields/NuclearAndGasActivityField.vue";
import ExtendedDataPointFormField from "@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue";
import {BaseFormFieldProps} from "@/components/forms/parts/fields/FormFieldProps";
import {
  nuclearAndGasTooltipMapping
} from '@/components/resources/frameworkDataSearch/nuclearAndGas/NuclearAndGasTooltipMapping';

export default defineComponent({
  created() {
    console.log("NuclearAndGasFormElement created.")
    console.log(this.name)
  },
  name: "NuclearAndGasFormElement",
  components: {ExtendedDataPointFormField, NuclearAndGasActivityField},
  props: {
    ...BaseFormFieldProps
  },
  computed: {
    activities() {
      if (this.name?.toLowerCase().includes("denominator")) {
        return nuclearAndGasTooltipMapping.alignedDenominator
      } else if (this.name?.toLowerCase().includes("numerator")) {
        return nuclearAndGasTooltipMapping.alignedNumerator
      } else if (this.name?.toLowerCase().includes("aligned")) {
        return nuclearAndGasTooltipMapping.eligibleButNotAligned
      } else {
        return nuclearAndGasTooltipMapping.nonEligible
      }
    }
  }
})
</script>
