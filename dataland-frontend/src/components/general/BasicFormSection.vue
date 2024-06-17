<template>
  <div :data-test="dataTest" class="basic-form-section bg-white radius-1 p-4">
    <h4 class="p-0 pb-4 mt-2 border-bottom">{{ header }}</h4>
    <div class="content p-0 m-0"><slot /></div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'BasicFormSection',
  props: {
    header: {
      type: String,
      required: true,
    },
    dataTest: {
      type: String,
      default: 'basic-form-section',
    },
  },
});
</script>

<style scoped lang="scss">
div.basic-form-section {
  margin-bottom: 1.5rem;

  &:last-of-type {
    margin-bottom: 0;
  }
}

.border-bottom {
  border-bottom: 1px solid #d9d9d9;
}

.content {
  padding-block: inherit;
}
</style>
