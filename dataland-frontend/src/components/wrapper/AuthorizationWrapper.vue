<template>
  <div v-if="!hasUserRequiredRole && waitingForCompanyOwnershipData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Checking for company ownership...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="(hasUserRequiredRole && !isFrameworkPrivate) || isUserCompanyOwner">
    <slot></slot>
  </div>

  <TheContent
    v-if="!waitingForCompanyOwnershipData && !isUserCompanyOwner && (!hasUserRequiredRole || isFrameworkPrivate)"
    class="paper-section flex"
  >
    <MiddleCenterDiv class="col-12">
      <div class="col-6 md:col-8 lg:col-12">
        <h1>You do not have permission to visit this page.</h1>
      </div>
    </MiddleCenterDiv>
  </TheContent>
</template>

<script lang="ts">
import { defineComponent, inject } from 'vue';
import type Keycloak from 'keycloak-js';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import TheContent from '@/components/generics/TheContent.vue';
import MiddleCenterDiv from '@/components/wrapper/MiddleCenterDivWrapper.vue';
import { hasUserCompanyRoleForCompany } from '@/utils/CompanyRolesUtils';
import { CompanyRole } from '@clients/communitymanager';
import { getAllPrivateFrameworkIdentifiers } from '@/frameworks/BasePrivateFrameworkRegistry';

export default defineComponent({
  name: 'AuthorizationWrapper',
  components: { TheContent, MiddleCenterDiv },
  data() {
    return {
      hasUserRequiredRole: null as boolean | null,
      isUserCompanyOwner: null as boolean | null,
      waitingForCompanyOwnershipData: true,
      isFrameworkPrivate: null as boolean | null,
    };
  },
  props: {
    requiredRole: {
      type: String,
      required: true,
    },
    allowCompanyOwnerForCompanyId: String,
    dataType: String,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  mounted: function () {
    void this.checkUserPermissions();
  },
  methods: {
    /**
     * Set if the user is allowed to upload data for the current company
     * @returns a promise that resolves to void, so the successful execution of the function can be awaited
     */
    async checkUserPermissions(): Promise<void> {
      if (this.checkIsFrameworkPrivate() && this.allowCompanyOwnerForCompanyId) {
        this.isUserCompanyOwner = await hasUserCompanyRoleForCompany(
          CompanyRole.CompanyOwner,
          this.allowCompanyOwnerForCompanyId,
          this.getKeycloakPromise
        );
        this.waitingForCompanyOwnershipData = false;
      } else {
        this.hasUserRequiredRole = await checkIfUserHasRole(this.requiredRole, this.getKeycloakPromise);
        if (!this.hasUserRequiredRole && this.allowCompanyOwnerForCompanyId) {
          this.isUserCompanyOwner = await hasUserCompanyRoleForCompany(
            CompanyRole.CompanyOwner,
            this.allowCompanyOwnerForCompanyId,
            this.getKeycloakPromise
          );
          this.waitingForCompanyOwnershipData = false;
        } else {
          this.waitingForCompanyOwnershipData = false;
        }
      }
    },
    /**
     * This method determines if a framework is private or not
     * @returns boolean if the framework is private or not
     */
    checkIsFrameworkPrivate() {
      if (this.dataType) {
        this.isFrameworkPrivate = getAllPrivateFrameworkIdentifiers().includes(this.dataType);
        return this.isFrameworkPrivate;
      }
    },
  },
});
</script>
