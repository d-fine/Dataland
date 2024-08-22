<template>
  <div
    v-if="!hasUserRequiredKeycloakRole && waitingForCompanyRoleAssignments"
    class="d-center-div text-center px-7 py-4"
  >
    <p class="font-medium text-xl">Checking for user roles...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="(hasUserRequiredKeycloakRole && !isFrameworkPrivate) || isUserCompanyOwnerOrUploader">
    <slot></slot>
  </div>

  <TheContent
    v-if="
      !waitingForCompanyRoleAssignments &&
      !isUserCompanyOwnerOrUploader &&
      (!hasUserRequiredKeycloakRole || isFrameworkPrivate)
    "
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
      hasUserRequiredKeycloakRole: null as boolean | null,
      isUserCompanyOwnerOrUploader: null as boolean | null,
      waitingForCompanyRoleAssignments: true,
      isFrameworkPrivate: null as boolean | null,
    };
  },
  props: {
    requiredRole: {
      type: String,
      required: true,
    },
    companyId: String,
    dataType: String,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  mounted: function () {
    this.setIfFrameworkIsPrivate();
    void this.setUserPermissions();
  },
  methods: {
    /**
     * Set if the user is allowed to upload data for the current company
     * @returns a promise that resolves to void, so the successful execution of the function can be awaited
     */
    async setUserPermissions(): Promise<void> {
      let isCompanyOwner = false;
      let isDataUploader = false;
      if (this.companyId) {
        [isCompanyOwner, isDataUploader] = await Promise.all([
          hasUserCompanyRoleForCompany(CompanyRole.CompanyOwner, this.companyId, this.getKeycloakPromise),
          hasUserCompanyRoleForCompany(CompanyRole.DataUploader, this.companyId, this.getKeycloakPromise),
        ]);
      }
      this.isUserCompanyOwnerOrUploader = isCompanyOwner || isDataUploader;
      this.hasUserRequiredKeycloakRole = await checkIfUserHasRole(this.requiredRole, this.getKeycloakPromise);
      this.waitingForCompanyRoleAssignments = false;
    },

    /**
     * This method sets if the data type in the props is private or not
     */
    setIfFrameworkIsPrivate() {
      if (this.dataType) {
        this.isFrameworkPrivate = getAllPrivateFrameworkIdentifiers().includes(this.dataType);
      }
    },
  },
});
</script>
