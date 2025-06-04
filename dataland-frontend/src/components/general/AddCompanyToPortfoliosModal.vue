<template>
  <PrimeDialog
    v-model:visible="isModalVisible"
    style="text-align: center; width: 30em"
    header="Add company to portfolios"
    :show-header="true"
    :closable="true"
    :dismissable-mask="true"
    :modal="true"
    :close-on-escape="true"
    data-test="addCompanyToPortfoliosModal"
    @after-hide="closeDialog"
  >
    <MultiSelect
      v-model="selectedPortfolios"
      :options="allUserPortfolios"
      optionLabel="portfolioName"
      placeholder="Select portfolios"
      class="w-full md:w-80"
    />
    <PrimeButton class="primary-button" aria-label="Add Company" @click="handleCompanyAddition">
      <span>Add company to portfolio(s)</span>
    </PrimeButton>
  </PrimeDialog>
</template>

<script setup lang="ts">
import { defineEmits, inject, ref, watch } from 'vue';
import { type BasePortfolio } from '@clients/userservice';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import MultiSelect from 'primevue/multiselect';
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';
import type Keycloak from 'keycloak-js';

interface ReducedBasePortfolio {
  portfolioId: string,
  portfolioName: string,
  companyIds: string[]
}

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const isModalVisible = ref<boolean>(false);
let allUserPortfolios: ReducedBasePortfolio[] = [];
let selectedPortfolios: ReducedBasePortfolio[] = [];

const props = defineProps({
  companyId: {
    type: String,
    required: true,
  },
  isModalOpen: {
    type: Boolean,
    required: false,
    default: false,
  },
});

const emit = defineEmits(['closePortfolioModal']);

watch<boolean>(
  () => props.isModalOpen,
  async (newValue) => {
    if (newValue) {
      await fetchUserPortfolios();
    }
    isModalVisible.value = newValue;
  }
);

const convertToReducedBasePortfolio = (basePortfolio: BasePortfolio): ReducedBasePortfolio => {
  return {
    portfolioId: basePortfolio.portfolioId,
    portfolioName: basePortfolio.portfolioName,
    companyIds: Array.from(basePortfolio.companyIds)
  };
};

const fetchUserPortfolios = async (): Promise<void> => {
  const allUserBasePortfolios = (
    await apiClientProvider.apiClients.portfolioController.getAllPortfoliosForCurrentUser()
  ).data;
  allUserPortfolios = allUserBasePortfolios.map(convertToReducedBasePortfolio);
};

const handleCompanyAddition = (): void => {
  if (selectedPortfolios.length === 0) return;
  selectedPortfolios.forEach(async (portfolio) => {
    await apiClientProvider.apiClients.portfolioController.replacePortfolio(portfolio.portfolioId, {
      portfolioName: portfolio.portfolioName,
      // as unknown as Set<string> cast required to ensure proper json is created
      companyIds: [...portfolio.companyIds, props.companyId] as unknown as Set<string>,
    });
  });
  closeDialog();
};

const closeDialog = (): void => {
  selectedPortfolios = [];
  isModalVisible.value = false;
  emit('closePortfolioModal');
};
</script>

<style scoped lang="scss"></style>
