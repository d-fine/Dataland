<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <MarginWrapper class="text-left mt-2">
        <BackButton />
        <FrameworkDataSearchBar class="mt-2" v-model="currentInput" @companies-received="handleQueryCompany" />
      </MarginWrapper>
      <MarginWrapper>
        <div class="grid align-items-end">
          <div class="col-9">
            <CompanyInformation :companyID="companyID" />
          </div>
        </div>
      </MarginWrapper>
      <MarginWrapper bgClass="surface-800">
        <slot></slot>
      </MarginWrapper>
    </TheContent>
  </AuthenticationWrapper>
</template>

<script>
import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import BackButton from "@/components/general/BackButton";
import TheHeader from "@/components/generics/TheHeader";
import TheContent from "@/components/generics/TheContent";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";
import CompanyInformation from "@/components/pages/CompanyInformation";
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
    handleQueryCompany() {
      this.$router.push({ name: "Search Companies for Framework Data", query: { input: this.currentInput } });
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
