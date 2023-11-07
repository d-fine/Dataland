<template>
  <TheHeader/>
  <TheContent class="paper-section flex">
    <MarginWrapper class="text-left surface-0" style="margin-right: 0">
      <BackButton />
      <FrameworkDataSearchBar
          :companyIdIfOnViewPage="companyId"
          class="mt-2"
          ref="frameworkDataSearchBar"
          @search-confirmed="handleSearchConfirm"
      />
    </MarginWrapper>
    <MarginWrapper class="surface-0" style="margin-right: 0">
      <div class="grid align-items-end">
        <div class="col-9">
          <CompanyInformationBanner
              :companyId="companyId"
              @fetchedCompanyInformation="handleFetchedCompanyInformation"
          />
        </div>
      </div>
    </MarginWrapper>
  </TheContent>
  <TheFooter/>
</template>

<script lang="ts">
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import {type ComponentPublicInstance, defineComponent, inject, ref} from "vue";
import PrimeButton from "primevue/button";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";
import BackButton from "@/components/general/BackButton.vue";
import ApiKeyCard from "@/components/resources/apiKey/ApiKeyCard.vue";
import CreateApiKeyCard from "@/components/resources/apiKey/CreateApiKeyCard.vue";
import MessageComponent from "@/components/messages/MessageComponent.vue";
import PrimeDialog from "primevue/dialog";
import PrimeTextarea from "primevue/textarea";
import {ApiClientProvider} from "@/services/ApiClients";
import {assertDefined} from "@/utils/TypeScriptUtils";
import type Keycloak from "keycloak-js";
import {type ApiKeyControllerApiInterface} from "@clients/apikeymanager";
import TheFooter from "@/components/generics/TheFooter.vue";
import CompanyInformationBanner from "@/components/pages/CompanyInformation.vue";
import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";

export default defineComponent({
  name: "CompanyCockpitPage",
  components: {
    MarginWrapper, FrameworkDataSearchBar, CompanyInformationBanner,
    AuthenticationWrapper,
    TheContent,
    TheHeader,
    MiddleCenterDiv,
    BackButton,
    PrimeButton,
    PrimeDialog,
    ApiKeyCard,
    CreateApiKeyCard,
    MessageComponent,
    PrimeTextarea,
    TheFooter,
  },
  props: {
    companyId: {
      type: String,
      required: true,
    }
  },
  mounted() {
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

    handleFetchedCompanyInformation() {

    }
  },
});
</script>

<style lang="scss" scoped>
.copy-button {
  cursor: pointer;
}

.p-inputText:enabled:focus {
  box-shadow: none;
}

.apiKeyInfo .p-message-success {
  background-color: var(--green-600);
  border-color: var(--green-600);
  color: white;
}
</style>
