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
          v-model="currentDataType"
          :options="dataTypesList"
          optionLabel="label"
          optionValue="value"
          :placeholder="humanizeString(dataType)"
          aria-label="Choose framework"
          class="fill-dropdown"
          dropdownIcon="pi pi-angle-down"
          @change="setFrameworkData"
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
      currentDataType: "",
      dataTypesList: [] as { label: string; value: string }[],
      humanizeString: humanizeString,
    };
  },
  created() {
    void this.getAllFrameworkDataToLoad();
  },
  methods: {
    setFrameworkData() {
      void this.$router.push(`/companies/${this.companyID as string}/frameworks/${this.currentDataType}`);
    },
    handleSearchConfirm(searchTerm: string) {
      return this.$router.push({
        name: "Search Companies for Framework Data",
        query: { input: searchTerm },
      });
    },
    async getAllFrameworkDataToLoad() {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const apiResponse = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID);
        const listOfDataMetaInfoForCompany = apiResponse.data;
        const listOfDataMetaInfoToEmit = [] as DataMetaInformation[];
        listOfDataMetaInfoForCompany.forEach((dataMetaInfo) => {
          if (!this.dataTypesList.some((dataTypeObject) => dataTypeObject.value === dataMetaInfo.dataType)) {
            this.dataTypesList.push({
              label: humanizeString(dataMetaInfo.dataType)
                ? humanizeString(dataMetaInfo.dataType)
                : dataMetaInfo.dataType,
              value: dataMetaInfo.dataType,
            });
          }
          if (dataMetaInfo.dataType === this.dataType) {
            listOfDataMetaInfoToEmit.push(dataMetaInfo);
          }
        });
        if (listOfDataMetaInfoToEmit.length) {
          this.$emit("updateDataId", listOfDataMetaInfoToEmit);
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
      void this.getAllFrameworkDataToLoad(); // TODO why do we need this watcher?
    },
  },
});
</script>
