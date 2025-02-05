<template>
  <AuthenticationWrapper>
    <TheHeader />
    <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_UPLOADER">
      <TheContent>
        <BackButton id="backButton" label="BACK" class="pl-2" />
        <Card class="col-12 text-left page-wrapper-card">
          <template #title>New Dataset - Company</template>
          <template #content>
            <div class="grid">
              <div class="col-9">
                <div id="option1Container" :class="['grid', { 'bottom-border-section': isAdmin }]">
                  <div id="option1Label" class="col-3 p-3">
                    <h4 v-if="isAdmin" id="option1Title">Option 01</h4>
                    <h3>Select a company</h3>
                    <p>Select the company for which you would like to add a new dataset.</p>
                  </div>
                  <div class="col-9 d-card">
                    <CompaniesOnlySearchBar
                      @select-company="pushToChooseFrameworkForDataUploadPageForItem"
                      wrapper-class-additions="w-full"
                    />
                    <div v-if="isAdmin" class="mt-6">
                      <span>Can't find the company? </span>
                      <a @click="autoScrollToCreateACompanyForm" class="cursor-pointer text-primary font-semibold"
                        >Add it.</a
                      >
                    </div>
                  </div>
                </div>

                <div v-if="isAdmin" id="option2Container" ref="option2Container" class="grid">
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
    </AuthorizationWrapper>
    <TheFooter :is-light-version="true" :sections="footerContent" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { defineComponent, inject, ref } from 'vue';
import TheContent from '@/components/generics/TheContent.vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import BackButton from '@/components/general/BackButton.vue';
import Card from 'primevue/card';
import CreateCompany from '@/components/forms/CreateCompany.vue';
import CompaniesOnlySearchBar from '@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue';
import { TIME_DELAY_BETWEEN_SUBMIT_AND_NEXT_ACTION_IN_MS } from '@/utils/Constants';
import AuthorizationWrapper from '@/components/wrapper/AuthorizationWrapper.vue';
import TheFooter from '@/components/generics/TheNewFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { type CompanyIdAndName } from '@clients/backend';
import type Keycloak from 'keycloak-js';
import router from '@/router';
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';

export default defineComponent({
  name: 'ChooseCompany',
  components: {
    TheFooter,
    AuthorizationWrapper,
    AuthenticationWrapper,
    BackButton,
    TheHeader,
    TheContent,
    CompaniesOnlySearchBar,
    CreateCompany,
    Card,
  },
  data() {
    return {
      KEYCLOAK_ROLE_UPLOADER,
      isAdmin: undefined as boolean | undefined,
    };
  },
  computed: {
    footerContent() {
      const content: Content = contentData;
      const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
      return footerPage?.sections;
    },
  },
  setup() {
    return {
      option2Container: ref<Element>(),
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  mounted() {
    checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, this.getKeycloakPromise)
      .then((isAdmin) => {
        this.isAdmin = isAdmin;
      })
      .catch((error) => console.error(error));
  },
  methods: {
    /**
     * Scrolls to the create company form section of the page
     */
    autoScrollToCreateACompanyForm() {
      if (this.option2Container) {
        this.option2Container.scrollIntoView({ behavior: 'smooth' });
      }
    },
    /**
     * Executes a router push to the upload overview page of a given company
     * @param companyId the ID of company in question
     */
    handleCompanyCreated(companyId: string) {
      setTimeout(() => {
        void router.push(`/companies/${companyId}/frameworks/upload`);
      }, TIME_DELAY_BETWEEN_SUBMIT_AND_NEXT_ACTION_IN_MS);
    },

    /**
     * Executes a router push to upload overview page of the given company
     * @param selectedCompany the company selected through by the search bar
     */
    async pushToChooseFrameworkForDataUploadPageForItem(selectedCompany: CompanyIdAndName) {
      await router.push(`/companies/${selectedCompany.companyId}/frameworks/upload`);
    },
  },
});
</script>
