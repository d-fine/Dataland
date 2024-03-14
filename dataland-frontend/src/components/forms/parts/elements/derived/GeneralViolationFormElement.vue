<template>
  <SingleSelectFormField
    name="nameOfGeneralViolation"
    label="Human Rights or Environmental Violations Definition"
    description="Please select the risk position of the violation."
    :options="riskOptions"
  />
  <YesNoFormField
    name="nameCounteractingMeasures"
    label="Counteracting Measures"
    description="Have measures been taken to address this violation?"
  />
  <FreeTextFormField
    name="nameWhichCounteractingMeasures"
    label="Which Counteracting Measures"
    description="Which measures have been applied to counteract this violation?"
  />
</template>

<script lang="ts">
import { RiskPositionType } from "@clients/backend";
import { defineComponent } from "vue";
import SingleSelectFormField from "@/components/forms/parts/fields/SingleSelectFormField.vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";

export default defineComponent({
  name: "GeneralViolationFormElement",
  data() {
    return {
      riskOptions: RiskPositionType,
    };
  },
  components: {
    SingleSelectFormField,
    YesNoFormField,
    FreeTextFormField,
  },
});
</script>
