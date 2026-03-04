<template>
  <Dialog
    :visible="isOpen"
    modal
    header="Get in Touch"
    :closable="state !== 'submitting'"
    :dismissableMask="state !== 'submitting'"
    :style="{ width: '560px', maxWidth: '95vw' }"
    @update:visible="handleClose"
  >
    <div v-if="state === 'success'" class="inquiry-modal__success" aria-live="polite">
      <i class="pi pi-check-circle inquiry-modal__success-icon" aria-hidden="true" />
      <p class="inquiry-modal__success-text">Your message has been sent. We will be in touch soon.</p>
    </div>

    <template v-else>
      <div class="inquiry-modal__error" aria-live="assertive">
        <p v-if="state === 'error'" class="inquiry-modal__error-text">
          Something went wrong. Please try again.
        </p>
      </div>

      <FormKit
        type="form"
        :actions="false"
        :disabled="state === 'submitting'"
        :attrs="{ 'aria-busy': String(state === 'submitting') }"
        @submit="handleSubmit"
      >
        <FormKit
          type="text"
          name="contactName"
          label="Name"
          v-model="contactName"
          validation="required"
          validation-visibility="blur"
        />
        <FormKit
          type="text"
          name="organisation"
          label="Organisation (optional)"
        />
        <FormKit
          type="email"
          name="contactEmail"
          label="Email"
          v-model="contactEmail"
          validation="required|email"
          validation-visibility="blur"
        />
        <FormKit
          type="textarea"
          name="message"
          label="Message"
          v-model="message"
          validation="required"
          validation-visibility="blur"
          :rows="4"
        />
        <Button
          type="submit"
          label="SUBMIT"
          data-test="submit-button"
          class="inquiry-modal__submit"
          :loading="state === 'submitting'"
          :disabled="state === 'submitting' || !isFormValid"
        />
      </FormKit>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import axios from 'axios';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import { useContactModal } from '@/composables/useContactModal';

type ModalState = 'idle' | 'submitting' | 'success' | 'error';

interface InquiryFormValues {
  contactName: string;
  organisation?: string;
  contactEmail: string;
  message: string;
}

const { isOpen, closeModal } = useContactModal();
const state = ref<ModalState>('idle');

// Reactive field values for submit button validity tracking
const contactName = ref('');
const contactEmail = ref('');
const message = ref('');

const isFormValid = computed(
  () =>
    contactName.value.trim().length > 0 &&
    contactEmail.value.trim().length > 0 &&
    message.value.trim().length > 0,
);

function handleClose(): void {
  state.value = 'idle';
  contactName.value = '';
  contactEmail.value = '';
  message.value = '';
  closeModal();
}

async function handleSubmit(values: InquiryFormValues): Promise<void> {
  state.value = 'submitting';
  try {
    await axios.post('/community/inquiry', {
      contactName: values.contactName,
      organisation: values.organisation || undefined,
      contactEmail: values.contactEmail,
      message: values.message,
    });
    state.value = 'success';
    setTimeout(handleClose, 3000);
  } catch {
    state.value = 'error';
  }
}
</script>

<style scoped lang="scss">
.inquiry-modal {
  &__success {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
    padding: 2rem 0;
    text-align: center;
  }

  &__success-icon {
    font-size: 3rem;
    color: var(--p-primary-color, #ff6813);
  }

  &__success-text {
    font-size: 1.125rem;
    font-weight: 600;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__error {
    min-height: 1.5rem;
  }

  &__error-text {
    margin: 0 0 1rem;
    color: var(--p-red-500, #ef4444);
    font-size: 0.9375rem;
  }

  &__submit {
    margin-top: 1rem;
    width: 100%;
  }
}
</style>