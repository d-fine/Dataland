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
    <PrimeButton
        class="primary-button"
        aria-label="Add Company"
        @click="handleCompanyAddition"
    >
    </PrimeButton>
  </PrimeDialog>
</template>

<script setup lang="ts">
import {inject, onMounted, ref, watch} from "vue";
import {BasePortfolio} from "@clients/userservice";
import {ApiClientProvider} from "@/services/ApiClients.ts";
import {assertDefined} from "@/utils/TypeScriptUtils.ts";
import PrimeDialog from 'primevue/dialog';
import type Keycloak from "keycloak-js";

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const isModalVisible = ref<boolean>(false);
const allUserPortfolios = ref<BasePortfolio[]>([]);
const selectedPortfolios = ref<BasePortfolio[]>([]);

const props = defineProps(
    {
      companyId: {
        type: String,
        required: true
      },
      isModalOpen: {
        type: Boolean,
        required: false,
        default: false
      }
    }
);

watch<boolean>(() => props.isModalOpen, async (newValue) => {
  if (newValue) {
    await fetchUserPortfolios();
  }
  isModalVisible.value = newValue;
});

const fetchUserPortfolios = async (): Promise<void> => {
  allUserPortfolios.value =
      (await apiClientProvider.apiClients.portfolioController.getAllPortfoliosForCurrentUser()).data;
}

const handleCompanyAddition = (): void => {
  if (selectedPortfolios.value.length === 0) return;
  selectedPortfolios.value.forEach((portfolio) => {
    portfolio.companyIds.add(props.companyId);
    apiClientProvider.apiClients.portfolioController.replacePortfolio(
        portfolio.portfolioId,
        {
          portfolioName: portfolio.portfolioName,
          // as unknown as Set<string> cast required to ensure proper json is created
          companyIds: portfolio.companyIds as unknown as Set<string>
        }
    )
  });

}
</script>

<style scoped lang="scss">

</style>