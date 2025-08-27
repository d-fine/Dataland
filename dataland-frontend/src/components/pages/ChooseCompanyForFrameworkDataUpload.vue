<template>
  <AuthenticationWrapper>
    <TheHeader />
    <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_UPLOADER">
      <TheContent>
        <div class="col-12 text-left"></div>
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
                    <div v-if="isAdmin">
                      <span>Can't find the company? </span>
                      <PrimeButton
                        label="Add it."
                        @click="autoScrollToCreateACompanyForm"
                        variant="text"
                        data-test="add-it-button"
                      />
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
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import CreateCompany from '@/components/forms/CreateCompany.vue';
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import CompaniesOnlySearchBar from '@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import AuthorizationWrapper from '@/components/wrapper/AuthorizationWrapper.vue';
import router from '@/router';
import { TIME_DELAY_BETWEEN_SUBMIT_AND_NEXT_ACTION_IN_MS } from '@/utils/Constants';
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { type CompanyIdAndName } from '@clients/backend';
import type Keycloak from 'keycloak-js';
import Card from 'primevue/card';
import { defineComponent, inject, ref } from 'vue';
import PrimeButton from 'primevue/button';

export default defineComponent({
  name: 'ChooseCompany',
  components: {
    TheFooter,
    AuthorizationWrapper,
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    CompaniesOnlySearchBar,
    CreateCompany,
    Card,
    PrimeButton,
  },
  data() {
    return {
      KEYCLOAK_ROLE_UPLOADER,
      isAdmin: undefined as boolean | undefined,
    };
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
<style scoped>
.text-primary {
  color: var(--main-color);
}

.d-card {
  background: var(--default-neutral-white);
  padding: var(--spacing-md);
  box-shadow: 0 0 3px 3px var(--shadow-color);
}
</style>
