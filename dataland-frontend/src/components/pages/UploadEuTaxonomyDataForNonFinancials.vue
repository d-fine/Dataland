<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <div class="grid">
        <CreateEUTaxonomyForNonFinancials />
      </div>
    </TheContent>
  </AuthenticationWrapper>
</template>

<script>
import TheHeader from "@/components/generics/TheHeader";
import TheContent from "@/components/generics/TheContent";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";
import CreateEUTaxonomyForNonFinancials from "@/components/forms/CreateEUTaxonomyForNonFinancials";

export default {
  name: "UploadEutaxonomyDataForNonFinancials",
  components: {
    CreateEUTaxonomyForNonFinancials,
    TheContent,
    TheHeader,
    AuthenticationWrapper,
  },
};
</script>

<style lang="scss">
@import "src/assets/css/variables";
</style>
