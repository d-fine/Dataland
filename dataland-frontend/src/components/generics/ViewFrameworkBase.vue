<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="surface-800 min-h-screen">
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

<script>
import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import BackButton from "@/components/general/BackButton.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import { ApiClientProvider } from "@/services/ApiClients";
export default {
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
  props: {
    companyID: {
      type: String,
    },
    dataType: {
      type: String,
    },
  },
  methods: {
    handleSearchConfirm(searchTerm) {
      this.$router.push({
        name: "Search Companies for Framework Data",
        query: { input: searchTerm },
      });
    },
    async getDataIdToLoad() {
      try {
        const metaDataControllerApi = await new ApiClientProvider(this.getKeycloakPromise()).getMetaDataControllerApi();
        const apiResponse = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID, this.dataType);
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
  inject: ["getKeycloakPromise"],
  created() {
    this.getDataIdToLoad();
  },
  watch: {
    companyID() {
      this.getDataIdToLoad();
    },
  },
};
</script>
