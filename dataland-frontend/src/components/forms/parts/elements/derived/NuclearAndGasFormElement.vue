<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    :input-class="inputClass"
  >
    <FormKit type="group">
      <div data-test="activityFormElement">
        <NuclearAndGasActivityField name="taxonomyAlignedShareDenominatorNAndG426" v-for="activity in activities"/>
      </div>
    </FormKit>
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import NuclearAndGasActivityField from "@/components/forms/parts/fields/NuclearAndGasActivityField.vue";
import ExtendedDataPointFormField from "@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue";
import {BaseFormFieldProps} from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  created() {
      console.log("NuclearAndGasFormElement created.")
  },
  name: "NuclearAndGasFormElement",
  components: {ExtendedDataPointFormField, NuclearAndGasActivityField},
  props: {
    ...BaseFormFieldProps
  },
  computed: {
    activities() {
      if(this.name?.includes("aligned") && this.name?.includes("Denominator")) {
        return
      }
    }
  }
})
</script>
