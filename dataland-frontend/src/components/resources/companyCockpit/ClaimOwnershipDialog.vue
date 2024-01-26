<template>
  <PrimeDialog
      id="claimOwnerShipDialog"
      :dismissable-mask="true"
      :modal="true"
      header="Header"
      footer="Footer"
      class="col-6"
      v-model:visible="dialogIsVisible"
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
          @click="$emit('toggleDialog')"
      />
    </template>
  </PrimeDialog>
</template>

<script>
import PrimeDialog from "primevue/dialog";
import PrimeButton from "primevue/button";

export default {
  name: "ClaimOwnershipDialog",
  components: {
    PrimeDialog,
    PrimeButton
  },
  data() {
    return {
      claimIsSubmitted: false,
      claimOwnershipMessage: "",
      dialogIsVisible: false,
    }

  },
  props: {
    dialogIsOpen: {
      type: Boolean,
      required: false,
      default: false
    },
    companyName: {
      type: String,
      required: true
    }
  },
  emits: ["toggleDialog"],
  methods: {
    submitInput() {
      // here has to happen interaction with the api
      this.claimIsSubmitted = true;
    },
  },
  watch: {
    dialogIsOpen(newValue) {
      this.dialogIsVisible = newValue;
    }
  }
}
</script>

<style scoped>

</style>