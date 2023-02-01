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
        optionLabel="companyInformation.companyName"
        :autoOptionFocus="false"
        placeholder="Search company by name or PermID"
        inputClass="h-3rem d-framework-searchbar-input"
        panelClass="d-framework-searchbar-panel"
        style="z-index: 10"
        @complete="searchCompanyName"
        @item-select="pushToChooseFrameworkForDataUploadPageForItem"
      >
        <template #option="slotProps">
          <i class="pi pi-search pl-3 pr-3" aria-hidden="true" />
          <SearchResultHighlighter
            :text="slotProps.option.companyInformation.companyName"
            :searchString="latestValidSearchString"
          />
        </template>
      </AutoComplete>
    </span>
  </span>
</template>

<script lang="ts">
import AutoComplete from "primevue/autocomplete";
import { StoredCompany } from "@clients/backend";
import SearchResultHighlighter from "@/components/resources/frameworkDataSearch/SearchResultHighlighter.vue";
import { defineComponent, inject, ref } from "vue";
import Keycloak from "keycloak-js";
import { useRoute } from "vue-router";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { ApiClientProvider } from "@/services/ApiClients";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      autocomplete: ref(),
    };
  },
  name: "ComapniesOnlySearchBar",
  components: { AutoComplete, SearchResultHighlighter },

  mounted() {
    // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
    this.autocomplete.$refs.focusInput.focus();
  },

  data: function () {
    return {
      searchBarInput: "",
      latestValidSearchString: "",
      autocompleteArray: [] as Array<object>,
      route: useRoute(),
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
     *
     * @param currentSearchString input to be checked (can be of type string or object)
     */
    saveCurrentSearchStringIfValid(currentSearchString: string | object) {
      if (currentSearchString && typeof currentSearchString === "string") {
        this.latestValidSearchString = currentSearchString;
      }
    },
    /**
     * Executes a router push to upload overview page of the given company
     *
     * @param event object containing the stored company
     * @param event.value the companyId to be pushed to
     */
    pushToChooseFrameworkForDataUploadPageForItem(event: { value: StoredCompany }) {
      void this.$router.push(`/companies/${event.value.companyId}/frameworks/upload`);
    },
    /**
     * Queries the getCompanies endpoint and writes the response to the variable autoCompleteArray
     *
     * @param companyName object containing the search query for the getCompanies endpoint
     * @param companyName.query the query for the getCompany endpoint
     */
    async searchCompanyName(companyName: { query: string }) {
      try {
        const companyDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getCompanyDataControllerApi();
        const response = await companyDataControllerApi.getCompanies(companyName.query);
        this.autocompleteArray = response.data;
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>
