<template>
  <FormKit type="group" :name="name">
    <div class="mb-3">
      <div class="grid grid-nogutter">
        <PercentageFormField
          label="Climate Change Mitigation and Climate Change Adaptation"
          :description=mitigationAndAdaptation
          :is-required="false"
          name="mitigationAndAdaptation"
          validation-label="Relative Value"
          validation="number|between:0,100"
          placeholder="CCM and CCA"
          class="col"
        />
        <PercentageFormField
          label="Climate Change Mitigation"
          :description=mitigation
          :is-required="false"
          name="mitigation"
          validation-label="Relative Value"
          validation="number|between:0,100"
          placeholder="Climate Change Mitigation"
          class="col"
        />
        <PercentageFormField
          label="Climate Change Adaptation"
          :description=adaptation
          :is-required="false"
          name="adaptation"
          validation-label="Relative Value"
          validation="number|between:0,100"
          placeholder="Climate Change Adaptation"
          class="col"
        />
      </div>
    </div>
  </FormKit>
</template>


<script lang="ts">
import {defineComponent} from 'vue'
import {BaseFormFieldProps} from "@/components/forms/parts/fields/FormFieldProps";
import PercentageFormField from './PercentageFormField.vue';
import {
  nuclearAndGasTooltipMapping
} from '@/components/resources/frameworkDataSearch/nuclearAndGas/NuclearAndGasTooltipMapping';


export default defineComponent({
  name: "NuclearAndGasActivityField",
  data() {
    return {
      // activity: this.name
    }
  },
  created() {
      console.log("NuclearAndGasActivityField created.")
  },
  components: {PercentageFormField},
  props: {
    ...BaseFormFieldProps,
  },
  computed: {
    mitigationAndAdaptation() {
      this.getTooltipByObjective()
      return "mitigationAndAdaptation"
    },
    mitigation() {
      this.getTooltipByObjective()
      return "mitigation"
    },
    adaptation() {
      this.getTooltipByObjective()
      return "adaptation"
    }
  },
  methods: {
    getTooltipByObjective() {
      for (const key in nuclearAndGasTooltipMapping) {
        if (key === this.name) {
          console.log(nuclearAndGasTooltipMapping[key as keyof typeof nuclearAndGasTooltipMapping])
        }
      }
    }
  }
})
</script>
