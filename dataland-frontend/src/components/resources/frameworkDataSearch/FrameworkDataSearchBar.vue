<template>
  <div class="grid">
    <div class="col-8 text-left">
      <span class="p-fluid">
        <span class="p-input-icon-left p-input-icon-right">
          <i
            class="pi pi-search d-framework-searchbar-input-icon"
            aria-hidden="true"
            style="z-index: 20; color: #958d7c"
          />
          <AutoComplete
            :inputId="searchBarId"
            ref="autocomplete"
            v-model="searchBarInput"
            :suggestions="autocompleteArrayDisplayed"
            optionLabel="companyName"
            :autoOptionFocus="false"
            placeholder="Search company by name or PermID"
            inputClass="h-3rem d-framework-searchbar-input"
            panelClass="d-framework-searchbar-panel"
            style="z-index: 10"
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
              <ul
                class="p-autocomplete-items pt-0"
                v-if="autocompleteArray && autocompleteArray.length >= maxNumOfDisplayedAutocompleteEntries"
              >
                <li class="p-autocomplete-item" @click="executeSearchIfNoItemFocused">
                  <span class="text-primary font-medium underline pl-3"> View all results </span>
                </li>
              </ul>
            </template>
          </AutoComplete>
        </span>
      </span>
    </div>
  </div>
</template>

<script lang="ts">
import AutoComplete from "primevue/autocomplete";
import SearchResultHighlighter from "@/components/resources/frameworkDataSearch/SearchResultHighlighter.vue";
import {
  DataSearchStoredCompany,
  FrameworkDataSearchFilterInterface,
  getCompanyDataForFrameworkDataSearchPage,
  getRouterLinkTargetFramework,
} from "@/utils/SearchCompaniesForFrameworkDataPageDataRequester";
import { defineComponent, inject, ref } from "vue";
import Keycloak from "keycloak-js";
import { useRoute } from "vue-router";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      autocomplete: ref<HTMLFormElement>(),
    };
  },
  name: "FrameworkDataSearchBar",
  components: { AutoComplete, SearchResultHighlighter },

  emits: ["companies-received", "search-confirmed"],

  props: {
    companyIdIfOnViewPage: {
      type: String,
    },
    searchBarId: {
      type: String,
      default: "framework_data_search_bar_standard",
    },
    filter: {
      type: Object as () => FrameworkDataSearchFilterInterface,
      default(): FrameworkDataSearchFilterInterface {
        return {
          companyNameFilter: "",
          frameworkFilter: ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE,
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
    this.searchBarInput = this.filter?.companyNameFilter ?? "";
    void this.queryCompany();
    if (!this.route.query.input) {
      this.focusOnSearchBar();
    }
  },

  watch: {
    searchBarId() {
      this.focusOnSearchBar();
    },
    searchBarInput(newValue: string) {
      this.saveCurrentSearchStringIfValid(newValue);
    },
    filter: {
      handler() {
        this.searchBarInput = this.filter?.companyNameFilter ?? "";
        void this.queryCompany();
      },
      deep: true,
    },
  },

  data: function () {
    return {
      wereKeysPressed: false,
      currentFocusedOptionIndex: -1,
      searchBarInput: "",
      latestValidSearchString: "",
      autocompleteArray: [] as Array<object>,
      autocompleteArrayDisplayed: [] as Array<object>,
      route: useRoute(),
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
      if (currentSearchString && typeof currentSearchString === "string") {
        this.latestValidSearchString = currentSearchString;
      }
    },

    /**
     * Called on button presses of the up/down keys and updates the index of the currently selected element.
     */
    getCurrentFocusedOptionIndex() {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.currentFocusedOptionIndex = this.autocomplete.focusedOptionIndex as number;
    },

    /**
     * Resets the currently selected index variable
     */
    setCurrentFocusedOptionIndexToDefault() {
      this.currentFocusedOptionIndex = -1;
    },

    /**
     * Focuses the search bar
     */
    focusOnSearchBar() {
      const autocompleteRefsObject = this.autocomplete?.$refs as Record<string, unknown>;
      const inputOfAutocompleteComponent = autocompleteRefsObject.focusInput as HTMLInputElement;
      inputOfAutocompleteComponent.focus();
    },

    /**
     * Called when an item is selected from the dropdown. Navigates to the view framework page for the selected company
     * @param event the click event
     * @param event.value the company that was clicked on
     */
    handleItemSelect(event: { value: DataSearchStoredCompany }) {
      if (this.companyIdIfOnViewPage != event.value.companyId) {
        void this.$router.push(getRouterLinkTargetFramework(event.value));
      } else {
        this.searchBarInput = event.value.companyName;
      }
    },

    /**
     * Called when enter is pressed in the search bar. Performs a company search with the new search bar text
     * if no specific company is highlighted
     */
    executeSearchIfNoItemFocused() {
      if (this.currentFocusedOptionIndex === -1 && this.wereKeysPressed) {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
        this.autocomplete.hide();
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
        this.autocomplete.$refs.focusInput.blur();
        this.$emit("search-confirmed", this.searchBarInput);
        void this.queryCompany();
      }
    },
    /**
     * Performs the company search if the parent component indicated it wants to receive the complete search results
     */
    async queryCompany() {
      if (this.emitSearchResultsArray) {
        const resultsArray = await getCompanyDataForFrameworkDataSearchPage(
          this.searchBarInput,
          false,
          new Set(this.filter?.frameworkFilter),
          new Set(this.filter?.countryCodeFilter),
          new Set(this.filter?.sectorFilter),
          this.getKeycloakPromise!()
        );
        this.$emit("companies-received", resultsArray);
      }
    },
    /**
     * This function is called to obtain search suggestions for the dropdown. Uses the Dataland API to search
     * companies by the current search bar input (and selected filters).
     * @param companyName the autocomplete suggestion event
     * @param companyName.query the query text entered into the search bar
     */
    async searchCompanyName(companyName: { query: string }) {
      this.autocompleteArray = await getCompanyDataForFrameworkDataSearchPage(
        companyName.query,
        true,
        new Set(this.filter?.frameworkFilter),
        new Set(this.filter?.countryCodeFilter),
        new Set(this.filter?.sectorFilter),
        this.getKeycloakPromise!()
      );
      this.autocompleteArrayDisplayed = this.autocompleteArray.slice(0, this.maxNumOfDisplayedAutocompleteEntries);
    },
  },
});
</script>
