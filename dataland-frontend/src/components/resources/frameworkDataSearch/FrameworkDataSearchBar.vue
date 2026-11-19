<template>
  <div class="flex">
    <div class="text-left col-10">
      <IconField>
        <InputIcon class="pi pi-search" aria-hidden="true" style="z-index: 20; color: #958d7c" />
        <AutoComplete
          :autofocus="true"
          ref="autocomplete"
          v-model="searchBarInput"
          :suggestions="autocompleteSuggestions"
          optionLabel="companyName"
          :autoOptionFocus="false"
          :min-length="3"
          input-id="search-bar-input"
          placeholder="Search company by name or identifier (e.g. PermID, LEI, ...)"
          inputClass="h-3rem d-framework-searchbar-input"
          panelClass="d-framework-searchbar-panel"
          variant="filled"
          :fluid="true"
          @complete="onComplete"
          @keydown="noteThatAKeyWasPressed"
          @keydown.down="getCurrentFocusedOptionIndex"
          @keydown.up="getCurrentFocusedOptionIndex"
          @option-select="handleItemSelect"
          @keyup.enter="executeSearchIfNoItemFocused"
          @focus="setCurrentFocusedOptionIndexToDefault"
        >
          <template #option="slotProps">
            <i class="pi pi-search pl-3 pr-3" aria-hidden="true" />
            <SearchResultHighlighter :text="slotProps.option.companyName" :searchString="latestValidSearchString" />
          </template>

          <template #footer>
            <PrimeButton
              v-if="autocompleteSuggestions && autocompleteSuggestions.length >= maxNumOfDisplayedAutocompleteEntries"
              severity="secondary"
              @click.stop="executeSearchFromViewAllResultsButton"
              label="View all results"
              fluid
              data-test="view-all-results-button"
            />
          </template>
        </AutoComplete>
      </IconField>
    </div>
    <div class="col-2 mt-2 justify-content-center text-left">
      <span class="text-danger" v-if="areNotEnoughCharactersProvided">Please type at least 3 characters</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { type CompanySearchFilter } from '@/api-queries/backend/company-data/companyDataKeys';
import { useGetCompaniesBySearchStringQuery } from '@/api-queries/backend/company-data/useGetCompaniesBySearchStringQuery';
import { useGetCompaniesQuery } from '@/api-queries/backend/company-data/useGetCompaniesQuery';
import SearchResultHighlighter from '@/components/resources/frameworkDataSearch/SearchResultHighlighter.vue';
import router from '@/router';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { type BasicCompanyInformation, type CompanyIdAndName } from '@clients/backend';
import { keepPreviousData } from '@tanstack/vue-query';
import AutoComplete from 'primevue/autocomplete';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import PrimeButton from 'primevue/button';
import { computed, ref, watch } from 'vue';

/**
 * This interface defines the internal state of the autocomplete component
 * not exposed in their typescript definition
 */
export interface AutoCompleteInternalState {
  focusedOptionIndex: number | null;

  hide(): void;

  $refs: {
    focusInput: HTMLInputElement;
  };
}

export type CompanySearchDropdownFilters = Required<Omit<CompanySearchFilter, 'searchString'>>;

const props = withDefaults(
  defineProps<{
    appliedSearchString?: string;
    filter?: CompanySearchDropdownFilters;
    maxNumOfDisplayedAutocompleteEntries?: number;
  }>(),
  {
    appliedSearchString: '',
    filter: (): CompanySearchDropdownFilters => ({
      dataTypes: FRAMEWORKS_WITH_VIEW_PAGE,
      countryCodes: [],
      sectors: [],
      reportingPeriods: [],
    }),
    maxNumOfDisplayedAutocompleteEntries: 3,
  }
);

const emit = defineEmits<{
  'search-confirmed': [searchString: string];
}>();

const autocomplete = ref<InstanceType<typeof AutoComplete> & AutoCompleteInternalState>();

const wereKeysPressed = ref(false);
const currentFocusedOptionIndex = ref(-1);
const searchBarInput = ref('');
const latestValidSearchString = ref('');
const notEnoughCharactersWarningTimeoutId = ref(0);
const areNotEnoughCharactersProvided = ref(false);

const autocompleteSearchString = ref('');
const autocompleteRequestCount = ref(0);
const areAllFiltersDeactivated = computed(
  () =>
    props.filter.dataTypes.length +
      props.filter.countryCodes.length +
      props.filter.sectors.length +
      props.filter.reportingPeriods.length ===
    0
);

const companyNamesQuery = useGetCompaniesBySearchStringQuery(
  {
    searchString: () => (areAllFiltersDeactivated.value ? autocompleteSearchString.value : undefined),
    resultLimit: () => props.maxNumOfDisplayedAutocompleteEntries,
  },
  { placeholderData: keepPreviousData }
);
const filteredCompaniesQuery = useGetCompaniesQuery(
  {
    searchString: autocompleteSearchString,
    dataTypes: () => props.filter.dataTypes,
    countryCodes: () => props.filter.countryCodes,
    sectors: () => props.filter.sectors,
    reportingPeriods: () => props.filter.reportingPeriods,
    chunkSize: () => props.maxNumOfDisplayedAutocompleteEntries,
    chunkIndex: 0,
  },
  {
    enabled: computed(() => !areAllFiltersDeactivated.value && !!autocompleteSearchString.value),
    placeholderData: keepPreviousData,
  }
);

