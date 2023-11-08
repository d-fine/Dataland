<template>
  <TheHeader/>
  <TheContent class="paper-section flex">
    <MarginWrapper class="text-left surface-0" style="margin-right: 0">
      <BackButton />
      <CompaniesOnlySearchBar
        @select-company="pushToCompanyCockpit"
        classes="w-8"
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
    <div class="card-holder">
      <div class="card-grid">
        <!-- TODO use cards here -->
        <div
            v-for="framework of Object.values(DataTypeEnum)"
            style="width: 339px; height: 282px; background: grey"
        >
          <h3>{{ framework }}</h3>
        </div>
      </div>
    </div>
  </TheContent>
  <TheFooter/>
</template>

<script lang="ts">
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import { defineComponent } from "vue";
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
import TheFooter from "@/components/generics/TheFooter.vue";
import CompanyInformationBanner from "@/components/pages/CompanyInformation.vue";
import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import CompaniesOnlySearchBar from "@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue";
import {CompanyIdAndName, DataTypeEnum} from "@clients/backend";

export default defineComponent({
  name: "CompanyCockpitPage",
  computed: {
    DataTypeEnum() {
      return DataTypeEnum
    }
  },
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
    CompaniesOnlySearchBar,
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

    /**
     * Executes a router push to upload overview page of the given company
     * @param selectedCompany the company selected through the search bar
     */
    async pushToCompanyCockpit(selectedCompany: CompanyIdAndName) {
      await this.$router.push(`/companies/${selectedCompany.companyId}`);
    },

    handleFetchedCompanyInformation() {

    }
  },
});
</script>

<style lang="scss" scoped>
.card-holder {
  width: 100%;
  display: flex;
  justify-content: center;
  padding-top: 40px;
  padding-bottom: 40px;
}

.card-grid {
  width: 50%;
  display: flex;
  flex-wrap: wrap;
  gap: 40px;
  justify-content: center;
}
</style>
