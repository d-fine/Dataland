<template>
  <div ref="sheet">
    <MarginWrapper v-if="!useMobileView" class="text-left surface-0" style="margin-right: 0">
      <BackButton />
      <CompaniesOnlySearchBar @select-company="$emit('selectCompany', $event)" classes="w-8" />
    </MarginWrapper>
    <template v-else>
      <div :class="`mobile-header${isCollapsed ? '--attached' : ''} surface-0`" ref="mobileHeader">
        <BackButton label="" />
        <div class="mobile-header__title">
          {{ mobileTitle }}
        </div>
      </div>
      <div v-if="isCollapsed" :style="`height: ${mobileHeaderHeight ?? 0}px`" />
    </template>
    <MarginWrapper class="surface-0" style="margin-right: 0">
      <div class="grid align-items-end">
        <CompanyInformationBanner
          :companyId="props.companyId"
          @fetchedCompanyInformation="onFetchedCompanyInformation($event)"
        />
      </div>
    </MarginWrapper>
  </div>
</template>

<script setup lang="ts">
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import BackButton from "@/components/general/BackButton.vue";
import CompanyInformationBanner from "@/components/pages/CompanyInformation.vue";
import CompaniesOnlySearchBar from "@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue";
import { type CompanyIdAndName, type CompanyInformation } from "@clients/backend";
import { computed, inject, onMounted, onUnmounted, ref } from "vue";

const useMobileView = inject<boolean>("useMobileView");

const sheet = ref<HTMLDivElement>();
const mobileHeader = ref<HTMLDivElement>();

const props = defineProps<{
  companyId: string;
  isReviewableByCurrentUser?: boolean;
}>();

const emit = defineEmits<{
  (e: "selectCompany", selectedCompany: CompanyIdAndName): void;
  (e: "fetchedCompanyInformation", companyInformation: CompanyInformation): void;
}>();

function onFetchedCompanyInformation(companyInfo: CompanyInformation): void {
  companyName.value = companyInfo.companyName;
  emit("fetchedCompanyInformation", companyInfo);
}

const companyName = ref<string>();
const mobileTitle = computed<string>(() => {
  const genericTitle = "Company Overview";
  if (isCollapsed.value) {
    return companyName.value ?? genericTitle;
  } else {
    return genericTitle;
  }
});

const sheetRect = ref<DOMRect>();
const mobileHeaderHeight = ref<number>();
function onScroll() {
  sheetRect.value = sheet.value!.getBoundingClientRect(); // TODO Emanuel: this throws lots of console errors for me
  mobileHeaderHeight.value = mobileHeader.value!.getBoundingClientRect().height;
}
onMounted(() => {
  window.addEventListener("scroll", onScroll);
});
onUnmounted(() => {
  window.removeEventListener("scroll", onScroll);
});
const isCollapsed = computed<boolean>(() => {
  console.log("enter");
  if (useMobileView && sheetRect.value && mobileHeaderHeight.value) {
    console.log("if");
    if (sheetRect.value.bottom <= mobileHeaderHeight.value) {
      return true;
    }
  }
  return false;
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

  &--attached {
    display: flex;
    flex-direction: row;
    width: 100%;
    text-align: center;
    padding: 20px;
    position: fixed;
    top: 0;
  }
}
</style>
