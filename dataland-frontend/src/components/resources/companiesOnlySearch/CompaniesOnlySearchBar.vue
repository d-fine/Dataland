<template>
  <div :class="wrapperClass">
    <i :class="iconClass" aria-hidden="true" />
    <AutoComplete
      input-id="company_search_bar_standard"
      ref="autocomplete"
      v-model="searchBarInput"
      :suggestions="autocompleteArray"
      :min-length="3"
      option-label="companyName"
      :auto-option-focus="false"
      placeholder="Search company by name or identifier (e.g. PermID, LEI, ...)"
      :input-class="inputClass"
      panel-class="d-framework-searchbar-panel search__autocomplete"
      append-to="self"
      @complete="searchCompanyName($event)"
      @item-select="$emit('selectCompany', $event.value)"
      @focus="$emit('focus')"
      @blur="$emit('blur')"
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
  </div>
</template>

<script lang="ts">
import AutoComplete, { type AutoCompleteCompleteEvent } from 'primevue/autocomplete';
import { type CompanyIdAndName } from '@clients/backend';
import SearchResultHighlighter from '@/components/resources/frameworkDataSearch/SearchResultHighlighter.vue';
import { defineComponent, inject, ref } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { ApiClientProvider } from '@/services/ApiClients';

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
      autocomplete: ref<HTMLFormElement>(),
    };
  },
  name: 'CompaniesOnlySearchBar',
  components: { AutoComplete, SearchResultHighlighter },
  mounted() {
    const autocompleteRefsObject = this.autocomplete?.$refs as Record<string, unknown>;
    const inputOfAutocompleteComponent = autocompleteRefsObject.focusInput as HTMLInputElement;
    if (window.innerWidth > 768) {
      inputOfAutocompleteComponent.focus();
    }
  },

  data: function () {
    return {
      searchBarInput: '',
      latestValidSearchString: '',
      autocompleteArray: [] as Array<CompanyIdAndName>,
      resultLimit: 100,
    };
  },
  props: {
    wrapperClass: {
      type: String,
      default: 'p-fluid p-input-icon-left p-input-icon-right p-input-icon-align',
    },
    inputClass: {
      type: String,
      default: 'h-3rem d-framework-searchbar-input',
    },
    iconClass: {
      type: String,
      default: 'pi pi-search d-framework-searchbar-input-icon search-icon',
    },
  },

  watch: {
    searchBarInput(newValue: string) {
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
  },
});
</script>
<style scoped>
.p-input-icon-align {
  text-align: left;
}
.search-icon {
  z-index: 20;
  color: #958d7c;
}
</style>
