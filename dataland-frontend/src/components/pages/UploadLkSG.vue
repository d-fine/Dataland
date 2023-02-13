<template>
  <AuthenticationWrapper>
    <TheHeader />
    <UploaderRoleWrapper>
      <TheContent>
        <BackButton id="backButton" />
        <CompanyInformation :companyID="companyID" />
        <CreateLksgDataset :companyID="companyID" />
      </TheContent>
    </UploaderRoleWrapper>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateLksgDataset from "@/components/forms/CreateLksgDataset.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import BackButton from "@/components/general/BackButton.vue";
import UploaderRoleWrapper from "@/components/wrapper/UploaderRoleWrapper.vue";

export default {
  name: "UploadLkSG",
  components: {
    UploaderRoleWrapper,
    CreateLksgDataset,
    TheContent,
    TheHeader,
    AuthenticationWrapper,
    CompanyInformation,
    DatalandFooter,
    BackButton,
  },
  props: {
    companyID: {
      type: String,
    },
  },
};
</script>
