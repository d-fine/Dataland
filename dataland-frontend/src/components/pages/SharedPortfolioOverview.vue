<template>
  <TheContent class="min-h-screen relative">
    <PortfolioOverviewTabs
      :value="currentPortfolioId"
      :portfolioNames="portfolioNames ?? []"
      @update:value="onTabChange"
    >
      <template #panel="{ portfolio }">
        <SharedPortfolioDetails
          :portfolioId="portfolio.portfolioId"
          :data-test="`portfolio-${portfolio.portfolioName}`"
        />
      </template>
      <template #empty>
        <PortfolioOverviewEmptyState :is-pending="isPending" :is-error="isError" />
      </template>
    </PortfolioOverviewTabs>
  </TheContent>
</template>

<script setup lang="ts">
import TheContent from '@/components/generics/TheContent.vue';
import SharedPortfolioDetails from '@/components/resources/portfolio/SharedPortfolioDetails.vue';
import PortfolioOverviewEmptyState from '@/components/resources/portfolio/PortfolioOverviewEmptyState.vue';
import PortfolioOverviewTabs from '@/components/resources/portfolio/PortfolioOverviewTabs.vue';
import { useGetAllSharedPortfolioNamesForCurrentUserQuery } from '@/api-queries/user-service/portfolio/useGetAllSharedPortfolioNamesForCurrentUserQuery';
import { usePortfolioOverview } from '@/components/resources/portfolio/usePortfolioOverview.ts';

const { data: portfolioNames, isPending, isError } = useGetAllSharedPortfolioNamesForCurrentUserQuery();

const { currentPortfolioId, onTabChange } = usePortfolioOverview({
  sessionStorageKey: 'last-selected-shared-portfolio-id',
  portfolioNames,
});
</script>
