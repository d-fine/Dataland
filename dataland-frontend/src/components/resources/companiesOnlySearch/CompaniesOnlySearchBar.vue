<template>
  <span class="p-fluid">
    <span class="p-input-icon-left p-input-icon-right">
      <i class="pi pi-search d-framework-searchbar-input-icon" aria-hidden="true" style="z-index: 20; color: #958d7c" />
      <AutoComplete
        inputId="company_search_bar_standard"
        ref="autocomplete"
        v-model="searchBarInput"
        :suggestions="autocompleteArray"
        :minLength="3"
        optionLabel="companyName"
        :autoOptionFocus="false"
        placeholder="Search company by name or PermID"
        inputClass="h-3rem d-framework-searchbar-input"
        panelClass="d-framework-searchbar-panel"
        style="z-index: 10"
        @complete="searchCompanyName($event)"
        @item-select="pushToChooseFrameworkForDataUploadPageForItem($event)"
      >
        <template #option="slotProps">
          <i class="pi pi-search pl-3 pr-3" aria-hidden="true" />
          <SearchResultHighlighter :text="slotProps.option.companyName" :searchString="latestValidSearchString" />
        </template>
      </AutoComplete>
    </span>
  </span>
</template>

<script lang="ts">
import AutoComplete, { AutoCompleteCompleteEvent, AutoCompleteItemSelectEvent } from "primevue/autocomplete";
import { CompanyIdAndName } from "@clients/backend";
import SearchResultHighlighter from "@/components/resources/frameworkDataSearch/SearchResultHighlighter.vue";
import { defineComponent, inject, ref } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { ApiClientProvider } from "@/services/ApiClients";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      autocomplete: ref<HTMLFormElement>(),
    };
  },
  name: "CompaniesOnlySearchBar",
  components: { AutoComplete, SearchResultHighlighter },

  mounted() {
    const autocompleteRefsObject = this.autocomplete?.$refs as Record<string, unknown>;
    const inputOfAutocompleteComponent = autocompleteRefsObject.focusInput as HTMLInputElement;
    inputOfAutocompleteComponent.focus();
  },

  data: function () {
    return {
      searchBarInput: "",
      latestValidSearchString: "",
      autocompleteArray: [] as Array<CompanyIdAndName>,
    };
  },

  watch: {
    searchBarInput(newValue: string) {
      this.saveCurrentSearchStringIfValid(newValue);
    },
  },

  methods: {
    /**
     * The input string is stored in the variable latestValidSearchString if it is a string and not empty
     * @param currentSearchString input to be checked (can be of type string or object)
     */
    saveCurrentSearchStringIfValid(currentSearchString: string | object) {
      if (currentSearchString && typeof currentSearchString === "string") {
        this.latestValidSearchString = currentSearchString;
      }
    },
    /**
     * Executes a router push to upload overview page of the given company
     * @param event object containing the stored company
     * @param event.value the stored company object
     */
    async pushToChooseFrameworkForDataUploadPageForItem(event: AutoCompleteItemSelectEvent) {
      await this.$router.push(
        `/companies/${assertDefined(assertDefined(event.value as CompanyIdAndName).companyId)}/frameworks/upload`
      );
    },
    /**
     * Queries the getCompanies endpoint and writes the response to the variable autoCompleteArray
     * @param autoCompleteCompleteEvent object containing the search query for the getCompanies endpoint
     */
    async searchCompanyName(autoCompleteCompleteEvent: AutoCompleteCompleteEvent) {
      try {
        const companyDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getCompanyDataControllerApi();
        const response = await companyDataControllerApi.getCompaniesBySearchString(autoCompleteCompleteEvent.query);
        this.autocompleteArray = response.data;
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>
