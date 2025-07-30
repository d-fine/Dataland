<template>
  <div>
    <div class="flex flex-col gap-2">
      <label for="topic" class="formkit-label">Choose a topic</label>
      <div class="card flex justify-content-center">
        <Dropdown
          id="topic"
          v-model="topic"
          :options="topics"
          optionLabel="name"
          placeholder="Where do you need help with?"
          class="w-full md:w-14rem"
        />
      </div>
    </div>
    <div class="flex flex-col gap-2">
      <label for="message" class="formkit-label">Your message to us</label>
      <Textarea id="message" v-model="message" placeholder="Your message" rows="5" cols="30" />
    </div>
    <Message v-if="emailSendingError" severity="error" class="m-0" :life="3000">
      {{ emailSendingError }}
    </Message>
    <Message v-if="emailSendingSuccess" severity="success" class="m-0" :life="3000">
      {{ emailSendingSuccess }}
    </Message>
    <p v-if="!isValidMessage" class="formkit-message">Please choose a topic and enter a message to us.</p>
    <PrimeButton
      type="button"
      label="Send"
      @click="sendEmail"
      class="primary-button"
      :loading="isSendingMail"
      :disabled="!isValidMessage"
      style="margin-left: 1em; float: right"
    />
  </div>
</template>

<script setup lang="ts">
import Textarea from 'primevue/textarea';
import Dropdown from 'primevue/dropdown';
import PrimeButton from 'primevue/button';
import { computed, inject } from 'vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const isSendingMail = ref(false);
const message = ref();
import { ref } from 'vue';
import type { SupportRequestData } from '@clients/userservice';
import Message from 'primevue/message';
import { AxiosError } from 'axios';

const emailSendingError = ref('');
const emailSendingSuccess = ref('');
const topic = ref();
const topics = ref([
  { name: 'Find identifiers', code: 'identifiers' },
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
