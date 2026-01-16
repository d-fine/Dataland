<template>
  <Tabs
    :value="value || 'no-portfolios-available'"
    :scrollable="true"
    data-test="portfolios"
    @update:value="$emit('update:value', $event)"
  >
    <div class="tabs-container">
      <TabList>
        <Tab v-for="portfolio in portfolioNames" :key="portfolio.portfolioId" :value="portfolio.portfolioId">
          <div class="tabview-header" :title="portfolio.portfolioName" :data-test="portfolio.portfolioName">
            {{ portfolio.portfolioName }}
          </div>
        </Tab>
      </TabList>
      <slot name="actions" />
    </div>
    <TabPanels>
      <TabPanel v-for="portfolio in portfolioNames" :key="portfolio.portfolioId" :value="portfolio.portfolioId">
        <slot name="panel" :portfolio="portfolio" />
      </TabPanel>
      <TabPanel value="no-portfolios-available">
        <slot name="empty">
          <h1 v-if="!portfolioNames || portfolioNames.length === 0">No Portfolios available.</h1>
        </slot>
      </TabPanel>
    </TabPanels>
  </Tabs>
</template>

<script setup lang="ts">
import Tabs from 'primevue/tabs';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import TabPanels from 'primevue/tabpanels';
import TabPanel from 'primevue/tabpanel';
import type { BasePortfolioName } from '@clients/userservice';

defineProps<{
  value?: string;
  portfolioNames: BasePortfolioName[];
}>();

defineEmits<{
  'update:value': [value: string | number];
}>();
</script>

<style scoped>
.tabs-container {
  display: flex;
  flex-direction: row;
  gap: var(--spacing-md);
  align-items: center;
  justify-content: space-between;
  padding-right: var(--spacing-md);
}

.tabview-header {
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-base);
  max-width: 15rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
