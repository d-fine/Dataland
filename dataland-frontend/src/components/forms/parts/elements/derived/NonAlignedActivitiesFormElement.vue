<template>
  <div data-test="activityFormElement">
    <ActivitySelector />
    <div class="form-field">
      <FinancialShareFormField name="share" />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import FinancialShareFormField from '@/components/forms/parts/kpiSelection/FinancialShareFormField.vue';
import ActivitySelector from '@/components/forms/parts/elements/derived/ActivitySelector.vue';

export default defineComponent({
  name: 'NonAlignedActivitiesFormElement',
  components: {
    ActivitySelector,
    FinancialShareFormField,
  },
});
</script>
