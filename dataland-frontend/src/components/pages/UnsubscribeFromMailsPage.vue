<template>
  <TheHeader :showUserProfileDropdown="false" />

  <TheContent class="container">
    <main>
      <div>
        <div v-if="!isUnsubscribed" class="subscribed">
          <h1>We are sorry you want to unsubscribe from our mailing list.</h1>
          <PrimeButton @click="unsubscribeFromMailingList" class="unsubscribe-button"> Unsubscribe </PrimeButton>
        </div>
        <div v-else>
          <p class="unsubscribed-confirmation">
            {{ message }}
          </p>
          <p>
            Go to
            <router-link to="/" class="text-primary" title="back to landing page">Dataland</router-link>
          </p>
        </div>
      </div>
    </main>
  </TheContent>

  <TheNewFooter :is-light-version="true" :sections="footerPageSections" class="footer" />
</template>

<script lang="ts">
import { defineComponent, inject } from 'vue';
import PrimeButton from 'primevue/button';
import type Keycloak from 'keycloak-js';
import TheHeader from '@/components/generics/TheHeader.vue';
import TheContent from '@/components/generics/TheContent.vue';
import TheNewFooter from '@/components/generics/TheNewFooter.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type { Content, Page, Section } from '@/types/ContentTypes';
import contentData from '@/assets/content.json';

export default defineComponent({
  name: 'UnsubscribeFromMailsPage',
  components: {
    TheContent,
    TheNewFooter,
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
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    return {
      footerPageSections: footerPage?.sections,
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
        console.log(response.data);
        if (response.data.includes('Successfully unsubscribed')) {
          this.message = 'You have been successfully removed from our mailing list.';
        } else {
          this.message = 'This UUID does not belong to any email address in our mailing list.';
        }
        this.changePageContent();
      } catch (error) {
        console.error(error);
      }
    },

    /**
     * This function sets the variable used for the conditional rendering of the page content to true
     */
    changePageContent() {
      this.isUnsubscribed = true;
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
