<template>
  <div class="container">
    <div>
      <p class="header-styling">Choose a topic</p>
      <Select
        id="get-help-topic"
        v-model="selectedTopic"
        :options="availableTopics"
        optionLabel="name"
        placeholder="Where do you need help with?"
      />
    </div>
    <div>
      <p class="header-styling">Your message to us</p>
      <Textarea
        id="get-help-message"
        v-model="message"
        placeholder="Please state for which companies you need help finding identifiers to add them to your portfolio"
        rows="5"
        fluid
      />
    </div>
    <Message v-if="emailSendingError" severity="error">
      {{ emailSendingMessage }}
    </Message>
    <Message v-if="emailSendingSuccess" severity="success">
      {{ emailSendingMessage }}
    </Message>
    <Message v-if="!isValidForm" severity="error" variant="simple" size="small"
      >Please choose a topic and enter a message to us.
    </Message>
    <PrimeButton
      label="Send"
      icon="pi pi-send"
      class="send-button"
      @click="sendEmail"
      :loading="isSendingMail"
      :disabled="!isValidForm || emailSendingSuccess || isSendingMail"
    />
  </div>
</template>

<script setup lang="ts">
import Textarea from 'primevue/textarea';
import Select from 'primevue/select';
import PrimeButton from 'primevue/button';
import { computed, inject, ref } from 'vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import type { SupportRequestData } from '@clients/userservice';
import Message from 'primevue/message';
import { AxiosError } from 'axios';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const isSendingMail = ref(false);
const message = ref('');
const emailSendingError = ref<boolean>();
const emailSendingSuccess = ref<boolean>();
const emailSendingMessage = ref('');
const availableTopics = ref([{ name: 'Find company identifiers' }, { name: 'Other topic' }]);
const selectedTopic = ref<{ name: string } | undefined>();

const isValidForm = computed(() => message.value && selectedTopic.value?.name);

/**
 * Send an email to request support
 */
async function sendEmail(): Promise<void> {
  if (!selectedTopic.value?.name || !message.value) {
    return;
  }
  const supportRequest: SupportRequestData = { topic: selectedTopic.value?.name, message: message.value };
  try {
    isSendingMail.value = true;
    await apiClientProvider.apiClients.portfolioController.postSupportRequest(supportRequest);
    emailSendingError.value = false;
    emailSendingSuccess.value = true;
    emailSendingMessage.value = 'Thank you for contacting us. We have received your request.';
  } catch (error) {
    emailSendingError.value = true;
    emailSendingSuccess.value = false;
    emailSendingMessage.value = error instanceof AxiosError ? error.message : 'An unknown error occurred.';
    console.log(error);
  } finally {
    isSendingMail.value = false;
  }
}
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  justify-content: center;
}

#get-help-message {
  resize: none;
  width: 100%;
}

#get-help-topic {
  width: 100%;
}

.header-styling {
  font-weight: var(--font-weight-bold);
}

.send-button {
  align-self: end;
}
</style>
