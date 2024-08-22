<template>
  <div ref="sheet" :class="`sheet ${isCollapsed ? 'visuals-hidden' : ''}`" data-test="sheet">
    <template v-if="!useMobileView">
      <div class="headline">
        <BackButton />
      </div>

      <CompaniesOnlySearchBar
        v-if="showSearchBar"
        @select-company="$router.push(`/companies/${$event.companyId}`)"
        class="w-8 mt-2"
      />
    </template>
    <template v-else>
      <div class="mobile-header">
        <BackButton label="" data-test-marker="back-button-mobile" />
        <div class="mobile-header__title" data-test="mobile-header-title">
          {{ mobileTitle }}
        </div>
      </div>
    </template>
    <CompanyInformationBanner
      :companyId="companyId"
      :show-single-data-request-button="showSingleDataRequestButton"
      @fetchedCompanyInformation="onFetchedCompanyInformation($event)"
      :framework="framework"
      :map-of-reporting-period-to-active-dataset="mapOfReportingPeriodToActiveDataset"
      class="w-12"
    />
  </div>
  <div ref="attachedSheet" :class="`sheet--attached ${isCollapsed ? '' : 'visuals-hidden'}`" data-test="sheet-attached">
    <div class="mobile-header">
      <BackButton label="" data-test-marker="back-button-mobile-attached-sheet" />
      <div class="mobile-header__title">
        {{ mobileTitle }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import BackButton from '@/components/general/BackButton.vue';
import CompanyInformationBanner from '@/components/pages/CompanyInformation.vue';
import CompaniesOnlySearchBar from '@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue';
import { type CompanyInformation, type DataMetaInformation, type DataTypeEnum } from '@clients/backend';
import { computed, inject, onMounted, onUnmounted, type PropType, ref } from 'vue';

const injectedMobileView = inject<{ value: boolean }>('useMobileView');
const useMobileView = computed<boolean | undefined>(() => injectedMobileView?.value);

const sheet = ref<HTMLDivElement>();
const attachedSheet = ref<HTMLDivElement>();

const { companyId, showSearchBar, showSingleDataRequestButton, framework, mapOfReportingPeriodToActiveDataset } =
  defineProps({
    companyId: {
      type: String,
      required: true,
    },
    showSearchBar: {
      type: Boolean,
      default: true,
    },
    showSingleDataRequestButton: {
      type: Boolean,
      default: false,
    },
    framework: {
      type: String as PropType<DataTypeEnum>,
      required: false,
    },
    mapOfReportingPeriodToActiveDataset: {
      type: Map as PropType<Map<string, DataMetaInformation>>,
      required: false,
    },
  });

const emit = defineEmits(['fetchedCompanyInformation']);

/**
 * On fetched company information defines the companyName and emits an event of type "fetchedCompanyInformation"
 * @param companyInfo company information from which the company name can be retrieved
 */
function onFetchedCompanyInformation(companyInfo: CompanyInformation): void {
  companyName.value = companyInfo.companyName;
  emit('fetchedCompanyInformation', companyInfo);
}

const companyName = ref<string>();
const mobileTitle = computed<string>(() => {
  const genericTitle = 'Company Overview';
  if (isCollapsed.value) {
    return companyName.value ?? genericTitle;
  } else {
    return genericTitle;
  }
});

const sheetRect = ref<DOMRect>();
const attachedSheetHeight = ref<number>();

/**
 * Sets the value of sheetRect and mobilHeaderHeight
 */
function onScroll(): void {
  sheetRect.value = sheet.value!.getBoundingClientRect();
  attachedSheetHeight.value = attachedSheet.value!.getBoundingClientRect().height;
}

onMounted(() => {
  window.addEventListener('scroll', onScroll);
});
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll);
});
const isCollapsed = computed<boolean>(() => {
  if (useMobileView.value && sheetRect.value != undefined && attachedSheetHeight.value != undefined) {
    if (sheetRect.value.bottom <= attachedSheetHeight.value) {
      return true;
    }
  }
  return false;
});
</script>

<style scoped lang="scss">
.sheet {
  width: 100%;
  background-color: var(--surface-0);
  box-shadow: 0 4px 4px 0 #00000005;
  padding: 0.5rem 1rem 1rem;
  display: flex;
  flex-direction: column;
  align-items: start;

  &--attached {
    width: 100%;
    background-color: var(--surface-0);
    box-shadow: 0 4px 4px 0 #00000005;
    padding: 0.5rem 1rem 1rem;
    position: fixed;
    top: 0;
  }
}

.visuals-hidden {
  visibility: hidden;
}

.headline {
  width: 100%;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.mobile-header {
  display: flex;
  flex-direction: row;
  width: 100%;
  text-align: center;

  &__title {
    width: 100%;
    text-align: center;
    font-weight: bold;
  }
}
</style>
