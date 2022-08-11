<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <div class="grid">
        <CreateEUTaxonomyForFinancials />
      </div>
    </TheContent>
  </AuthenticationWrapper>
</template>

<script>
import TheHeader from "@/components/generics/TheHeader";
import TheContent from "@/components/generics/TheContent";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";
import CreateEUTaxonomyForFinancials from "@/components/forms/CreateEUTaxonomyForFinancials";

export default {
  name: "UploadEuTaxonomyDataForFinancials",
  components: {
    CreateEUTaxonomyForFinancials,
    TheContent,
    TheHeader,
    AuthenticationWrapper,
  },
};
</script>

<style lang="scss">
@import "src/assets/css/variables";
</style>
