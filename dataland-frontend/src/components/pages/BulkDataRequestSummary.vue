<template>
  <div class="col-8 mx-auto bg-white mt-4">
    <h1 class="middle-center-div">Data Request Summary</h1>
    <div class="summary-section border-bottom py-4">
      <div class="summary-wrapper">
        <div class="grid col-6">
          <div class="col" data-test="reportingPeriodsHeading">
            <h4 class="middle-center-div summary-section-heading m-0">
              {{ summarySectionReportingPeriodsHeading }}
            </h4>
            <p class="middle-center-div summary-section-data m-0 mt-3">{{ humanizedReportingPeriods }}</p>
          </div>
          <div class="col" data-test="frameworksHeading">
            <h6 class="middle-center-div summary-section-heading m-0">
              {{ summarySectionFrameworksHeading }}
            </h6>
            <p class="middle-center-div summary-section-data m-0 mt-3">
              {{ humanizedSelectedFrameworks?.join(', ') }}
            </p>
          </div>
        </div>
      </div>
    </div>
    <div class="summary-section border-bottom py-4">
      <Accordion :multiple="true" :active-index="activeIndex">
        <AccordionTab v-for="(section, index) in sections" :key="index">
          <template #header>
            <span :data-test="section.dataTestHeader" class="flex align-items-center gap-2 w-full">
              <em class="material-icons info-icon" :class="section.iconColor">{{ section.icon }}</em>
              <span class="summary-section-heading">{{ section.title }}</span>
              <Badge :value="section.items.length" class="ml-auto mr-2" />
            </span>
          </template>
          <div v-if="section.textBox" class="text-center bg-gray-300 p-1 mt-1 mb-3">
            {{ section.textBox }}
          </div>
          <div
            v-if="
              section.items === acceptedDataRequests ||
              section.items === alreadyExistingNonFinalRequests ||
              section.items === alreadyExistingDatasets
            "
          >
            <div
              v-for="(entry, index) in section.items"
              :key="index"
              :data-test="section.dataTestContent"
              class="grid-container align-items-center"
            >
              <div class="col bold-text middle-center-div">{{ entry.userProvidedIdentifier }}</div>
              <div class="col bold-text middle-center-div">{{ entry.companyName }}</div>
              <div class="col bold-text middle-center-div">{{ entry.reportingPeriod }}</div>
              <div class="col bold-text middle-center-div">{{ humanizeStringOrNumber(entry.framework) }}</div>
              <a
                v-if="entry.resourceUrl"
                :href="entry.resourceUrl.toString()"
                target="_blank"
                class="text-primary no-underline font-bold"
              >
                <div class="text-right">
                  <span>{{ section.linkText }}</span>
                  <span class="ml-3">></span>
                </div>
              </a>
            </div>
          </div>
          <div v-else class="grid">
            <span class="col bold-text text-left px-6" :data-test="section.dataTestContent">{{
              section.items.join(', ')
            }}</span>
          </div>
        </AccordionTab>
      </Accordion>
    </div>
  </div>
</template>

<script setup lang="ts">
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Badge from 'primevue/badge';
import type { BulkDataRequestResponse } from '@clients/communitymanager';
import { getFrameworkTitle, humanizeStringOrNumber } from '@/utils/StringFormatter';

const props = defineProps<{
  bulkDataRequestResponse: BulkDataRequestResponse;
  humanizedReportingPeriods: string;
  summarySectionReportingPeriodsHeading: string;
  humanizedSelectedFrameworks: string[];
  summarySectionFrameworksHeading: string;
}>();

const { acceptedDataRequests, alreadyExistingNonFinalRequests, alreadyExistingDatasets, rejectedCompanyIdentifiers } =
  props.bulkDataRequestResponse;

const activeIndex: number[] = alreadyExistingDatasets?.length ? [1] : [];

const sections = [
  {
    title: 'CREATED REQUESTS',
    icon: 'check_circle',
    iconColor: 'green-text',
    items: acceptedDataRequests,
    linkText: 'VIEW REQUEST',
    dataTestHeader: 'acceptedDataRequestsHeader',
    dataTestContent: 'acceptedDataRequestsContent',
  },
  {
    title: 'SKIPPED REQUESTS - DATA ALREADY EXISTS',
    icon: 'info',
    iconColor: 'info-color',
    items: alreadyExistingDatasets,
    linkText: 'VIEW DATA',
    dataTestHeader: 'alreadyExistingDatasetsHeader',
    dataTestContent: 'alreadyExistingDatasetsContent',
    textBox:
      'If you believe that a dataset is incomplete or deprecated, you can still request it by submitting a single\n' +
      'data request on the corresponding dataset page.',
  },
  {
    title: 'SKIPPED REQUESTS - REQUEST ALREADY EXISTS',
    icon: 'info',
    iconColor: 'info-color',
    items: alreadyExistingNonFinalRequests,
    linkText: 'VIEW REQUEST',
    dataTestHeader: 'alreadyExistingNonFinalRequestsHeader',
    dataTestContent: 'alreadyExistingNonFinalRequestsContent',
  },
  {
    title: 'REJECTED IDENTIFIERS',
    icon: 'error',
    iconColor: 'red-text',
    items: rejectedCompanyIdentifiers,
    dataTestHeader: 'rejectedCompanyIdentifiersHeader',
    dataTestContent: 'rejectedCompanyIdentifiersContent',
    textBox: 'No company on Dataland is associated to any of the following identifier',
  },
];
</script>

<style scoped lang="scss">
@use '@/assets/scss/variables';

.border-bottom {
  border-bottom: 1px solid #dadada;
}

div.summary-section {
  .summary-section-heading {
    font-weight: 500;
    font-size: 16px;
    line-height: 20px;

    .info-icon {
      margin-bottom: -2px;
      vertical-align: bottom;
    }
  }

  .summary-section-data {
    font-weight: 700;
  }
}

.summary-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px; /* Adjust the gap as needed */
}

.info-color {
  color: variables.$orange-prime;
}

.bold-text {
  font-weight: bold;
}

.grid-container {
  display: grid;
  grid-template-columns: 2fr 4fr 2fr 4fr 2fr;
  gap: 1px;
}
</style>
