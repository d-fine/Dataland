<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <div class="grid">
        <div class="col-12">
          <div class="grid">
            <RetrieveCompany />
          </div>
        </div>
      </div>
    </TheContent>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import RetrieveCompany from "@/components/forms/RetrieveCompany.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";

export default {
  name: "SearchCompanies",
  components: {
    TheContent,
    TheHeader,
    RetrieveCompany,
    AuthenticationWrapper,
    DatalandFooter,
  },
};
</script>
