<template>
  <div
    class="claim-panel"
    @fetchedCompanyInformation="onFetchedCompanyInformation"
  >
    <div class="next-to-each-other vertical-middle">
      <h2 class="claim-panel__ownership-question">Responsible for {{ companyName }}?</h2>
      <a class="link" @click="$dialog.open(ClaimDatasetOwnerShipDialog)" v-if="!claimIsSubmitted">Claim dataset ownership.</a>
      <p v-else>Dataset ownership claimed.</p>
    </div>

    <ClaimDatasetOwnerShipDialog v-if="dialogIsOpen">
      <div v-if="!claimIsSubmitted">
        <h1>Claim dataset ownership for your company.</h1>
        <button @click="toggleDialog"></button>
        <div>
          <p>
            Are you responsible for the datasets of {{ companyName }}? Claim dataset ownership in order to ensure high
            quality and transparency over your company's data.
          </p>
          <p>Feel free to share any additional information with us:</p>
        </div>
        <input v-model="claimOwnershipMessage" placeholder="Write your message here." />

        <button @click="submitInput">SUBMIT</button>
      </div>
      <div v-else>
        <h1>Thank you for claiming data ownership for {{ companyName }}.</h1>
        <p>We will reach out to you soon via email.</p>
        <button @click="toggleDialog">CLOSE</button>
      </div>
    </ClaimDatasetOwnerShipDialog>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import ClaimDatasetOwnerShipDialog from "@/components/general/ClaimDatasetOwnerShipDialog.vue";
import { CompanyInformation } from "@clients/backend";

export default defineComponent({
  name: "CompanyCockpitPage",
  computed: {
    ClaimDatasetOwnerShipDialog() {
      return ClaimDatasetOwnerShipDialog
    }
  },
  components: { ClaimDatasetOwnerShipDialog },
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
  data() {
    return {
      dialogIsOpen: false,
      claimOwnershipMessage: "",
      claimIsSubmitted: false,
    };
  },
  methods: {
    claimOwnership() {
      this.dialogIsOpen = true;
    },
    toggleDialog() {
      this.dialogIsOpen = !this.dialogIsOpen;
    },
    submitInput() {
      // here has to happen interaction with the api
      this.claimIsSubmitted = true;
    },
    /**
     * On fetched company information defines the companyName
     * @param companyInfo company information from which the company name can be retrieved
     */
    onFetchedCompanyInformation(companyInfo: CompanyInformation): void {
      this.companyName = companyInfo.companyName;
    },
  },
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
