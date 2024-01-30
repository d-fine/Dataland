<template>
  <div class="claim-panel">
    <div class="next-to-each-other vertical-middle">
      <h2 class="claim-panel__ownership-question">Responsible for {{ companyName }}?</h2>
      <a class="link" @click="$emit('toggleDialog')">Claim company dataset ownership.</a>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "CompanyCockpitPage",
  inject: {
    injectedUseMobileView: {
      from: "useMobileView",
      default: false,
    },
  },
  props: {
    companyName: {
      type: String,
      required: true,
    },
  },
  emits: ["toggleDialog"],
});
</script>

<style scoped lang="scss">
.claim-panel {
  grid-column: 1 / -1;
  min-height: 100px;
  background-color: var(--surface-card);
  padding: $spacing-md;
  border-radius: $radius-xxs;
  text-align: left;
  box-shadow: 0 0 12px var(--gray-300);

  &__ownership-question {
    font-size: 21px;
    font-weight: 700;
  }
}
</style>
