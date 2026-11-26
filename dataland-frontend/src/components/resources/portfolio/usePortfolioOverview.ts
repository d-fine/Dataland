import { computed, type ComputedRef, type Ref } from 'vue';
import { useSessionStorage } from '@vueuse/core';
import type { BasePortfolioName } from '@clients/userservice';

interface UsePortfolioOverviewOptions {
  sessionStorageKey: string;
  portfolioNames: Ref<BasePortfolioName[] | undefined>;
}

type UsePortfolioOverviewReturn = {
  currentPortfolioId: ComputedRef<string | undefined>;
  selectPortfolio: (portfolioId: string) => void;
  onTabChange: (value: string | number) => void;
};

/**
 * Composable for managing which portfolio tab is active on a portfolio overview page.
 * The selection is persisted in the session storage. The active portfolio is the selected one if it is part of the
 * given portfolio names, otherwise the first portfolio.
 * @param options the options of the composable
 * @param options.sessionStorageKey the session storage key under which the selected portfolio id is persisted
 * @param options.portfolioNames the portfolios shown as tabs, e.g. the data of a portfolio names query
 * @returns the active portfolio id and functions to change the selection
 */
export function usePortfolioOverview({
  sessionStorageKey,
  portfolioNames,
}: UsePortfolioOverviewOptions): UsePortfolioOverviewReturn {
  const selectedPortfolioId = useSessionStorage<string | undefined>(sessionStorageKey, undefined);

  const currentPortfolioId = computed(() => {
    const availablePortfolios = portfolioNames.value ?? [];
    if (availablePortfolios.some((portfolio) => portfolio.portfolioId === selectedPortfolioId.value)) {
      return selectedPortfolioId.value;
    }
    return availablePortfolios[0]?.portfolioId;
  });

  /**
   * Selects the portfolio with the given id, e.g. after it was created.
   * @param portfolioId the id of the portfolio to select
   */
  function selectPortfolio(portfolioId: string): void {
    selectedPortfolioId.value = portfolioId;
  }

  /**
   * Handles the tab change event by selecting the portfolio of the tab.
   * @param value The value of the tab aka the portfolioId of the selected portfolio.
   */
  function onTabChange(value: string | number): void {
    selectPortfolio(String(value));
  }

  return {
    currentPortfolioId,
    selectPortfolio,
    onTabChange,
  };
}
