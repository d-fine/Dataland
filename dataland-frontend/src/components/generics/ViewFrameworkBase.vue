<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section min-h-screen">
      <MarginWrapper class="text-left mt-2 surface-0">
        <BackButton />
        <FrameworkDataSearchBar class="mt-2" @search-confirmed="handleSearchConfirm" />
      </MarginWrapper>
      <MarginWrapper class="surface-0">
        <div class="grid align-items-end">
          <div class="col-9">
            <CompanyInformation :companyID="companyID" />
          </div>
        </div>
      </MarginWrapper>
      <MarginWrapper class="text-left surface-0">
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
          @change="setFramework"
        />
      </MarginWrapper>
      <MarginWrapper>
        <slot></slot>
      </MarginWrapper>
    </TheContent>
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
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { DataMetaInformation } from "@clients/backend";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Dropdown from "primevue/dropdown";
import { humanizeString } from "@/utils/StringHumanizer";

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
    };
  },
  data() {
    return {
      currentInput: "",
      chosenDataTypeInDropdown: "",
      dataTypesInDropdown: [] as { label: string; value: string }[],
      humanizeString: humanizeString,
    };
  },
  created() {
    void this.getAllFrameworkDataToLoad();
  },
  methods: {
    setFramework() {
      void this.$router.push(`/companies/${this.companyID as string}/frameworks/${this.chosenDataTypeInDropdown}`);
    },
    handleSearchConfirm(searchTerm: string) {
      return this.$router.push({
        name: "Search Companies for Framework Data",
        query: { input: searchTerm },
      });
    },

    appendDistinctDataTypeToDropdownOptionsIfNotIncludedYet(dataMetaInfo: DataMetaInformation) {
      if (!this.dataTypesInDropdown.some((dataTypeObject) => dataTypeObject.value === dataMetaInfo.dataType)) {
        this.dataTypesInDropdown.push({ label: humanizeString(dataMetaInfo.dataType), value: dataMetaInfo.dataType });
      }
    },

    async getAllFrameworkDataToLoad() {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const apiResponse = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID);
        const listOfDataMetaInfoForCompany = apiResponse.data;
        const listOfDataIdsToEmit = [] as string[];
        listOfDataMetaInfoForCompany.forEach((dataMetaInfo) => {
          this.appendDistinctDataTypeToDropdownOptionsIfNotIncludedYet(dataMetaInfo);
          if (dataMetaInfo.dataType === this.dataType) {
            listOfDataIdsToEmit.push(dataMetaInfo.dataId);
          }
        });
        if (listOfDataIdsToEmit.length) {
          this.$emit("updateDataId", listOfDataIdsToEmit);
        } else {
          this.$emit("updateDataId", null);
        }
      } catch (error) {
        console.error(error);
      }
    },
  },
  watch: {
    companyID() {
      void console.log("change"); //this.getAllFrameworkDataToLoad(); // TODO why do we need this watcher?
    },
  },
});
</script>
