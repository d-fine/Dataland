<template>
  <PrimeButton
    class="uppercase p-button pl-1 pr-1 pb-1 pt-1 justify-content-center h-3rem w-full"
    name="join_dataland_button"
    @click="register"
  >
    <span class="d-letters"> Create an account </span>
    <i class="material-icons pl-1" aria-hidden="true" alt="chevron_right">chevron_right</i>
  </PrimeButton>
</template>

<script lang="ts">
import PrimeButton from 'primevue/button';
import { defineComponent, inject } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';

export default defineComponent({
  name: 'JoinDatalandButton',
  components: { PrimeButton },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  methods: {
    /**
     * Redirects the user to the dataland registration page
     */
    register() {
      assertDefined(this.getKeycloakPromise)()
        .then(async (keycloak) => {
          if (!keycloak.authenticated) {
            const baseUrl = window.location.origin;
            const url = await keycloak.createRegisterUrl({
              redirectUri: `${baseUrl}/companies`,
            });
            location.assign(url);
          }
        })
        .catch((error) => console.log(error));
    },
  },
});
</script>
<style scoped>
.d-letters {
  letter-spacing: 0.05em;
}

.p-button {
  white-space: nowrap;
  cursor: pointer;
  font-weight: var(--button-fw);
  text-decoration: none;
  min-width: 10em;
  width: fit-content;
  justify-content: center;
  display: inline-flex;
  align-items: center;
  vertical-align: bottom;
  flex-direction: row;
  letter-spacing: 0.05em;
  font-family: inherit;
  transition: all 0.2s;
  border-radius: 0;
  text-transform: uppercase;
  font-size: 0.875rem;

  &:enabled:hover {
    color: white;
    background: hsl(from var(--btn-primary-bg) h s calc(l - 20));
    border-color: hsl(from var(--btn-primary-bg) h s calc(l - 20));
  }

  &:enabled:active {
    background: hsl(from var(--btn-primary-bg) h s calc(l - 10));
    border-color: hsl(from var(--btn-primary-bg) h s calc(l - 10));
  }

  &:disabled {
    background-color: transparent;
    border: 0;
    color: var(--btn-disabled-color);
    cursor: not-allowed;
  }

  &:focus {
    outline: 0 none;
    outline-offset: 0;
    box-shadow: 0 0 0 0.2rem var(--btn-focus-border-color);
  }
}

.p-button {
  color: var(--btn-primary-color);
  background: var(--btn-primary-bg);
  border: 1px solid var(--btn-primary-bg);
  padding: var(--spacing-xs) var(--spacing-md);
  line-height: 1rem;
  margin: var(--spacing-xxs);
}
</style>
