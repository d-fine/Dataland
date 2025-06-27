<template>
  <IconField>
    <InputIcon class="pi pi-search" aria-hidden="true" />
    <AutoComplete
      v-model="searchBarInput"
      :suggestions="searchBarSuggestions"
      @complete="searchCompanyName"
      option-label="companyName"
      :input-id="searchBarId"
      placeholder="Search company by name or identifier (e.g. PermID, LEI, ...)"
      :min-length="minimalQueryLength"
      :fluid="true"
      variant="filled"
    >
      <template #footer>
        <div v-if="searchBarSuggestions.length > 0">
          <Button label="View all results" variant="text" fluid />
        </div>
      </template>
    </AutoComplete>
  </IconField>
  <Message
    v-if="searchBarInput.length > 0 && !isQueryLongEnough"
    severity="error"
    size="small"
    variant="simple"
    class="col-5"
  >
    Please type at least 3 characters.
  </Message>
</template>

<script setup lang="ts">
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants.ts';
import { type FrameworkDataSearchFilterInterface } from '@/utils/SearchCompaniesForFrameworkDataPageDataRequester.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { type CompanyIdAndName } from '@clients/backend';
import type Keycloak from 'keycloak-js';
import { computed, inject, ref } from 'vue';
import Button from 'primevue/button';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import Message from 'primevue/message';
import AutoComplete, { type AutoCompleteCompleteEvent } from 'primevue/autocomplete';

export interface FrameworkDataSearchBarProps {
  searchBarId: string;
  filter?: FrameworkDataSearchFilterInterface;
  chunkSize?: number;
  currentPage?: number;
}

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const {
  searchBarId,
  chunkSize = 3,
  currentPage = 0,
  filter = {
    companyNameFilter: '',
    frameworkFilter: FRAMEWORKS_WITH_VIEW_PAGE,
    sectorFilter: [],
    countryCodeFilter: [],
  },
} = defineProps<FrameworkDataSearchBarProps>();

const searchBarInput = ref('');
const searchBarSuggestions = ref<CompanyIdAndName[]>([]);

const companyDataController = new ApiClientProvider(assertDefined(getKeycloakPromise)()).backendClients
  .companyDataController;
const minimalQueryLength = 3;

const isQueryLongEnough = computed(() => searchBarInput.value.length >= minimalQueryLength);

/**
 *
 */
async function searchCompanyName(autoCompleteCompleteEvent: AutoCompleteCompleteEvent): Promise<undefined> {
  try {
    searchBarSuggestions.value = (
      await companyDataController.getCompaniesBySearchString(autoCompleteCompleteEvent.query, chunkSize)
    ).data;
  } catch (e) {
    console.log('Searching Companies threw the following error: ', e);
  }
}
</script>

<style scoped>
:deep(.p-autocomplete-input) {
  height: 3rem;
}
</style>
