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
            v-model="searchBarInput"
            ref="autocomplete"
            inputClass="h-3rem d-framework-searchbar-input"
            field="companyName"
            style="z-index: 10"
            placeholder="Search company by name or PermID"
            @complete="searchCompanyName"
            @item-select="handleItemSelect"
            @keyup.enter="handleKeyupEnter"
            panelClass="d-framework-searchbar-panel"
          >
            <template #item="slotProps">
              <i class="pi pi-search pl-3 pr-3" aria-hidden="true" />
              <SearchResultHighlighter :text="slotProps.item.companyName" :searchString="searchBarInput" />
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

<script lang="ts">
import AutoComplete from "primevue/autocomplete";
import SearchResultHighlighter from "@/components/resources/frameworkDataSearch/SearchResultHighlighter.vue";
import {
  DataSearchStoredCompany,
  getCompanyDataForFrameworkDataSearchPage,
  getRouterLinkTargetFramework,
} from "@/utils/SearchCompaniesForFrameworkDataPageDataRequester";
import { defineComponent, inject, ref } from "vue";
import { DataTypeEnum } from "@clients/backend";
import Keycloak from "keycloak-js";
import { useRoute } from "vue-router";
import { assertDefined } from "@/utils/TypeScriptUtils";

export interface FrameworkDataSearchFilterInterface {
  companyNameFilter: string;
  frameworkFilter: Array<DataTypeEnum>;
  countryCodeFilter: Array<string>;
  sectorFilter: Array<string>;
}

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      autocomplete: ref(),
    };
  },
  name: "FrameworkDataSearchBar",
  components: { AutoComplete, SearchResultHighlighter },

  emits: ["companies-received", "search-confirmed"],

  props: {
    searchBarName: {
      type: String,
      default: "framework_data_search_bar_standard",
    },
    filter: {
      type: Object as () => FrameworkDataSearchFilterInterface,
      default(): FrameworkDataSearchFilterInterface {
        return {
          companyNameFilter: "",
          frameworkFilter: Object.values(DataTypeEnum).filter(
            (frameworkName) => ["lksg", "sfdr"].indexOf(frameworkName) === -1
          ),
          sectorFilter: [],
          countryCodeFilter: [],
        };
      },
    },
    maxNumAutoCompleteEntries: {
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
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.autocomplete.focus();
    }
  },

  watch: {
    searchBarName() {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.autocomplete.focus();
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
      searchBarInput: "",
      autocompleteArray: [] as Array<object>,
      autocompleteArrayDisplayed: [] as Array<object>,
      loading: false,
      route: useRoute(),
    };
  },
  methods: {
    handleItemSelect(event: { value: DataSearchStoredCompany }) {
      void this.$router.push(getRouterLinkTargetFramework(event.value));
    },
    handleKeyupEnter() {
      this.$emit("search-confirmed", this.searchBarInput);
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.autocomplete.hideOverlay();
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.autocomplete.$refs.input.blur();
      void this.queryCompany();
    },
    async queryCompany() {
      if (this.emitSearchResultsArray) {
        this.loading = true;
        const resultsArray = await getCompanyDataForFrameworkDataSearchPage(
          this.searchBarInput,
          false,
          new Set(this.filter?.frameworkFilter),
          new Set(this.filter?.countryCodeFilter),
          new Set(this.filter?.sectorFilter),
          assertDefined(this.getKeycloakPromise)()
        );
        this.$emit("companies-received", resultsArray);
        this.loading = false;
      }
    },
    async searchCompanyName(companyName: { query: string }) {
      this.loading = true;
      this.autocompleteArray = await getCompanyDataForFrameworkDataSearchPage(
        companyName.query,
        true,
        new Set(this.filter?.frameworkFilter),
        new Set(this.filter?.countryCodeFilter),
        new Set(this.filter?.sectorFilter),
        assertDefined(this.getKeycloakPromise)()
      );
      this.autocompleteArrayDisplayed = this.autocompleteArray.slice(0, this.maxNumAutoCompleteEntries);
      this.loading = false;
    },
  },
});
</script>
