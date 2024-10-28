<template>
  <h1>Hallo die Seite ist noch leer</h1>
  <h1>Die uuid ist: {{ subscriptionId }}</h1>

  <PrimeButton @click="logSubscriptionId"> Unsubscribe </PrimeButton>
</template>

<script lang="ts">
import { type ComponentPublicInstance, defineComponent, inject, ref } from 'vue';
import PrimeButton from 'primevue/button';

export default defineComponent({
  name: 'UnsubscribeFromMailsPage',
  components: {
    PrimeButton,
  },

  props: {
    subscriptionId: {
      type: String,
      required: true,
    },
  },
  /*
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },

  data() {
    return {

    };
  },
*/

  methods: {
    /**
     * This function logs the subscirptionId
     */
    logSubscriptionId() {
      console.log(this.subscriptionId);
    },
  },
});
</script>
