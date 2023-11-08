<template>
  <MarginWrapper class="text-left surface-0" style="margin-right: 0">
    <BackButton />
    <FrameworkDataSearchBar
      v-if="!isReviewableByCurrentUser"
      :companyIdIfOnViewPage="companyID"
      class="mt-2"
      ref="frameworkDataSearchBar"
      @search-confirmed="handleSearchConfirm"
    />
  </MarginWrapper>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import BackButton from "@/components/general/BackButton.vue";
export default defineComponent({
  name: "SearchbarAndBackButton",
  components: { FrameworkDataSearchBar, MarginWrapper, BackButton },
  props: {
    companyID: {
      type: String,
      required: true,
    },
    isReviewableByCurrentUser: { type: Boolean },
  },
  methods: {
    /**
     * Handles the "search-confirmed" event of the search bar by visiting the search page with the query param set to
     * the search term provided by the event.
     * @param searchTerm The search term provided by the "search-confirmed" event of the search bar
     */
    async handleSearchConfirm(searchTerm: string) {
      await this.$router.push({
        name: "Search Companies for Framework Data",
        query: { input: searchTerm },
      });
    },
  },
});
</script>
