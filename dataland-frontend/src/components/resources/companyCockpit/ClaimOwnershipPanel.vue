<template>
  <div class="claim-panel">
    <div class="next-to-each-other vertical-middle">
      <h2 class="claim-panel__ownership-question">Responsible for {{ companyName }}?</h2>
      <a class="link" @click="toggleDialog">Claim company dataset ownership.</a>
    </div>
  </div>
  <ClaimOwnershipDialog
      :dialog-is-open="dialogIsOpen"
      :company-name="companyName"
      :company-id="companyId"
      :claim-is-submitted="claimIsSubmitted"
      @claim-submitted="onClaimSubmitted"
  />
</template>

<script lang="ts">
import {defineComponent} from "vue";
import ClaimOwnershipDialog from "@/components/resources/companyCockpit/ClaimOwnershipDialog.vue";

export default defineComponent({
  name: "CompanyCockpitPage",
  components: {
    ClaimOwnershipDialog
  },
  inject: {
    injectedUseMobileView: {
      from: "useMobileView",
      default: false,
    },
  },
  data() {
    return {
      dialogIsOpen: false,
      claimIsSubmitted: false,
    }
  },
  watch: {
    companyId(newCompanyId, oldCompanyId) {
      if (newCompanyId !== oldCompanyId) {
        this.dialogIsOpen = false;
        this.claimIsSubmitted = false;
      }
    }
  },
  props: {
    companyName: {
      type: String,
      required: true,
    },
    companyId: {
      type: String,
      required: true,
    }
  },
  methods: {
    onClaimSubmitted() {
      this.claimIsSubmitted = true;
    }
  }
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
