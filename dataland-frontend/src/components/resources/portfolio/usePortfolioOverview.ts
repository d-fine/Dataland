import { ref, onMounted, type Ref } from 'vue';
import { type RemovableRef, useSessionStorage } from '@vueuse/core';
import type { BasePortfolioName } from '@clients/userservice';

interface UsePortfolioOverviewOptions {
  sessionStorageKey: string;
  fetchPortfolios: () => Promise<BasePortfolioName[]>;
}

type UsePortfolioOverviewReturn = {
  currentPortfolioId: RemovableRef<string | undefined>;
  portfolioNames: Ref<BasePortfolioName[]>;
  getPortfolios: () => Promise<void>;
  setCurrentPortfolioId: (portfolioId?: string) => void;
  onTabChange: (value: string | number) => void;
};

/**
 * Composable for managing portfolio overview state.
 * @param sessionStorageKey
 * @param fetchPortfolios
 */
export function usePortfolioOverview({
  sessionStorageKey,
  fetchPortfolios,
}: UsePortfolioOverviewOptions): UsePortfolioOverviewReturn {
  const currentPortfolioId = useSessionStorage<string | undefined>(sessionStorageKey, undefined);
  const portfolioNames = ref<BasePortfolioName[]>([]);

  onMounted(() => {
    void getPortfolios().then(() => setCurrentPortfolioId());
  });

  /**
   * Retrieve all (shared) portfolios for the currently logged-in user.
   */
  async function getPortfolios(): Promise<void> {
    try {
      portfolioNames.value = await fetchPortfolios();
      setCurrentPortfolioId();
    } catch (error) {
      console.log(error);
    }
  }

  /**
   * Sets the current portfolio ID based on the following priority:
   * 1. If a portfolioId is provided (e.g. after creating a new portfolio), use it if valid.
   * 2. If not, and a session-stored portfolioId exists, use it if valid.
   * 3. If none of the above are valid, fall back to the first portfolio in the list.
   */
  function setCurrentPortfolioId(portfolioId?: string): void {
    if (portfolioNames.value.length === 0) {
      currentPortfolioId.value = undefined;
      return;
    }

    if (portfolioId && portfolioNames.value.some((p) => p.portfolioId === portfolioId)) {
      currentPortfolioId.value = portfolioId;
      return;
    }

    if (currentPortfolioId.value && portfolioNames.value.some((p) => p.portfolioId === currentPortfolioId.value)) {
      return;
    }

    currentPortfolioId.value = portfolioNames.value[0]?.portfolioId;
  }

  /**
   * Handles the tab change event by changing the currentPortfolioId.
   * @param value The value of the tab aka the portfolioId of the selected portfolio.
   */
  function onTabChange(value: string | number): void {
    setCurrentPortfolioId(String(value));
  }

  return {
    currentPortfolioId,
    portfolioNames,
    getPortfolios,
    setCurrentPortfolioId,
    onTabChange,
  };
}
