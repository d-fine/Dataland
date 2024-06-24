<template>
  <SingleSelectFormField
    name="riskPosition"
    label="Identified Risk"
    description="Which risks were specifically identified in the risk analysis?"
    :options="riskOptions"
    placeholder="Select Risk"
    validation="required"
  />
  <YesNoFormField
    name="measuresTaken"
    @update-yes-no-value="handleValueUpdate"
    label="Counteracting Measures"
    description="Have measures been defined to counteract the risk?"
    data-test="counteractingMeasures"
    validation="required"
  />
  <FreeTextFormField
    name="listedMeasures"
    v-if="counteractingMeasures"
    label="Which Counteracting Measures"
    description="Which measures have been applied to counteract the risk?"
    data-test="listedMeasures"
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
  name: 'RiskAssessmentFormElement',
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
