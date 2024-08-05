<template>
  <div data-test="activityFormElement">
    <ActivitySelector />
    <FinancialShareExtendedDataPointFormField
      name="share"
      label="Share"
      description="Relative and absolute share of the underlying cash flow"
    />

    <ObjectivesFormField />
    <div class="header-separator">
      <YesNoFormField
        name="minimumSafeguards"
        description="Are minimum social safeguards fulfilled?"
        label="Minimum Social Safeguards"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import FinancialShareExtendedDataPointFormField from '@/components/forms/parts/kpiSelection/FinancialShareExtendedDataPointFormField.vue';
import ObjectivesFormField from '@/components/forms/parts/kpiSelection/ObjectivesFormField.vue';
import ActivitySelector from '@/components/forms/parts/elements/derived/ActivitySelector.vue';
import YesNoFormField from '@/components/forms/parts/fields/YesNoFormField.vue';

export default defineComponent({
  name: 'AlignedActivitiesFormElements',
  components: {
    YesNoFormField,
    ActivitySelector,
    FinancialShareExtendedDataPointFormField,
    ObjectivesFormField,
  },
});
</script>
