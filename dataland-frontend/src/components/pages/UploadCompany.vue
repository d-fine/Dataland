<template>
  <AuthenticationWrapper>
    <TheHeader />
    <UploaderRoleWrapper>
      <TheContent>
        <div class="grid">
          <CreateCompany />
        </div>
      </TheContent>
    </UploaderRoleWrapper>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateCompany from "@/components/forms/CreateCompany.vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import UploaderRoleWrapper from "@/components/wrapper/UploaderRoleWrapper.vue";

export default {
  name: "UploadCompany",
  components: {
    UploaderRoleWrapper,
    CreateCompany,
    TheContent,
    TheHeader,
    AuthenticationWrapper,
    DatalandFooter,
  },
};
</script>
