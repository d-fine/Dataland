<template>
  <TheHeader :showUserProfileDropdown="false" />

  <main>
    <div>
      <div v-if="isSubscribed" class="subscribed">
        <h1>We are sorry you want to unsubscribe from our mailing list.</h1>

        <!--  <h1>Die uuid ist: {{ subscriptionId }}</h1> -->
        <PrimeButton @click="unsubscribeFromMailingList" class="unsubscribe-button"> Unsubscribe </PrimeButton>
      </div>

      <p v-else class="unsubscribed-confirmation">
        <span> {{ mailAddress }} </span> <br />
        has successfully been removed from our mailing list.
      </p>
    </div>
  </main>
</template>

<script lang="ts">
import { defineComponent, inject, ref } from 'vue';
import PrimeButton from 'primevue/button';
import type Keycloak from 'keycloak-js';
import TheHeader from '@/components/generics/TheHeader.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';

export default defineComponent({
  name: 'UnsubscribeFromMailsPage',
  components: {
    TheHeader,
    PrimeButton,
  },

  props: {
    subscriptionId: {
      type: String,
      required: true,
    },
  },

  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },

  data() {
    return {
      isSubscribed: true,
      mailAddress: '',
    };
  },

  methods: {
    /**
     * This function sends the subscriptionId to our backend and receives the corresponding email address
     */
    async unsubscribeFromMailingList() {
      try {
        const emailUnsubscribeApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
          .emailController;
        const response = await emailUnsubscribeApi.unsubscribeUuid(this.subscriptionId);
      } catch (error) {
        console.error(error);
      }
      /*
      send http post-request : subscriptionId

      get email-address that belongs to subscriptionId
      store as string in this.mailAddress

       */
      this.mailAddress = 'mail-address@example.com';

      this.changePageContent();
    },

    /* async searchCompanyName(autoCompleteCompleteEvent: AutoCompleteCompleteEvent) {
      try {
        const companyDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).backendClients
            .companyDataController;
        const response = await companyDataControllerApi.getCompaniesBySearchString(
            autoCompleteCompleteEvent.query,
            this.resultLimit
        );
        this.autocompleteArray = response.data;
      } catch (error) {
        console.error(error);
      }
    },
 */

    /**
     * This function sets the variable used for the conditional rendering of the page content to false
     */
    changePageContent() {
      this.isSubscribed = false;
    },
  },
});
</script>

<style lang="scss" scoped>
main {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.unsubscribe-button {
  font-size: 1.6rem;
  padding: 0.7rem 1rem;
}

h1,
.unsubscribed-confirmation {
  font-size: 1.3rem;
  font-weight: 500;
}
.unsubscribed-confirmation > span {
  text-decoration: underline;
}
</style>
