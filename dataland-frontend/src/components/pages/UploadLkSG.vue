<template>
  <AuthenticationWrapper>
    <TheHeader />
    <UploaderRoleWrapper>
        <MarginWrapper>
          <BackButton id="backButton" />
          <CompanyInformation :companyID="companyID" />
        </MarginWrapper>
        <CreateLksgDataset :companyID="companyID" />
    </UploaderRoleWrapper>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateLksgDataset from "@/components/forms/CreateLksgDataset.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import TheFooter from "@/components/general/TheFooter.vue";
import BackButton from "@/components/general/BackButton.vue";
import UploaderRoleWrapper from "@/components/wrapper/UploaderRoleWrapper.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";

export default {
  name: "UploadLkSG",
  components: {
    MarginWrapper,
    UploaderRoleWrapper,
    CreateLksgDataset,
    TheContent,
    TheHeader,
    AuthenticationWrapper,
    CompanyInformation,
    TheFooter,
    BackButton,
  },
  props: {
    companyID: {
      type: String,
    },
  },
};
</script>
