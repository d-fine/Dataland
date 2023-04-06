<template>
  <AuthenticationWrapper>
    <TheHeader />
    <UploaderRoleWrapper>
      <BackButton id="backButton" class="mt-2 pl-3" />
      <CompanyInformation :companyID="companyID" />
      <CreateLksgDataset :companyID="companyID" />
    </UploaderRoleWrapper>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateLksgDataset from "@/components/forms/CreateLksgDataset.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import TheFooter from "@/components/general/TheFooter.vue";
import BackButton from "@/components/general/BackButton.vue";
import UploaderRoleWrapper from "@/components/wrapper/UploaderRoleWrapper.vue";

export default {
  name: "UploadLkSG",
  components: {
    UploaderRoleWrapper,
    CreateLksgDataset,
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
