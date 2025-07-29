<template>
  <div>
    <div class="flex items-center gap-4 mb-8">
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
    <div class="flex items-center gap-4 mb-8">
      <label for="message" class="formkit-label">Your message to us</label>
      <Textarea id="message" v-model="message" rows="5" cols="30" />
    </div>
    <PrimeButton
      type="button"
      label="Send"
      @click="sendEmail"
      class="primary-button"
      :loading="isSendingMail"
      style="margin-left: 1em; float: right"
    />
  </div>
</template>

<script setup lang="ts">
import Textarea from 'primevue/textarea';
import Dropdown from 'primevue/dropdown';
import PrimeButton from 'primevue/button';
import { inject } from 'vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const isSendingMail = ref(false);
const message = ref('');
import { ref } from 'vue';

const topic = ref();
const topics = ref([
  { name: 'Find identifiers', code: 'identifiers' },
  { name: 'Other topic', code: 'Other' },
]);

/**
 * Add identifiers from companyIdentifierInput to list. Invalid Identifiers remain in the input text field.
 */
async function sendEmail(): Promise<void> {
  try {
    isSendingMail.value = true;
    await apiClientProvider.backendClients.companyDataController.postCompanyValidation([message.value]);
  } catch (error) {
    console.log(error);
  } finally {
    isSendingMail.value = false;
  }
}
</script>
