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
          @item-select="handleItemSelect"
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
              @click="executeSearchIfNoItemFocused"
              label="View all results"
              fluid
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

<script lang="ts">
import SearchResultHighlighter from '@/components/resources/frameworkDataSearch/SearchResultHighlighter.vue';
import router from '@/router';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import {
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
import { defineComponent, inject, ref } from 'vue';

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

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
      autocomplete: ref<InstanceType<typeof AutoComplete> & AutoCompleteInternalState>(),
    };
  },
  name: 'FrameworkDataSearchBar',
  components: { AutoComplete, SearchResultHighlighter, IconField, InputIcon, PrimeButton },

  emits: ['companies-received', 'search-confirmed'],

  props: {
    chunkSize: {
      type: Number,
      default: null,
    },
    currentPage: {
      type: Number,
      default: 0,
    },
    filter: {
      type: Object as () => FrameworkDataSearchFilterInterface,
      default(): FrameworkDataSearchFilterInterface {
        return {
          companyNameFilter: '',
          frameworkFilter: FRAMEWORKS_WITH_VIEW_PAGE,
          sectorFilter: [],
          countryCodeFilter: [],
        };
      },
    },
    maxNumOfDisplayedAutocompleteEntries: {
      type: Number,
      default: 3,
    },
    emitSearchResultsArray: {
      type: Boolean,
      default: false,
    },
  },
  mounted() {
    this.searchBarInput = this.filter?.companyNameFilter ?? '';
    void this.queryCompany();
  },

  watch: {
    searchBarInput(newValue: string) {
      this.validateSearchBarInput();
      this.saveCurrentSearchStringIfValid(newValue);
    },
    filter: {
      handler() {
        this.searchBarInput = this.filter?.companyNameFilter ?? '';
        void this.queryCompany();
      },
      deep: true,
    },
    currentPage() {
      this.queryCompany(this.currentPage).catch(() => {
        console.error(`Unable to load data for ${this.currentPage} page`);
      });
    },
  },

  data: function () {
    return {
      wereKeysPressed: false,
      currentFocusedOptionIndex: -1,
      searchBarInput: '',
      latestValidSearchString: '',
      autocompleteArray: [] as Array<object>,
      autocompleteArrayDisplayed: [] as Array<object>,
      notEnoughCharactersWarningTimeoutId: 0,
      areNotEnoughCharactersProvided: false,
    };
  },
  methods: {
    /**
     * Sets the wereKeysPressed variable to true
     */
    noteThatAKeyWasPressed() {
      this.wereKeysPressed = true;
    },

    /**
     * Updates the local search string if the new search string is well-defined
     * @param currentSearchString the potentially new search string
     */
    saveCurrentSearchStringIfValid(currentSearchString: string | object) {
      if (currentSearchString && typeof currentSearchString === 'string') {
        this.latestValidSearchString = currentSearchString;
      }
    },

    /**
     * Called on button presses of the up/down keys and updates the index of the currently selected element.
     */
    getCurrentFocusedOptionIndex() {
      this.currentFocusedOptionIndex = this.autocomplete?.focusedOptionIndex ?? -1;
    },

    /**
     * Resets the currently selected index variable
     */
    setCurrentFocusedOptionIndexToDefault() {
      this.currentFocusedOptionIndex = -1;
    },

    /**
     * Called when an item is selected from the dropdown. Navigates to the company cockpit page for the selected company
     * @param event the click event
     * @param event.value the company that was clicked on
     */
    handleItemSelect(event: { value: BasicCompanyInformation }) {
      const companyIdOfSelectedItem = event.value.companyId;
      void router.push(`/companies/${companyIdOfSelectedItem}`);
    },

    /**
     * Called when enter is pressed in the search bar. Performs a company search with the new search bar text
     * if no specific company is highlighted
     */
    executeSearchIfNoItemFocused() {
      if (!this.areNotEnoughCharactersProvided && this.currentFocusedOptionIndex === -1 && this.wereKeysPressed) {
        this.autocomplete?.hide();
        this.$emit('search-confirmed', this.searchBarInput);
        void this.queryCompany();
      }
    },
    /**
     * Performs the company search if the parent component indicated it wants to receive the given chunk of the
     * complete search results
     * and the total number of records
     * @param chunkIndex the index of the requested chunk
     */
    async queryCompany(chunkIndex = 0) {
      if (this.emitSearchResultsArray) {
        const resultsArray = await this.getCompanies(chunkIndex);
        const totalNumberOfCompanies = await this.getTotalNumberOfCompanies();
        this.$emit('companies-received', resultsArray, chunkIndex, totalNumberOfCompanies);
      }
    },
    /**
     * Performs the company search if the parent component indicated it wants to receive the complete search results
     * @param chunkIndex the index of the requested chunk
     * @returns chunk of companies
     */
    async getCompanies(chunkIndex: number) {
      return await getCompanyDataForFrameworkDataSearchPage(
        this.searchBarInput,
        new Set(this.filter?.frameworkFilter),
        new Set(this.filter?.countryCodeFilter),
        new Set(this.filter?.sectorFilter),
        assertDefined(this.getKeycloakPromise)(),
        this.chunkSize,
        chunkIndex
      );
    },
    /**
     * Get the total number of copanies with the given filter
     * @returns total number of companies
     */
    async getTotalNumberOfCompanies() {
      return await getNumberOfCompaniesForFrameworkDataSearchPage(
        this.searchBarInput,
        new Set(this.filter?.frameworkFilter),
        new Set(this.filter?.countryCodeFilter),
        new Set(this.filter?.sectorFilter),
        assertDefined(this.getKeycloakPromise)()
      );
    },
    /**
     * This function is called to obtain search suggestions for the dropdown. Uses the Dataland API to search
     * companies by the current search bar input (and selected filters).
     * @param companyName the autocomplete suggestion event
     * @param companyName.query the query text entered into the search bar
     */
    async searchCompanyName(companyName: { query: string }) {
      if (
        areAllFiltersDeactivated(
          this.filter?.frameworkFilter,
          this.filter?.countryCodeFilter,
          this.filter?.sectorFilter
        )
      ) {
        this.autocompleteArray = await getCompanyDataForFrameworkDataSearchPageWithoutFilters(
          companyName.query,
          assertDefined(this.getKeycloakPromise)(),
          this.maxNumOfDisplayedAutocompleteEntries
        );
      } else {
        this.autocompleteArray = await getCompanyDataForFrameworkDataSearchPage(
          companyName.query,
          new Set(this.filter?.frameworkFilter),
          new Set(this.filter?.countryCodeFilter),
          new Set(this.filter?.sectorFilter),
          assertDefined(this.getKeycloakPromise)(),
          this.maxNumOfDisplayedAutocompleteEntries,
          0
        );
      }
      this.autocompleteArrayDisplayed = this.autocompleteArray;
    },

    /**
     * Validates the current search bar input. If there are only one or two characters typed, an error message
     * shall be rendered asking the user to provide at least three characters.
     */
    validateSearchBarInput() {
      clearTimeout(this.notEnoughCharactersWarningTimeoutId);
      const areThereNotEnoughCharacters = this.searchBarInput.length > 0 && this.searchBarInput.length < 3;

      if (areThereNotEnoughCharacters) {
        this.notEnoughCharactersWarningTimeoutId = setTimeout(() => {
          this.areNotEnoughCharactersProvided = true;
        }, 1000);
      } else {
        this.areNotEnoughCharactersProvided = false;
      }
    },
  },
});

/**
 * Checks if all filteres are deactivated. Is used for triggering a special case function
 * @param frameworkFilter selection options of framework filter
 * @param countryCodeFilter selection options of country code filter
 * @param sectorFilter selection options of sector filter
 * @returns boolean value representing check result
 */
function areAllFiltersDeactivated(
  frameworkFilter: Array<DataTypeEnum>,
  countryCodeFilter: Array<string>,
  sectorFilter: Array<string>
): boolean {
  return !(frameworkFilter.length + countryCodeFilter.length + sectorFilter.length);
}
</script>
<style scoped>
.text-primary {
  color: var(--main-color);
}

.text-danger {
  color: var(--fk-color-error);
  font-size: var(--font-size-xs);
}
</style>
