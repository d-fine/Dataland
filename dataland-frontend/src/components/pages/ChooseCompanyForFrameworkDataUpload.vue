<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <BackButton id="backButton" label="MY DATASETS" />
      <Card class="col-12 text-left page-wrapper-card">
        <template #title>New Dataset - Company</template>
        <template #content>
          <div class="grid">
            <div class="col-9">
              <div id="option1Container" class="grid bottom-border-section">
                <div id="option1Label" class="col-3 p-3">
                  <h4 id="option1Title">Option 01</h4>
                  <h3>Select a company</h3>
                  <p>Select the company for which you would like to add a new dataset.</p>
                </div>
                <div class="col-9 d-card">
                  <div class="mb-3">
                    <span>Type at least 3 characters to search for companies on Dataland:</span>
                  </div>
                  <CompaniesOnlySearchBar />
                  <div class="mt-6">
                    <span>Can't find the company? </span>
                    <span @click="autoScrollToCreateACompanyForm" class="cursor-pointer text-primary font-semibold"
                      >Add it.</span
                    >
                  </div>
                </div>
              </div>

              <div id="option2Container" ref="option2Container" class="grid">
                <div id="option2Label" class="col-3 p-3">
                  <h4 id="option2Title">Option 02</h4>
                  <h3>Add a new company</h3>
                  <p>
                    If you want to add a dataset for a new company, you first have to create the company. To create a
                    new company, all mandatory * fields must be filled.
                  </p>
                </div>
                <div id="createCompanyForm" class="col-9 d-card">
                  <CreateCompany @companyCreated="handleCompanyCreated" />
                </div>
              </div>
            </div>
          </div>
        </template>
      </Card>
    </TheContent>
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import BackButton from "@/components/general/BackButton.vue";
import Card from "primevue/card";
import CreateCompany from "@/components/forms/CreateCompany.vue";
import CompaniesOnlySearchBar from "@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue";
import { TIME_DELAY_BETWEEN_UPLOAD_AND_REDIRECT_IN_MS } from "@/utils/Constants";

export default defineComponent({
  name: "Choose Company",
  components: {
    AuthenticationWrapper,
    BackButton,
    TheHeader,
    TheContent,
    CompaniesOnlySearchBar,
    CreateCompany,
    Card,
  },
  setup() {
    return {
      option2Container: ref(),
    };
  },
  props: {
    companyID: {
      type: String,
    },
  },

  methods: {
    autoScrollToCreateACompanyForm() {
      const option2Container = this.option2Container;
      if (option2Container) {
        option2Container.scrollIntoView({ behavior: "smooth" });
      }
    },
    handleCompanyCreated(companyId: string) {
      setTimeout(() => {
        void this.$router.push(`/companies/${companyId}/frameworks/upload`);
      }, TIME_DELAY_BETWEEN_UPLOAD_AND_REDIRECT_IN_MS);
    },
  },
});
</script>
