<template>
  <div class="flex">
    <div class="text-left col-10">
      <IconField>
        <InputIcon class="pi pi-search" aria-hidden="true" style="z-index: 20; color: #958d7c" />
        <AutoComplete
          :autofocus="true"
          ref="autocomplete"
          v-model="searchBarInput"
          :suggestions="autocompleteArrayDisplayed"
          optionLabel="companyName"
          :autoOptionFocus="false"
          :min-length="3"
          input-id="search-bar-input"
          placeholder="Search company by name or identifier (e.g. PermID, LEI, ...)"
          inputClass="h-3rem d-framework-searchbar-input"
          panelClass="d-framework-searchbar-panel"
          variant="filled"
          :fluid="true"
          @complete="searchCompanyName"
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
              v-if="autocompleteArray && autocompleteArray.length >= maxNumOfDisplayedAutocompleteEntries"
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
import SearchResultHighlighter from '@/components/resources/frameworkDataSearch/SearchResultHighlighter.vue';
import router from '@/router';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import {
  type FrameworkDataSearchDropdownFilters,
  type FrameworkDataSearchFilterInterface,
  getCompanyDataForFrameworkDataSearchPage,
  getCompanyDataForFrameworkDataSearchPageWithoutFilters,
  getNumberOfCompaniesForFrameworkDataSearchPage,
} from '@/utils/SearchCompaniesForFrameworkDataPageDataRequester';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { type BasicCompanyInformation, type DataTypeEnum } from '@clients/backend';
import type Keycloak from 'keycloak-js';
import AutoComplete from 'primevue/autocomplete';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import PrimeButton from 'primevue/button';
import { inject, onMounted, ref, watch } from 'vue';

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

const props = withDefaults(
  defineProps<{
    chunkSize?: number | null;
    currentPage?: number;
    filter?: FrameworkDataSearchFilterInterface;
    maxNumOfDisplayedAutocompleteEntries?: number;
    emitSearchResultsArray?: boolean;
  }>(),
  {
    chunkSize: null,
    currentPage: 0,
    filter: (): FrameworkDataSearchFilterInterface => ({
      companyNameFilter: '',
      frameworkFilter: FRAMEWORKS_WITH_VIEW_PAGE,
      sectorFilter: [],
      countryCodeFilter: [],
      reportingPeriodFilter: [],
    }),
    maxNumOfDisplayedAutocompleteEntries: 3,
    emitSearchResultsArray: false,
  }
);

const emit = defineEmits<{
  'companies-received': [
    companiesReceived: Array<BasicCompanyInformation>,
    chunkIndex: number,
    totalNumberOfCompanies: number,
  ];
  'search-confirmed': [companyNameFilter: string];
}>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const autocomplete = ref<InstanceType<typeof AutoComplete> & AutoCompleteInternalState>();

const wereKeysPressed = ref(false);
const currentFocusedOptionIndex = ref(-1);
const searchBarInput = ref('');
const latestValidSearchString = ref('');
const autocompleteArray = ref<Array<object>>([]);
const autocompleteArrayDisplayed = ref<Array<object>>([]);
const notEnoughCharactersWarningTimeoutId = ref(0);
const areNotEnoughCharactersProvided = ref(false);

watch(searchBarInput, (newValue) => {
  validateSearchBarInput();
  saveCurrentSearchStringIfValid(newValue);
});

watch(
  () => props.filter,
  () => {
    searchBarInput.value = props.filter?.companyNameFilter ?? '';
    void queryCompany();
  },
  { deep: true }
);

watch(
  () => props.currentPage,
  () => {
    queryCompany(props.currentPage).catch(() => {
      console.error(`Unable to load data for ${props.currentPage} page`);
    });
  }
);

onMounted(() => {
  searchBarInput.value = props.filter?.companyNameFilter ?? '';
  void queryCompany();
});

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
 * Executes the search operation by triggering relevant actions.
 * Hides the autocomplete suggestion box, emits a search confirmation event,
 * and initiates a company query based on the current search input.
 *
 * @return {void} No return value.
 */
