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
    <Listbox
        v-model="selectedPortfolios"
        :options="allUserPortfolios"
        multiple
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
import {defineEmits, inject, ref, watch} from 'vue';
import {type BasePortfolio} from '@clients/userservice';
import {ApiClientProvider} from '@/services/ApiClients.ts';
import {assertDefined} from '@/utils/TypeScriptUtils.ts';
import Listbox from 'primevue/listbox';
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
const selectedPortfolios = ref<ReducedBasePortfolio[]>([]);

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
  if (selectedPortfolios.value.length === 0) return;
  selectedPortfolios.value.forEach(async (selectedPortfolio) => {
    console.log("selectedPortfolio: " + JSON.stringify(selectedPortfolio) + "!!!!!!!");
    /*
    const reducedBasePortfolio = allUserPortfolios.find(
        (reducedBasePortfolio) => {
          console.log("reducedBasePortfolio.portfolioName: " + reducedBasePortfolio.portfolioName + "!!!!!!!")
          return reducedBasePortfolio.portfolioName === selectedPortfolio.portfolioName
        }
    );
     */
    await apiClientProvider.apiClients.portfolioController.replacePortfolio(
        selectedPortfolio.portfolioId,
        {
          portfolioName: selectedPortfolio.portfolioName,
          // as unknown as Set<string> cast required to ensure proper json is created
          companyIds: [...selectedPortfolio.companyIds, props.companyId] as unknown as Set<string>,
        });
  });
  closeDialog();
};

const closeDialog = (): void => {
  selectedPortfolios.value = [];
  isModalVisible.value = false;
  emit('closePortfolioModal');
};
</script>

<style scoped lang="scss"></style>
