<template>
  <div
      class="claim-panel"
      @fetchedCompanyInformation="onFetchedCompanyInformation"
  >
    <div class="next-to-each-other vertical-middle">
      <h2 class="claim-panel__ownership-question">Responsible for {{ companyName }}?</h2>
      <a class="link" @click="toggleDialog" v-if="!claimIsSubmitted">Claim dataset ownership.</a>
      <p v-else>Dataset ownership claimed.</p>
    </div>

    <PrimeDialog
        id="claimOwnerShipDialog"
        :dismissable-mask="true"
        :modal="true"
        header="Header"
        footer="Footer"
        class="col-6"
        v-model:visible="dialogIsOpen"
    >

      <template #header>
        <h2 v-if="!claimIsSubmitted" class="m-0">Claim dataset ownership for your company.</h2>
        <h2 v-else class="m-0">Thank you for claiming data ownership for {{ companyName }}.</h2>
      </template>

      <div v-if="!claimIsSubmitted">
        <p>Are you responsible for the datasets of {{ companyName }}? Claim dataset ownership in order to ensure high
          quality and transparency over your company's data.</p>
        <p>Feel free to share any additional information with us:</p>
        <FormKit v-model="claimOwnershipMessage"
                 type="textarea"
                 name="claimOwnershipMessage"
                 placeholder="Write your message."
                 class="w-full p-inputtext "

        />
      </div>
      <div v-else>
        <p> We will reach out to you soon via email.</p>
      </div>
      <template #footer v-if="!claimIsSubmitted">
        <PrimeButton
            label="SUBMIT"
            @click="submitInput"
        />
      </template>
      <template #footer v-else>
        <PrimeButton
            label="CLOSE"
            @click="toggleDialog"
        />
      </template>
    </PrimeDialog>

  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import {CompanyInformation} from "@clients/backend";
import PrimeDialog from "primevue/dialog";
import PrimeButton from "primevue/button";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import InputText from "primevue/inputtext";

export default defineComponent({
  name: "CompanyCockpitPage",
  components: {
    InputTextFormField,
    PrimeDialog,
    PrimeButton,
    InputText
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