function executeSearch(): void {
  autocomplete.value?.hide();
  emit('search-confirmed', searchBarInput.value);
  void queryCompany();
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
 * Performs the company search if the parent component indicated it wants to receive the given chunk of the
 * complete search results
 * and the total number of records
 * @param chunkIndex the index of the requested chunk
 */
async function queryCompany(chunkIndex = 0): Promise<void> {
  if (props.emitSearchResultsArray) {
    const resultsArray = await getCompanies(chunkIndex);
    const totalNumberOfCompanies = await getTotalNumberOfCompanies();
    emit('companies-received', resultsArray, chunkIndex, totalNumberOfCompanies);
  }
}

/**
 * Builds the combination of dropdown filters (as Sets) currently selected on this component's filter prop.
 * @returns the current dropdown filters
 */
function getCurrentDropdownFilters(): FrameworkDataSearchDropdownFilters {
  return {
    frameworkFilter: new Set(props.filter?.frameworkFilter),
    countryCodeFilter: new Set(props.filter?.countryCodeFilter),
    sectorFilter: new Set(props.filter?.sectorFilter),
    reportingPeriodFilter: new Set(props.filter?.reportingPeriodFilter),
  };
}

/**
 * Performs the company search if the parent component indicated it wants to receive the complete search results
 * @param chunkIndex the index of the requested chunk
 * @returns chunk of companies
 */
async function getCompanies(chunkIndex: number): Promise<Array<BasicCompanyInformation>> {
  return await getCompanyDataForFrameworkDataSearchPage(
    searchBarInput.value,
    getCurrentDropdownFilters(),
    assertDefined(getKeycloakPromise)(),
    props.chunkSize ?? undefined,
    chunkIndex
  );
}

/**
 * Get the total number of copanies with the given filter
 * @returns total number of companies
 */
async function getTotalNumberOfCompanies(): Promise<number> {
  return await getNumberOfCompaniesForFrameworkDataSearchPage(
    searchBarInput.value,
    getCurrentDropdownFilters(),
    assertDefined(getKeycloakPromise)()
  );
}

/**
 * This function is called to obtain search suggestions for the dropdown. Uses the Dataland API to search
 * companies by the current search bar input (and selected filters).
 * @param companyName the autocomplete suggestion event
 * @param companyName.query the query text entered into the search bar
 */
async function searchCompanyName(companyName: { query: string }): Promise<void> {
  if (
    areAllFiltersDeactivated(
      props.filter?.frameworkFilter,
      props.filter?.countryCodeFilter,
      props.filter?.sectorFilter,
      props.filter?.reportingPeriodFilter
    )
  ) {
    autocompleteArray.value = await getCompanyDataForFrameworkDataSearchPageWithoutFilters(
      companyName.query,
      assertDefined(getKeycloakPromise)(),
      props.maxNumOfDisplayedAutocompleteEntries
    );
  } else {
    autocompleteArray.value = await getCompanyDataForFrameworkDataSearchPage(
      companyName.query,
      getCurrentDropdownFilters(),
      assertDefined(getKeycloakPromise)(),
      props.maxNumOfDisplayedAutocompleteEntries,
      0
    );
  }
  autocompleteArrayDisplayed.value = autocompleteArray.value;
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

/**
 * Checks if all filteres are deactivated. Is used for triggering a special case function
 * @param frameworkFilter selection options of framework filter
 * @param countryCodeFilter selection options of country code filter
 * @param sectorFilter selection options of sector filter
 * @param reportingPeriodFilter selection options of reporting period filter
 * @returns boolean value representing check result
 */
function areAllFiltersDeactivated(
  frameworkFilter: Array<DataTypeEnum>,
  countryCodeFilter: Array<string>,
  sectorFilter: Array<string>,
  reportingPeriodFilter: Array<string>
): boolean {
  return !(frameworkFilter.length + countryCodeFilter.length + sectorFilter.length + reportingPeriodFilter.length);
}

defineExpose({ closeOverlay });
</script>
<style scoped>
.text-danger {
  color: var(--fk-color-error);
  font-size: var(--font-size-xs);
}
</style>
