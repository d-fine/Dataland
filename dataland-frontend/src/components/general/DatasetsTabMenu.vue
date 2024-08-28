<template>
  <TabView
    v-if="initialTabIndex != undefined"
    :activeIndex="initialTabIndex"
    @tab-change="handleTabChange"
    class="col-12"
  >
    <TabPanel
      v-for="tab in tabs"
      :key="tab.label"
      :disabled="!(tabs.indexOf(tab) == initialTabIndex || (tab.isVisible ?? true))"
      :header="tab.label"
    >
      <slot v-if="tabs.indexOf(tab) == initialTabIndex"></slot>
    </TabPanel>
  </TabView>
</template>

<script lang="ts">
import { defineComponent, inject } from 'vue';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import { checkIfUserHasRole, KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakUtils';
import type Keycloak from 'keycloak-js';
import { CompanyRole, type CompanyRoleAssignment } from '@clients/communitymanager';

export default defineComponent({
  name: 'DatasetsTabMenu',
  components: {
    TabView,
    TabPanel,
  },
  props: {
    initialTabIndex: {
      type: Number,
      required: true,
    },
  },
  data: () => ({
    tabs: [
      {
        label: 'COMPANIES',
        route: '/companies',
        isVisible: true,
      },
      {
        label: 'MY DATASETS',
        route: '/datasets',
        isVisible: true,
      },
      {
        label: 'QA',
        route: '/qualityassurance',
        isVisible: false,
      },
      {
        label: 'MY DATA REQUESTS',
        route: '/requests',
        isVisible: true,
      },
      {
        label: 'DATA REQUESTS FOR MY COMPANIES',
        route: '/companyrequests',
        isVisible: false,
      },
    ] as Tab[],
  }),
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
      companyRoleAssignments: inject<Array<CompanyRoleAssignment>>('companyRoleAssignments'),
    };
  },
  created() {
    this.setVisibilityForTabWithQualityAssurance();
    this.setVisibilityForTabWithAccessRequestsForMyCompanies();
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
      checkIfUserHasRole(KEYCLOAK_ROLE_REVIEWER, this.getKeycloakPromise).then((hasUserReviewerRights) => {
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
     * Routes to companies page when AVAILABLE DATASET tab is clicked
     * @param event the event containing the index of the newly selected tab
     * @param event.index the index of the tab element
     */
    async handleTabChange(event: { index: number }): Promise<void> {
      if (this.initialTabIndex != event.index) {
        await this.$router.push(this.tabs[event.index].route);
      }
    },
  },
});

interface Tab {
  label: string;
  route: string;
  isVisible: boolean;
}
</script>

<style>
.p-tabview .p-tabview-nav li.p-disabled .p-tabview-nav-link {
  display: none;
}
</style>
