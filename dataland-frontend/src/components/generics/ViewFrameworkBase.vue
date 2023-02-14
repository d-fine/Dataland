<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section min-h-screen">
      <MarginWrapper class="text-left surface-0" style="margin-right: 0rem">
        <BackButton />
        <FrameworkDataSearchBar class="mt-2" ref="frameworkDataSearchBar" @search-confirmed="handleSearchConfirm" />
      </MarginWrapper>
      <MarginWrapper class="surface-0" style="margin-right: 0rem">
        <div class="grid align-items-end">
          <div class="col-9">
            <CompanyInformation :companyID="companyID" />
          </div>
        </div>
      </MarginWrapper>
      <MarginWrapper v-if="noFailure" class="text-left surface-0" style="margin-right: 0rem">
        <Dropdown
          id="frameworkDataDropdown"
          v-model="chosenDataTypeInDropdown"
          :options="dataTypesInDropdown"
          optionLabel="label"
          optionValue="value"
          :placeholder="humanizeString(dataType)"
          aria-label="Choose framework"
          class="fill-dropdown"
          dropdownIcon="pi pi-angle-down"
          @change="redirectToViewPageForChosenFramework"
        />
      </MarginWrapper>
      <MarginWrapper v-if="noFailure" style="margin-right: 0rem">
        <slot></slot>
      </MarginWrapper>
    </TheContent>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import BackButton from "@/components/general/BackButton.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { defineComponent, inject, ref } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Dropdown from "primevue/dropdown";
import { humanizeString } from "@/utils/StringHumanizer";
import { ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS } from "@/utils/Constants";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import { DatasetQualityStatus } from "@clients/backend";

export default defineComponent({
  name: "ViewFrameworkBase",
  components: {
    TheContent,
    TheHeader,
    BackButton,
    MarginWrapper,
    FrameworkDataSearchBar,
    Dropdown,
    AuthenticationWrapper,
    CompanyInformation,
    DatalandFooter,
  },
  emits: ["updateDataId"],
  props: {
    companyID: {
      type: String,
    },
    dataType: {
      type: String,
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      frameworkDataSearchBar: ref<typeof FrameworkDataSearchBar>(),
    };
  },
  data() {
    return {
      chosenDataTypeInDropdown: "",
      dataTypesInDropdown: [] as { label: string; value: string }[],
      humanizeString: humanizeString,
      windowScrollHandler: (): void => {
        this.handleScroll();
      },
      noFailure: true,
    };
  },
  created() {
    void this.getAllDataIdsForFrameworkAndEmitThem();
    window.addEventListener("scroll", this.windowScrollHandler);
  },
  methods: {
    /**
     * Hides the dropdown of the Autocomplete-component
     */
    handleScroll() {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.frameworkDataSearchBar?.$refs.autocomplete.hide();
    },
    /**
     * Visits the framework view page for the framework which was chosen in the dropdown
     */
    redirectToViewPageForChosenFramework() {
      void this.$router.push(`/companies/${this.companyID as string}/frameworks/${this.chosenDataTypeInDropdown}`);
    },
    /**
     * Handles the "search-confirmed" event of the search bar by visiting the search page with the query param set to
     * the search term provided by the event.
     *
     * @param searchTerm The search term provided by the "search-confirmed" event of the search bar
     */
    async handleSearchConfirm(searchTerm: string) {
      await this.$router.push({
        name: "Search Companies for Framework Data",
        query: { input: searchTerm },
      });
    },

    /**
     * Checks if the provided data type already exists in the vue components dataTypesInDropdown-array and adds it if not.
     *
     * @param dataType The data type to check for
     */
    appendDistinctDataTypeToDropdownOptionsIfNotIncludedYet(dataType: string) {
      if (!this.dataTypesInDropdown.some((dataTypeObject) => dataTypeObject.value === dataType)) {
        this.dataTypesInDropdown.push({ label: humanizeString(dataType), value: dataType });
      }
    },

    /**
     * Goes through all data meta info for the currently viewed company and does two things. First it saves all distinct
     * data types into the vue components dataTypesInDropdown-array. Second it collects all data IDs for data of the
     * currently selected framework type and emits them.
     */
    async getAllDataIdsForFrameworkAndEmitThem() {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const apiResponse = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID);
        const listOfDataMetaInfoForCompany = apiResponse.data;
        const listOfDataIdsToEmit = [] as string[];
        listOfDataMetaInfoForCompany
          .filter((dataMetaInfo) => dataMetaInfo.qualityStatus == DatasetQualityStatus.Accepted)
          .forEach((dataMetaInfo) => {
            if (ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS.includes(dataMetaInfo.dataType)) {
              this.appendDistinctDataTypeToDropdownOptionsIfNotIncludedYet(dataMetaInfo.dataType);
            }
            if (dataMetaInfo.dataType === this.dataType) {
              listOfDataIdsToEmit.push(dataMetaInfo.dataId);
            }
          });
        this.$emit("updateDataId", listOfDataIdsToEmit);
      } catch (error) {
        this.noFailure = false;
        console.error(error);
      }
    },
  },
  watch: {
    companyID() {
      void this.getAllDataIdsForFrameworkAndEmitThem();
    },
  },
});
</script>
