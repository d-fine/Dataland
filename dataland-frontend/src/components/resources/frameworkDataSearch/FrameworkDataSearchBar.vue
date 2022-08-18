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
          <i v-if="loading" class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
          <i v-else aria-hidden="true" />
          <AutoComplete
            :suggestions="autocompleteArrayDisplayed"
            :name="searchBarName"
            :modelValue="modelValue"
            ref="autocomplete"
            inputClass="h-3rem d-framework-searchbar-input"
            field="companyName"
            style="z-index: 10"
            placeholder="Search company by name or PermID"
            @input="handleInput"
            @complete="searchCompanyName"
            @item-select="handleItemSelect"
            @keyup.enter="handleKeyupEnter"
            panelClass="d-framework-searchbar-panel"
          >
            <template #item="slotProps">
              <i class="pi pi-search pl-3 pr-3" aria-hidden="true" />
              <SearchResultHighlighter :text="slotProps.item.companyName" :searchString="this.modelValue" />
            </template>
            <template #footer>
              <ul
                class="p-autocomplete-items pt-0"
                v-if="autocompleteArray && autocompleteArray.length >= maxNumAutoCompleteEntries"
              >
                <li class="p-autocomplete-item" @click="handleKeyupEnter">
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

<style>
.d-framework-searchbar-input-icon {
  padding-left: 0.75rem !important;
}

.d-framework-searchbar-input {
  padding-left: 3rem !important;
}

.d-framework-searchbar-panel {
  max-height: 500px !important;
}

.d-framework-searchbar-panel .p-autocomplete-items {
  padding: 0 !important;
}

.d-framework-searchbar-panel .p-autocomplete-item {
  height: 3.5rem !important;
  padding: 0 !important;
  display: flex;
  align-content: center;
  align-items: center;
}
</style>

<script>
import AutoComplete from "primevue/autocomplete";
import SearchResultHighlighter from "@/components/resources/frameworkDataSearch/SearchResultHighlighter";
import {
  getCompanyDataForFrameworkDataSearchPage,
  getRouterLinkTargetFramework,
} from "@/utils/SearchCompaniesForFrameworkDataPageDataRequester";

export default {
  name: "FrameworkDataSearchBar",
  components: { AutoComplete, SearchResultHighlighter },

  emits: ["companies-received", "update:modelValue", "rendered"],

  props: {
    searchBarName: {
      type: String,
      default: "framework_data_search_bar_standard",
    },
    modelValue: {
      type: String,
      default: null,
    },
    maxNumAutoCompleteEntries: {
      type: Number,
      default: 3,
    },
    frameworksToFilterFor: {
      type: Array,
      default: () => [],
    },
  },

  mounted() {
    this.$emit("rendered", true);
  },

  watch: {
    searchBarName() {
      this.$refs.autocomplete.focus();
    },
  },

  data() {
    return {
      autocompleteArray: [],
      autocompleteArrayDisplayed: null,
      loading: false,
      currentInput: null,
    };
  },

  inject: ["getKeycloakPromise"],
  methods: {
    handleInput(inputEvent) {
      this.currentInput = inputEvent.target.value;
      this.$emit("update:modelValue", this.currentInput);
    },

    handleItemSelect(event) {
      this.$router.push(this.getRouterLinkTargetFrameworkInt(event.value));
    },
    handleKeyupEnter() {
      this.queryCompany(this.currentInput);
      this.$refs.autocomplete.hideOverlay();
    },
    async queryCompany(companyName) {
      this.loading = true;
      const resultsArray = await getCompanyDataForFrameworkDataSearchPage(
        companyName,
        "",
        false,
        this.frameworksToFilterFor,
        this.getKeycloakPromise()
      );
      this.$emit("companies-received", resultsArray);
      this.loading = false;
    },
    async searchCompanyName(companyName) {
      this.loading = true;
      this.autocompleteArray = await getCompanyDataForFrameworkDataSearchPage(
        companyName.query,
        "",
        true,
        this.frameworksToFilterFor,
        this.getKeycloakPromise()
      );
      this.autocompleteArrayDisplayed = this.autocompleteArray.slice(0, this.maxNumAutoCompleteEntries);
      this.loading = false;
    },
    getRouterLinkTargetFrameworkInt(companyData) {
      return getRouterLinkTargetFramework(companyData, this.frameworksToFilterFor);
    },
  },

  unmounted() {
    window.removeEventListener("scroll", this.handleScroll);
  },
};
</script>
