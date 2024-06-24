<template>
  <SingleSelectFormField
    name="riskPosition"
    label="Human Rights or Environmental Violations Definition"
    description="Please select the risk position of the violation."
    :options="riskOptions"
    placeholder="Select Risk"
    validation="required"
  />
  <YesNoFormField
    name="measuresTaken"
    @update-yes-no-value="handleValueUpdate"
    label="Counteracting Measures"
    description="Have measures been taken to address this violation?"
    validation="required"
  />
  <FreeTextFormField
    name="listedMeasures"
    v-if="counteractingMeasures"
    label="Which Counteracting Measures"
    description="Which measures have been applied to counteract this violation?"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import SingleSelectFormField from '@/components/forms/parts/fields/SingleSelectFormField.vue';
import YesNoFormField from '@/components/forms/parts/fields/YesNoFormField.vue';
import FreeTextFormField from '@/components/forms/parts/fields/FreeTextFormField.vue';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets';
import { convertYesNoUndefinedToBoolean } from '@/utils/YesNoNa';

export default defineComponent({
  name: 'GeneralViolationsAssessmentFormElement',
  data() {
    return {
      riskOptions: getDataset(DropdownDatasetIdentifier.RiskPositions),
      counteractingMeasures: undefined as boolean | undefined,
    };
  },
  components: {
    SingleSelectFormField,
    YesNoFormField,
    FreeTextFormField,
  },
  methods: {
    /**
     * Handles the update of the YesNoValue
     * @param yesNoValue the updated value
     */
    handleValueUpdate(yesNoValue: string | undefined) {
      this.counteractingMeasures = convertYesNoUndefinedToBoolean(yesNoValue);
    },
  },
});
</script>