/**
 * The AutoComplete only shows its overlay when it receives a new suggestions array after a completion request.
 * Therefore, the suggestions keep their previous array while the suggestions for a new search string are loading and
 * become a new array once they are loaded, also if the same search string is completed again.
 */
const autocompleteSuggestions = computed<Array<CompanyIdAndName | BasicCompanyInformation> | undefined>(
  (previousSuggestions) => {
    void autocompleteRequestCount.value;
    const activeQuery = areAllFiltersDeactivated.value ? companyNamesQuery : filteredCompaniesQuery;
    if (activeQuery.isError.value) return [];
    const suggestions = activeQuery.data.value;
    if (!suggestions || activeQuery.isPlaceholderData.value) return previousSuggestions;
    return [...suggestions];
  }
);

watch(
  () => props.appliedSearchString,
  (appliedSearchString) => {
    searchBarInput.value = appliedSearchString;
  },
  { immediate: true }
);

watch(searchBarInput, (newValue) => {
  validateSearchBarInput();
  saveCurrentSearchStringIfValid(newValue);
});

/**
 * Requests suggestions for the text entered into the search bar.
 * @param event the completion event of the AutoComplete
 * @param event.query the text entered into the search bar
 */
function onComplete(event: { query: string }): void {
  autocompleteSearchString.value = event.query;
  autocompleteRequestCount.value++;
}

/**
 * Sets the wereKeysPressed variable to true
 */
function noteThatAKeyWasPressed(): void {
  wereKeysPressed.value = true;
}

/**
 * Updates the local search string if the new search string is well-defined
 * @param currentSearchString the potentially new search string
 */
function saveCurrentSearchStringIfValid(currentSearchString: string | object): void {
  if (currentSearchString && typeof currentSearchString === 'string') {
    latestValidSearchString.value = currentSearchString;
  }
}

/**
 * Called on button presses of the up/down keys and updates the index of the currently selected element.
 */
function getCurrentFocusedOptionIndex(): void {
  currentFocusedOptionIndex.value = autocomplete.value?.focusedOptionIndex ?? -1;
}

/**
 * Resets the currently selected index variable
 */
function setCurrentFocusedOptionIndexToDefault(): void {
  currentFocusedOptionIndex.value = -1;
}

/**
 * Called when an item is selected from the dropdown. Navigates to the company cockpit page for the selected company
 * @param event the click event
 * @param event.value the company that was clicked on
 */
function handleItemSelect(event: { value: BasicCompanyInformation }): void {
  const companyIdOfSelectedItem = event.value.companyId;
  void router.push(`/companies/${companyIdOfSelectedItem}`);
}

/**
 * Hides the autocomplete suggestion box and emits a search confirmation event with the current search input.
 */
function executeSearch(): void {
  autocomplete.value?.hide();
  emit('search-confirmed', searchBarInput.value);
}

/**
 * Executes a search operation if no item is currently focused and the necessary conditions are met.
 * The method checks that there are enough characters provided, no option is currently focused,
 * and keys were previously pressed to trigger the search execution.
 *
 * @return {void} This method does not return a value.
 */
function executeSearchIfNoItemFocused(): void {
  if (!areNotEnoughCharactersProvided.value && currentFocusedOptionIndex.value === -1 && wereKeysPressed.value) {
    executeSearch();
  }
}

/**
 * Executes a search operation if the required character conditions are met.
 *
 * This method checks if the minimum character requirement is satisfied.
 * If the condition is met, it triggers the search execution process.
 *
 * @return {void} Does not return a value.
 */
function executeSearchFromViewAllResultsButton(): void {
  if (!areNotEnoughCharactersProvided.value) {
    executeSearch();
  }
}

/**
 * Validates the current search bar input. If there are only one or two characters typed, an error message
 * shall be rendered asking the user to provide at least three characters.
 */
function validateSearchBarInput(): void {
  clearTimeout(notEnoughCharactersWarningTimeoutId.value);
  const areThereNotEnoughCharacters = searchBarInput.value.length > 0 && searchBarInput.value.length < 3;

  if (areThereNotEnoughCharacters) {
    notEnoughCharactersWarningTimeoutId.value = setTimeout(() => {
      areNotEnoughCharactersProvided.value = true;
    }, 1000);
  } else {
    areNotEnoughCharactersProvided.value = false;
  }
}

/**
 * Invoked when autocomplete shall hide.
 */
function closeOverlay(): void {
  autocomplete.value?.hide();
}

defineExpose({ closeOverlay });
</script>
<style scoped>
.text-danger {
  color: var(--fk-color-error);
  font-size: var(--font-size-xs);
}
</style>
