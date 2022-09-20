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
import RetrieveSkyminder from "@/components/forms/RetrieveSkyminder.vue";
import RetrieveCompany from "@/components/forms/RetrieveCompany.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";

export default {
  name: "SearchCompanies",
  components: {
    TheContent,
    TheHeader,
    RetrieveCompany,
    RetrieveSkyminder,
    AuthenticationWrapper,
  },
};
</script>
