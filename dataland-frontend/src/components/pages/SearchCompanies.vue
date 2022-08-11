<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <div class="grid">
        <div class="col-12">
          <div class="grid">
            <RetrieveCompany />
            <RetrieveSkyminder />
          </div>
        </div>
      </div>
    </TheContent>
  </AuthenticationWrapper>
</template>

<script>
import RetrieveSkyminder from "@/components/forms/RetrieveSkyminder";
import RetrieveCompany from "@/components/forms/RetrieveCompany";
import TheHeader from "@/components/structure/TheHeader";
import TheContent from "@/components/structure/TheContent";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";

export default {
  name: "SearchData",
  components: {
    TheContent,
    TheHeader,
    RetrieveCompany,
    RetrieveSkyminder,
    AuthenticationWrapper,
  },
};
</script>
