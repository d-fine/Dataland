<template>
  <div
      class="claim-panel">
    <div class="next-to-each-other vertical-middle">
      <h2 class="claim-panel__ownership-question">Responsible for {{ companyName }}?</h2>
      <a class="link" @click="$emit('toggleDialog')">Claim dataset ownership.</a>
    </div>


  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";

export default defineComponent({
  name: "CompanyCockpitPage",
  components: {
    InputTextFormField,
  },
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
    dialogIsOpen: {
      type: Boolean,
      default: false,
    }
  },
  emits: ["toggleDialog"]
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
  cursor: pointer;

  &:hover {
    box-shadow: 0 0 32px 8px #1e1e1e14;

    .summary-panel__separator {
      border-bottom-color: var(--primary-color);
    }
  }

  &__ownership-question {
    font-size: 21px;
    font-weight: 700;
  }
}
</style>
