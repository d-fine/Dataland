<template>
  <TheHeader :showUserProfileDropdown="false" />

  <TheContent class="container">
    <main>
      <div>
        <div v-if="!isUnsubscribed" class="subscribed">
          <h1>We are sorry you want to unsubscribe from our mailing list.</h1>

          <!--  <h1>Die uuid ist: {{ subscriptionId }}</h1> -->
          <PrimeButton @click="unsubscribeFromMailingList" class="unsubscribe-button"> Unsubscribe </PrimeButton>
        </div>
        <div v-else >
          <p class="unsubscribed-confirmation">
            <span> {{ mailAddress }} </span> <br />
            has successfully been removed from our mailing list.
          </p>
          <p>
            Go to
            <router-link to="/" class="text-primary" title="back to landing page">Dataland</router-link>
          </p>
        </div>
      </div>
    </main>
  </TheContent>


  <TheNewFooter :is-light-version="true" :sections="footerContent" class="footer"/>
</template>

<script lang="ts">
import { defineComponent, inject, ref } from 'vue';
import PrimeButton from 'primevue/button';
import type Keycloak from 'keycloak-js';
import TheHeader from '@/components/generics/TheHeader.vue';
import TheContent from "@/components/generics/TheContent.vue";
import TheNewFooter from "@/components/generics/TheNewFooter.vue";
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type {Page, Section} from "@/types/ContentTypes";
import contentData from "@/assets/content.json";


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

  data():{ footerContent: Section[] | undefined } {
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    const footerContent = footerPage?.sections;
    return {
      isUnsubscribed: false,
      mailAddress: '',
      footerContent,
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
        console.log(response.data);
        /* this.mailAddress = response.data */
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

    /**
     * This function sets the variable used for the conditional rendering of the page content to false
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
  @media only screen and (max-width: 768px){
    height: 60vh;
  }
}
main{
  position: absolute;
  left: 50%;
  top: 55%;
  transform: translate(-50%, -50%);
  @media only screen and (max-width: 768px){
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
  @media only screen and (max-width: 768px){
    font-size: 1rem;
  }
}
.unsubscribed-confirmation > span {
  text-decoration: underline;
}
.footer{
  position: absolute;
  width: 100vw;
  top: 70vh;
  left: 0;
  @media only screen and (max-width: 768px){
    top: 60vh;
  }
}
</style>
