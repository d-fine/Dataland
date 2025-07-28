<template>
  <div :class="wrapperClassBaseClasses + wrapperClassAdditions">
    <IconField class="col-12">
      <InputIcon class="pi pi-search" aria-hidden="true" style="z-index: 20; color: #958d7c" />
      <AutoComplete
        input-id="company_search_bar_standard"
        id="autocomplete"
        v-model="searchBarInput"
        fluid
        :suggestions="autocompleteArray"
        :min-length="3"
        option-label="companyName"
        :auto-option-focus="false"
        placeholder="Search company by name or identifier (e.g. PermID, LEI, ...)"
        :input-class="inputClass"
        panel-class="d-framework-searchbar-panel"
        append-to="self"
        @complete="searchCompanyName($event)"
        @item-select="$emit('selectCompany', $event.value)"
        @focus="$emit('focus')"
        @blur="$emit('blur')"
        :pt="autoCompletePassThrough"
      >
        <template #option="slotProps">
          <i class="pi pi-search pl-3 pr-3" aria-hidden="true" />
          <SearchResultHighlighter :text="slotProps.option.companyName" :searchString="latestValidSearchString" />
        </template>
        <template #footer>
          <ul class="p-autocomplete-items pt-0" v-if="autocompleteArray && autocompleteArray.length >= resultLimit">
            <li class="p-autocomplete-item">
              <span class="font-medium pl-3"> Only showing {{ resultLimit }} results, please refine your query.</span>
            </li>
          </ul>
        </template>
      </AutoComplete>
    </IconField>
  </div>
  <div class="mt-2">
    <span class="red-text" v-if="showNotEnoughCharactersWarning">Please type at least 3 characters</span>
  </div>
</template>

<script lang="ts">
import SearchResultHighlighter from '@/components/resources/frameworkDataSearch/SearchResultHighlighter.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { type CompanyIdAndName } from '@clients/backend';
import type Keycloak from 'keycloak-js';
import AutoComplete, { type AutoCompleteCompleteEvent } from 'primevue/autocomplete';
import { defineComponent, inject } from 'vue';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  name: 'CompaniesOnlySearchBar',
  components: { AutoComplete, SearchResultHighlighter, IconField, InputIcon },
  mounted() {
    if (window.innerWidth > 768) {
      const inputElement = document.getElementById('autocomplete')?.querySelector('.p-inputtext') as HTMLInputElement;
      inputElement.focus();
    }
  },
  beforeUnmount() {
    clearTimeout(this.notEnoughCharactersWarningTimeoutId);
  },

  data: function () {
    return {
      searchBarInput: '',
      latestValidSearchString: '',
      autocompleteArray: [] as Array<CompanyIdAndName>,
      resultLimit: 100,
      notEnoughCharactersWarningTimeoutId: 0,
      showNotEnoughCharactersWarning: false,
      wrapperClassBaseClasses: 'p-input-icon-left p-input-icon-right p-input-icon-align ',
    };
  },
  props: {
    wrapperClassAdditions: {
      type: String,
    },
    inputClass: {
      type: String,
      default: 'h-3rem d-framework-searchbar-input w-full',
    },
    autoCompletePassThrough: {
      type: Object,
      required: false,
    },
  },

  watch: {
    searchBarInput(newValue: string) {
      this.validateSearchBarInput();
      this.saveCurrentSearchStringIfValid(newValue);
    },
  },
  emits: ['focus', 'blur', 'selectCompany'],
  methods: {
    /**
     * The input string is stored in the variable latestValidSearchString if it is a string and not empty
     * @param currentSearchString input to be checked (can be of type string or object)
     */
    saveCurrentSearchStringIfValid(currentSearchString: string | object) {
      if (currentSearchString && typeof currentSearchString === 'string') {
        this.latestValidSearchString = currentSearchString;
      }
    },
    /**
     * Queries the getCompanies endpoint and writes the response to the variable autoCompleteArray
     * @param autoCompleteCompleteEvent object containing the search query for the getCompanies endpoint
     */
    async searchCompanyName(autoCompleteCompleteEvent: AutoCompleteCompleteEvent) {
      try {
        const companyDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).backendClients
          .companyDataController;
        const response = await companyDataControllerApi.getCompaniesBySearchString(
          autoCompleteCompleteEvent.query,
          this.resultLimit
        );
        this.autocompleteArray = response.data;
      } catch (error) {
        console.error(error);
      }
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
          this.showNotEnoughCharactersWarning = true;
        }, 1000);
      } else {
        this.showNotEnoughCharactersWarning = false;
      }
    },
  },
});
</script>
<style scoped>
.red-text {
  color: var(--red);
}
</style>
