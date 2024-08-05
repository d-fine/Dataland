<template>
  <div data-test="activityFormElement">
    <ActivitySelector />
    <div class="form-field">
      <FinancialShareExtendedDataPointFormField name="share" />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import FinancialShareExtendedDataPointFormField from '@/components/forms/parts/kpiSelection/FinancialShareExtendedDataPointFormField.vue';
import ActivitySelector from '@/components/forms/parts/elements/derived/ActivitySelector.vue';

export default defineComponent({
  name: 'NonAlignedActivitiesFormElement',
  components: {
    ActivitySelector,
    FinancialShareExtendedDataPointFormField,
  },
});
</script>
