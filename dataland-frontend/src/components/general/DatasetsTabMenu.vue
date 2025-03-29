<template>
  <Tabs value="/companies" v-if="initialTabIndex != undefined" @tab-change="handleTabChange" class="col-12">
    <TabList>
      <Tab
        v-for="tab in tabs"
        :key="tab.label"
        :value="tab.value"
        :tabindex="initialTabIndex"
        :disabled="!(tabs.indexOf(tab) == initialTabIndex || (tab.isVisible ?? true))"
        :active="initialTabIndex == tabs.indexOf(tab)"
        :data-p-active="initialTabIndex == tabs.indexOf(tab)"
        v-bind:class="initialTabIndex == tabs.indexOf(tab) ? 'p-tab-active' : ''"
      >
        <router-link v-if="tab.route" v-slot="{ href, navigate }" :to="tab.route">
          <a :href="href" @click="navigate">
            <span>{{ tab.label }}</span>
          </a>
        </router-link>
      </Tab>
    </TabList>
  </Tabs>
</template>

<script lang="ts">
import { defineComponent, inject } from 'vue';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import Tabs from 'primevue/tabs';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import type Keycloak from 'keycloak-js';
import { CompanyRole, type CompanyRoleAssignment } from '@clients/communitymanager';
import router from '@/router';
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakRoles';

export default defineComponent({
  name: 'DatasetsTabMenu',
  components: {
    Tab,
    Tabs,
    TabList,
  },
  props: {
    initialTabIndex: {
      type: Number,
      required: true,
    },
  },
  data(): { tabs: Tab[] } {
    return {
      tabs: [
        { label: 'COMPANIES', value: 'companies', route: '/companies', isVisible: true },
        { label: 'MY DATASETS', value: 'dataset', route: '/datasets', isVisible: true },
        { label: 'QA', value: 'qualityassurance', route: '/qualityassurance', isVisible: false },
        { label: 'MY DATA REQUESTS', value: 'datarequest', route: '/requests', isVisible: true },
        {
          label: 'DATA REQUESTS FOR MY COMPANIES',
          value: 'companyrequest',
          route: '/companyrequests',
          isVisible: false,
        },
        { label: 'ALL DATA REQUESTS', value: 'allrequests', route: '/requestoverview', isVisible: false },
      ],
    };
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
      companyRoleAssignments: inject<Array<CompanyRoleAssignment>>('companyRoleAssignments'),
    };
  },
  created() {
    this.setVisibilityForTabWithQualityAssurance();
    this.setVisibilityForTabWithAccessRequestsForMyCompanies();
    this.setVisibilityForAdminTab();
  },
  watch: {
    companyRoleAssignments() {
      this.setVisibilityForTabWithAccessRequestsForMyCompanies();
    },
  },
  methods: {
    /**
     * Sets the visibility of the tab for Quality Assurance.
     * If the user does have the Keycloak-role "Reviewer", it is shown. Else it stays invisible.
     */
    setVisibilityForTabWithQualityAssurance() {
      void checkIfUserHasRole(KEYCLOAK_ROLE_REVIEWER, this.getKeycloakPromise).then((hasUserReviewerRights) => {
        this.tabs[2].isVisible = hasUserReviewerRights;
      });
    },

    /**
     * Sets the visibility of the tab for data access requests to companies of the current user.
     * If the user does have any company ownership, the tab is shown. Else it stays invisible.
     */
    setVisibilityForTabWithAccessRequestsForMyCompanies() {
      const companyOwnershipAssignments = this.companyRoleAssignments?.filter(
        (roleAssignment) => roleAssignment.companyRole == CompanyRole.CompanyOwner
      );
      if (companyOwnershipAssignments) {
        this.tabs[4].isVisible = companyOwnershipAssignments.length > 0;
      }
    },
    /**
     * Sets the visibility of the all data requests tab.
     * Only Admins can see the tab.
     */
    setVisibilityForAdminTab() {
      void checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, this.getKeycloakPromise).then((hasUserAdminRights) => {
        this.tabs[5].isVisible = hasUserAdminRights;
      });
    },
    /**
     * Routes to companies page when AVAILABLE DATASET tab is clicked
     * @param event the event containing the index of the newly selected tab
     * @param event.index the index of the tab element
     */
    async handleTabChange(event: { index: number }): Promise<void> {
      if (this.initialTabIndex != event.index) {
        await router.push(this.tabs[event.index].route);
      }
    },
  },
});

export interface Tab {
  label: string;
  value: string;
  route: string;
  isVisible: boolean;
}
</script>
