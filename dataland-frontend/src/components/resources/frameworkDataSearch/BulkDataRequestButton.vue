<template>
  <router-link to="/bulkdatarequest" class="no-underline" data-test="bulkDataRequestButton">
    <PrimeButton class="uppercase p-button p-button-sm mr-3">
      <i class="material-icons"> add_box </i>
      <span class="d-letters pl-2"> BULK DATA REQUEST </span>
    </PrimeButton>
  </router-link>
</template>

<script lang="ts">
import PrimeButton from 'primevue/button';
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'BulkDataRequestButton',
  components: { PrimeButton },
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
</style>
