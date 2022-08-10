<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <div class="grid">
        <CreateCompany />
      </div>
    </TheContent>
  </AuthenticationWrapper>
</template>

<script>
import TheHeader from "@/components/structure/TheHeader";
import TheContent from "@/components/structure/TheContent";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";
import CreateCompany from "@/components/forms/CreateCompany";

export default {
  name: "CreateData",
  components: {
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
