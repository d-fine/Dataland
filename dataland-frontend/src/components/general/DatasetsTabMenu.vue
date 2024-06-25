<template>
  <TabView
    v-if="initialTabIndex != undefined"
    :activeIndex="initialTabIndex"
    @tab-change="handleTabChange"
    class="col-12"
  >
    <TabPanel
      v-for="tab in tabs"
      :key="tab"
      :disabled="!(tabs.indexOf(tab) == initialTabIndex || (tab.isVisible ?? true))"
      :header="tab.label"
    >
      <slot v-if="tabs.indexOf(tab) == initialTabIndex"></slot>
    </TabPanel>
  </TabView>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent, inject } from 'vue';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import { checkIfUserHasRole, KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakUtils';
import type Keycloak from 'keycloak-js';

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
        isVisible: true,
      },
      {
        label: 'MY DATA REQUESTS',
        route: '/requests',
        isVisible: true,
      },
    ] as Tab[],
  }),
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  created() {
    checkIfUserHasRole(KEYCLOAK_ROLE_REVIEWER, this.getKeycloakPromise)
      .then((hasUserReviewerRights) => {
        this.tabs[2].isVisible = hasUserReviewerRights;
      })
      .catch((error) => console.log(error));
  },
  methods: {
    /**
     * Routes to companies page when AVAILABLE DATASET tab is clicked
     * @param event the event containing the index of the newly selected tab
     * @param event.index the index of the tab element
     */
    async handleTabChange(event: { index: number }): void {
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
