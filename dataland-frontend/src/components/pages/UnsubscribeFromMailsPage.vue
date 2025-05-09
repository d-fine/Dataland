<template>
  <TheHeader :showUserProfileDropdown="false" />

  <TheContent class="container">
    <main>
      <div>
        <div v-if="!isUnsubscribed" class="subscribed">
          <h1>Please confirm to unsubscribe from our mailing list.</h1>
          <PrimeButton @click="unsubscribeFromMailingList" data-test="unsubscribeButton" class="unsubscribe-button">
            Unsubscribe
          </PrimeButton>
        </div>
        <div v-else>
          <p data-test="unsubscribeMessage" class="unsubscribed-confirmation">
            {{ message }}
          </p>
          <p>
            Go to
            <RouterLink to="/" class="text-primary" title="back to landing page">Dataland</RouterLink>
          </p>
        </div>
      </div>
    </main>
  </TheContent>

  <TheFooter />
</template>

<script lang="ts">
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import { defineComponent, inject } from 'vue';

export default defineComponent({
  name: 'UnsubscribeFromMailsPage',
  components: {
    TheFooter,
    TheContent,
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
      isUnsubscribed: false,
      message: '',
    };
  },

  methods: {
    /**
     * This function sends the subscriptionId to our backend
     */
    async unsubscribeFromMailingList() {
      try {
        const emailUnsubscribeApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
          .emailController;
        const response = await emailUnsubscribeApi.unsubscribeUuid(this.subscriptionId);
        if (response.data.includes('Successfully unsubscribed')) {
          this.message = 'You have been successfully removed from our mailing list.';
        } else {
          this.message = 'This UUID does not correspond to any email address in our mailing list.';
        }
        this.isUnsubscribed = true;
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>

<style lang="scss" scoped>
.container {
  position: absolute;
  height: 70vh;
  left: 0;
  top: 0;
  @media only screen and (max-width: 768px) {
    height: 60vh;
  }
}
main {
  position: absolute;
  left: 50%;
  top: 55%;
  transform: translate(-50%, -50%);
  @media only screen and (max-width: 768px) {
    top: 58%;
  }
}

.unsubscribe-button {
  font-size: 1.6rem;
  padding: 0.7rem 1rem;
}

h1,
.unsubscribed-confirmation {
  font-size: 1.3rem;
  font-weight: 500;
  @media only screen and (max-width: 768px) {
    font-size: 1rem;
  }
}
.unsubscribed-confirmation > span {
  text-decoration: underline;
}
.footer {
  position: absolute;
  width: 100vw;
  top: 70vh;
  left: 0;
  @media only screen and (max-width: 768px) {
    top: 60vh;
  }
}
</style>
