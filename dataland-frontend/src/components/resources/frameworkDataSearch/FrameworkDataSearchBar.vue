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

<script lang="ts">
import AutoComplete from "primevue/autocomplete";
import SearchResultHighlighter from "@/components/resources/frameworkDataSearch/SearchResultHighlighter.vue";
import {
  DataSearchStoredCompany,
  getCompanyDataForFrameworkDataSearchPage,
  getRouterLinkTargetFramework,
} from "@/utils/SearchCompaniesForFrameworkDataPageDataRequester";
import { defineComponent, inject, ref } from "vue";
import { DataTypeEnum } from "build/clients/backend";
import Keycloak from "keycloak-js";
import { useRoute } from "vue-router";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      autocomplete: ref(),
    };
  },
  name: "FrameworkDataSearchBar",
  components: { AutoComplete, SearchResultHighlighter },

  emits: ["companies-received", "update:modelValue", "rendered", "search-confirmed"],

  props: {
    searchBarName: {
      type: String,
      default: "framework_data_search_bar_standard",
    },
    modelValue: {
      type: String,
      default: "",
    },
    maxNumAutoCompleteEntries: {
      type: Number,
      default: 3,
    },
    frameworksToFilterFor: {
      type: Array as () => Array<DataTypeEnum>,
      default: () => [],
    },
    countryCodesToFilterFor: {
      type: Array as () => Array<string>,
      default: () => [],
    },
    sectorsToFilterFor: {
      type: Array as () => Array<string>,
      default: () => [],
    },
    enableFullSearch: {
      type: Boolean,
      default: false,
    },
  },
  mounted() {
    this.queryCompany();
    if (!this.route.query.input) {
      this.autocomplete.focus();
    }
  },

  watch: {
    searchBarName() {
      this.autocomplete.focus();
    },
    frameworksToFilterFor: {
      handler() {
        this.queryCompany();
      },
      deep: true,
    },
    countryCodesToFilterFor: {
      handler() {
        this.queryCompany();
      },
      deep: true,
    },
    sectorsToFilterFor: {
      handler() {
        this.queryCompany();
      },
      deep: true,
    },
  },

  data: function () {
    return {
      autocompleteArray: [] as Array<object>,
      autocompleteArrayDisplayed: [] as Array<object>,
      loading: false,
      route: useRoute(),
    };
  },

  methods: {
    handleInput(inputEvent: { target: { value: string } }) {
      this.$emit("update:modelValue", inputEvent.target.value);
    },

    handleItemSelect(event: { value: DataSearchStoredCompany }) {
      this.$router.push(getRouterLinkTargetFramework(event.value));
    },
    handleKeyupEnter() {
      this.queryCompany();
      this.autocomplete.hideOverlay();
      this.autocomplete.$refs.input.blur();
      this.$emit("search-confirmed", this.modelValue);
    },
    async queryCompany() {
      if (this.getKeycloakPromise !== undefined && this.enableFullSearch) {
        this.loading = true;
        const resultsArray = await getCompanyDataForFrameworkDataSearchPage(
          this.modelValue,
          false,
          new Set(this.frameworksToFilterFor),
          new Set(this.countryCodesToFilterFor),
          new Set(this.sectorsToFilterFor),
          this.getKeycloakPromise()
        );
        this.$emit("companies-received", resultsArray);
        this.loading = false;
      }
    },
    async searchCompanyName(companyName: { query: string }) {
      if (this.getKeycloakPromise !== undefined) {
        this.loading = true;
        this.autocompleteArray = await getCompanyDataForFrameworkDataSearchPage(
          companyName.query,
          true,
          new Set(this.frameworksToFilterFor),
          new Set(this.countryCodesToFilterFor),
          new Set(this.sectorsToFilterFor),
          this.getKeycloakPromise()
        );
        this.autocompleteArrayDisplayed = this.autocompleteArray.slice(0, this.maxNumAutoCompleteEntries);
        this.loading = false;
      }
    },
  },
});
</script>
