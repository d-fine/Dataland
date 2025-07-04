<template>
  <PrimeButton @click="onClick" class="uppercase p-button p-button-sm" data-test="singleDataRequestButton">
    <span class="d-letters"> Request Data </span>
  </PrimeButton>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import PrimeButton from 'primevue/button';
import { type DataTypeEnum } from '@clients/backend';
import { type RouteLocationNormalizedLoaded } from 'vue-router';
import router from '@/router';

export default defineComponent({
  name: 'SingleDataRequestButton',
  components: { PrimeButton },
  props: {
    companyId: {
      type: String,
      required: true,
    },
  },
  methods: {
    /**
     * navigates to the single data request page
     * @returns a router push
     */
    onClick() {
      const thisCompanyId = this.companyId;
      const currentRoute: RouteLocationNormalizedLoaded = router.currentRoute.value;
      const dataType = currentRoute.params.dataType;
      const preSelectedFramework = dataType ? (dataType as DataTypeEnum) : '';
      return router.push({
        path: `/singledatarequest/${thisCompanyId}`,
        query: {
          preSelectedFramework: preSelectedFramework,
        },
      });
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

  &.p-button-sm {
    font-size: var(--font-size-sm);
    padding: var(--spacing-xs) var(--spacing-sm);
  }
}
</style>
