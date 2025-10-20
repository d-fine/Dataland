<template>
  <div class="container" data-test="company-info-sheet">
    <CompaniesOnlySearchBar
      v-if="!useMobileView"
      @select-company="router.push(`/companies/${$event.companyId}`)"
      wrapper-class-additions="w-8 mt-2"
    />

    <CompanyInformationBanner
      :companyId="companyId"
      :show-single-data-request-button="showSingleDataRequestButton"
      @fetchedCompanyInformation="onFetchedCompanyInformation($event)"
      class="w-12"
    />
  </div>
</template>

<script setup lang="ts">
import CompanyInformationBanner from '@/components/pages/CompanyInformation.vue';
import CompaniesOnlySearchBar from '@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue';
import router from '@/router';
import { type CompanyInformation } from '@clients/backend';
import { inject, ref } from 'vue';

const { companyId, showSingleDataRequestButton } = defineProps({
  companyId: {
    type: String,
    required: true,
  },
  showSingleDataRequestButton: {
    type: Boolean,
    default: false,
  },
});

const useMobileView = inject<boolean>('useMobileView', false);

const emit = defineEmits(['fetchedCompanyInformation']);
const companyName = ref<string>();

/**
 * On fetched company information defines the companyName and emits an event of type "fetchedCompanyInformation"
 * @param companyInfo company information from which the company name can be retrieved
 */
function onFetchedCompanyInformation(companyInfo: CompanyInformation): void {
  companyName.value = companyInfo.companyName;
  emit('fetchedCompanyInformation', companyInfo);
}
</script>

<style scoped lang="scss">
.container {
  width: 100%;
  background-color: var(--surface-0);
  padding: 0.5rem 1rem 1rem;
  display: flex;
  flex-direction: column;
  align-items: start;
  gap: 0;
}

.headline {
  width: 100%;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}
</style>
