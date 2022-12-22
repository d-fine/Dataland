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
import { DataTypeEnum } from "@clients/backend";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "ViewFrameworkBase",
  components: {
    TheContent,
    TheHeader,
    BackButton,
    MarginWrapper,
    FrameworkDataSearchBar,
    AuthenticationWrapper,
    CompanyInformation,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      currentInput: "",
    };
  },
  props: {
    companyID: {
      type: String,
    },
    dataType: {
      type: String,
    },
  },
  methods: {
    handleSearchConfirm(searchTerm: string) {
      return this.$router.push({
        name: "Search Companies for Framework Data",
        query: { input: searchTerm },
      });
    },
    async getDataIdToLoad() {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const apiResponse = await metaDataControllerApi.getListOfDataMetaInfo(
          this.companyID,
          this.dataType as DataTypeEnum
        );
        const listOfMetaData = apiResponse.data;
        if (listOfMetaData.length > 0) {
          this.$emit("updateDataId", listOfMetaData[0].dataId);
        } else {
          this.$emit("updateDataId", null);
        }
      } catch (error) {
        console.error(error);
      }
    },
  },
  created() {
    void this.getDataIdToLoad();
  },
  watch: {
    companyID() {
      void this.getDataIdToLoad();
    },
  },
});
</script>
