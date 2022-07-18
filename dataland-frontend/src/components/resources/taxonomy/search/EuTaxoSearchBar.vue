<template>
  <MarginWrapper>
    <div class="grid">
      <div class="col-8 text-left">
        <span class="p-fluid">
          <span class="p-input-icon-left p-input-icon-right">
            <i class="pi pi-search" aria-hidden="true" style="z-index: 20; color: #958d7c" />
            <i v-if="loading" class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
            <i v-else aria-hidden="true" />
            <AutoComplete
              :suggestions="autocompleteArrayDisplayed"
              :name="taxoSearchBarName"
              :modelValue="modelValue"
              ref="autocomplete"
              inputClass="h-3rem"
              field="companyName"
              style="z-index: 10"
              placeholder="Search company by name or PermID"
              @input="handleInput"
              @complete="searchCompanyName"
              @item-select="handleItemSelect"
              @keyup.enter="handleKeyupEnter"
            >
              <template #footer>
                <ul
                  class="p-autocomplete-items pt-0"
                  v-if="autocompleteArray && autocompleteArray.length >= maxNumAutoCompleteEntries"
                >
                  <li class="p-autocomplete-item text-primary font-semibold" @click="handleViewAllResults">
                    View all results.
                  </li>
                </ul>
              </template>
            </AutoComplete>
          </span>
        </span>
      </div>
    </div>
  </MarginWrapper>
</template>

<script>
import AutoComplete from "primevue/autocomplete";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import { searchTaxonomyPageCompanyDataRequester } from "@/utils/SearchTaxonomyPageCompanyDataRequester";

export default {
  name: "EuTaxoSearchBar",
  components: { AutoComplete, MarginWrapper },

  emits: ["companies-received", "update:modelValue", "rendered"],

  props: {
    taxoSearchBarName: {
      type: String,
      default: "eu_taxonomy_search_bar_standard",
    },
    modelValue: {
      type: String,
      default: null,
    },
    maxNumAutoCompleteEntries: {
      type: Number,
      default: 3,
    },
  },

  mounted() {
    this.$emit("rendered", true);
  },

  watch: {
    taxoSearchBarName() {
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

  inject: ["getKeycloakInitPromise", "keycloak_init"],
  methods: {
    handleInput(inputEvent) {
      this.currentInput = inputEvent.target.value;
      this.$emit("update:modelValue", this.currentInput);
    },

    handleItemSelect(event) {
      this.$router.push(`/companies/${event.value.companyId}/eutaxonomies`);
    },
    handleKeyupEnter() {
      this.queryCompany(this.$refs.autocomplete.modelValue);
      this.$refs.autocomplete.hideOverlay();
    },
    handleViewAllResults() {
      this.queryCompany(this.currentInput);
      this.$refs.autocomplete.hideOverlay();
    },

    async filterByIndex(stockIndex) {
      const resultsArray = await searchTaxonomyPageCompanyDataRequester(
        "",
        stockIndex,
        false,
        this.getKeycloakInitPromise(),
        this.keycloak_init
      );
      this.$emit("companies-received", resultsArray);
    },

    async queryCompany(companyName) {
      this.loading = true;
      const resultsArray = await searchTaxonomyPageCompanyDataRequester(
        companyName,
        "",
        false,
        this.getKeycloakInitPromise(),
        this.keycloak_init
      );
      this.$emit("companies-received", resultsArray);
      this.loading = false;
    },

    async searchCompanyName(companyName) {
      this.loading = true;
      this.autocompleteArray = await searchTaxonomyPageCompanyDataRequester(
        companyName.query,
        "",
        true,
        this.getKeycloakInitPromise(),
        this.keycloak_init
      );
      this.autocompleteArrayDisplayed = this.autocompleteArray.slice(0, this.maxNumAutoCompleteEntries);
      this.loading = false;
    },
  },

  unmounted() {
    window.removeEventListener("scroll", this.handleScroll);
  },
};
</script>
