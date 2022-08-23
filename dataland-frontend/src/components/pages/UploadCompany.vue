<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <div class="grid">
        <CreateCompany />
      </div>
    </TheContent>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script>
import TheHeader from "@/components/generics/TheHeader";
import TheContent from "@/components/generics/TheContent";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";
import CreateCompany from "@/components/forms/CreateCompany";
import DatalandFooter from "@/components/general/DatalandFooter";

export default {
  name: "UploadCompany",
  components: {
    DatalandFooter,
    CreateCompany,
    TheContent,
    TheHeader,
    AuthenticationWrapper,
  },
};
</script>

<style lang="scss">
@import "src/assets/css/variables";
</style>
