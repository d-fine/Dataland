<template>
  <MarginWrapper v-if="!useMobileView" class="text-left surface-0" style="margin-right: 0">
    <BackButton />
    <CompaniesOnlySearchBar @select-company="$emit('selectCompany', $event)" classes="w-8" />
  </MarginWrapper>
  <div v-else class="mobile-header surface-0">
    <BackButton label="" />
    <div class="mobile-header__title">
      {{ mobileTitle }}
    </div>
  </div>
  <MarginWrapper class="surface-0" style="margin-right: 0">
    <div class="grid align-items-end">
      <div class="col-9">
        <CompanyInformationBanner
            :companyId="props.companyId"
            @fetchedCompanyInformation="onFetchedCompanyInformation($event)"
        />
      </div>
    </div>
  </MarginWrapper>
</template>

<script setup lang="ts">
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import BackButton from "@/components/general/BackButton.vue";
import CompanyInformationBanner from "@/components/pages/CompanyInformation.vue";
import CompaniesOnlySearchBar from "@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue";
import {CompanyIdAndName, CompanyInformation} from "@clients/backend";
import {computed, inject, ref} from "vue";

const useMobileView = inject<boolean>("useMobileView")

const props = defineProps<{
  companyId: string;
  isReviewableByCurrentUser?: boolean
}>();

const emit = defineEmits<{
  (e: "selectCompany", selectedCompany: CompanyIdAndName): void
  (e: "fetchedCompanyInformation", companyInformation: CompanyInformation): void
}>();

function onFetchedCompanyInformation(companyInfo: CompanyInformation): void {
  companyName.value = companyInfo.companyName;
  emit("fetchedCompanyInformation", companyInfo)
}

const companyName = ref<string>()
const mobileTitle = computed<string>(() => {
  const isCollapsed = true;// TODO shift computation out
  const genericTitle = "Company Overview";
  if(isCollapsed) {
    return companyName.value ?? genericTitle;
  } else {
    return genericTitle;
  }
});
</script>

<style scoped lang="scss">

.mobile-header {
  display: flex;
  flex-direction: row;
  width: 100%;
  text-align: center;
  padding: 20px;

  &__title {
    width: 100%;
    text-align: center;
    font-weight: bold;
  }
}

</style>