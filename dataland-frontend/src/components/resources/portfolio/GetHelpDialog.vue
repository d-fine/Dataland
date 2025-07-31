<template>
  <div class="container">
    <div>
      <p class="header-styling">Choose a topic</p>
      <Dropdown
        id="get-help-topic"
        v-model="topic"
        :options="topics"
        optionLabel="name"
        placeholder="Where do you need help with?"
      />
    </div>
    <div>
      <p class="header-styling">Your message to us</p>
      <Textarea
        id="get-help-message"
        v-model="message"
        placeholder="State for which companies you need help finding identifiers so that you can add them to your portfolio"
        rows="5"
        fluid
      />
    </div>
    <Message v-if="emailSendingError" severity="error">
      {{ emailSendingError }}
    </Message>
    <Message v-if="emailSendingSuccess" severity="success">
      {{ emailSendingSuccess }}
    </Message>
    <Message v-if="!isValidMessage" severity="error" variant="simple" size="small"
      >Please choose a topic and enter a message to us.
    </Message>
    <PrimeButton
      label="Send"
      icon="pi pi-send"
      class="send-button"
      @click="sendEmail"
      :loading="isSendingMail"
      :disabled="!isValidMessage"
    />
  </div>
</template>

<script setup lang="ts">
import Textarea from 'primevue/textarea';
import Dropdown from 'primevue/dropdown';
import PrimeButton from 'primevue/button';
import { computed, inject, ref } from 'vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const isSendingMail = ref(false);
const message = ref();

import type { SupportRequestData } from '@clients/userservice';
import Message from 'primevue/message';
import { AxiosError } from 'axios';

const emailSendingError = ref('');
const emailSendingSuccess = ref('');
const topic = ref();
const topics = ref([
  { name: 'Find company identifiers', code: 'identifiers' },
  { name: 'Other topic', code: 'Other' },
]);

const isValidMessage = computed(() => message.value && topic.value.name);

/**
 * Send an email to request support
 */
async function sendEmail(): Promise<void> {
  const supportRequest: SupportRequestData = { topic: topic.value.name, message: message.value };
  try {
    isSendingMail.value = true;
    await apiClientProvider.apiClients.portfolioController.postSupportRequest(supportRequest);
    emailSendingSuccess.value = 'Thank you for contacting us. We have received your request.';
  } catch (error) {
    emailSendingError.value = error instanceof AxiosError ? error.message : 'An unknown error occurred.';
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
  gap: 0.5rem;
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
  font-weight: 700;
}

.send-button {
  align-self: end;
}
</style>
