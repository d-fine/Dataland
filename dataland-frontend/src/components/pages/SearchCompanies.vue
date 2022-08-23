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
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script>
import RetrieveSkyminder from "@/components/forms/RetrieveSkyminder";
import RetrieveCompany from "@/components/forms/RetrieveCompany";
import TheHeader from "@/components/generics/TheHeader";
import TheContent from "@/components/generics/TheContent";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";
import DatalandFooter from "@/components/general/DatalandFooter";

export default {
  name: "SearchCompanies",
  components: {
    DatalandFooter,
    TheContent,
    TheHeader,
    RetrieveCompany,
    RetrieveSkyminder,
    AuthenticationWrapper,
  },
};
</script>
