<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <div class="grid">
        <CreateCompany />
        <CreateEUTaxonomyForNonFinancials />
      </div>
    </TheContent>
  </AuthenticationWrapper>
</template>

<script>
import CreateCompany from "@/components/forms/CreateCompany";
import TheHeader from "@/components/structure/TheHeader";
import TheContent from "@/components/structure/TheContent";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";
import CreateEUTaxonomyForNonFinancials from "@/components/forms/CreateEUTaxonomyForNonFinancials";

export default {
  name: "CreateData",
  components: {
    CreateEUTaxonomyForNonFinancials,
    TheContent,
    TheHeader,
    CreateCompany,
    AuthenticationWrapper,
  },
};
</script>

<style lang="scss">
@import "src/assets/css/variables";
</style>
