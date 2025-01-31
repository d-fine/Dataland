<template>
  <div class="col-8 mx-auto bg-white mt-4">
    <h1 class="middle-center-div">Data Request Summary</h1>
    <div class="summary-section border-bottom py-4">
      <div class="summary-wrapper">
        <div class="grid col-6">
          <div class="col">
            <h4 class="middle-center-div summary-section-heading m-0">
              {{ summarySectionReportingPeriodsHeading }}
            </h4>
            <p class="middle-center-div summary-section-data m-0 mt-3">{{ humanizedReportingPeriods }}</p>
          </div>
          <div class="col">
            <h6 class="middle-center-div summary-section-heading m-0">
              {{ summarySectionFrameworksHeading }}
            </h6>
            <p class="middle-center-div summary-section-data m-0 mt-3">
              {{ humanizedSelectedFrameworks.join(', ') }}
            </p>
          </div>
        </div>
      </div>
    </div>
    <div class="summary-section border-bottom py-4">
      <Accordion>
        <AccordionTab>
          <template #header>
            <span class="flex align-items-center gap-2 w-full">
              <em class="material-icons info-icon green-text">check_circle</em>
              <span class="summary-section-heading">CREATED REQUESTS</span>
              <Badge :value="createdRequests.length" class="ml-auto mr-2" />
            </span>
          </template>
          <div data-test="createdRequests">
            <div class="grid-container align-items-center" v-for="(entry, index) in createdRequests" :key="index">
              <div class="col bold-text middle-center-div">{{ entry.userProvidedCompanyId }}</div>
              <div class="col bold-text middle-center-div">{{ entry.companyName }}</div>
              <div class="col bold-text middle-center-div">{{ entry.reportingPeriod }}</div>
              <div class="col bold-text middle-center-div">{{ getFrameworkTitle(entry.framework) }}</div>
              <a :href="entry.requestUrl.toString()" class="text-primary no-underline font-bold">
                <div class="text-right">
                  <span>VIEW REQUEST</span>
                  <span class="ml-3">></span>
                </div>
              </a>
            </div>
          </div>
        </AccordionTab>
        <AccordionTab>
          <template #header>
            <span class="flex align-items-center gap-2 w-full">
              <em class="material-icons info-icon info-color">info</em>
              <span class="summary-section-heading">SKIPPED REQUESTS - DATA ALREADY EXISTS</span>
              <Badge :value="existingDatasets.length" class="ml-auto mr-2" />
            </span>
          </template>
          <div class="text-center bg-gray-300 p-1 mt-1 mb-3">
            If you believe that a dataset is incomplete or deprecated, you can still request it by submitting a single
            data request on the corresponding dataset page.
          </div>
          <div data-test="existingDatasets">
            <div class="grid-container align-items-center" v-for="(entry, index) in existingDatasets" :key="index">
              <div class="col bold-text middle-center-div">{{ entry.userProvidedCompanyId }}</div>
              <div class="col bold-text middle-center-div">{{ entry.companyName }}</div>
              <div class="col bold-text middle-center-div">{{ entry.reportingPeriod }}</div>
              <div class="col bold-text middle-center-div">{{ getFrameworkTitle(entry.framework) }}</div>
              <a :href="entry.datasetUrl.toString()" class="text-primary no-underline font-bold">
                <div class="text-right">
                  <span>VIEW DATA</span>
                  <span class="ml-3">></span>
                </div>
              </a>
            </div>
          </div>
        </AccordionTab>
        <AccordionTab>
          <template #header>
            <span class="flex align-items-center gap-2 w-full">
              <em class="material-icons info-icon info-color">info</em>
              <span class="summary-section-heading">SKIPPED REQUESTS - REQUESTS ALREADY EXIST</span>
              <Badge :value="existingRequests.length" class="ml-auto mr-2" />
            </span>
          </template>
          <div data-test="existingRequests">
            <div class="grid-container align-items-center" v-for="(entry, index) in existingRequests" :key="index">
              <div class="col bold-text middle-center-div">{{ entry.userProvidedCompanyId }}</div>
              <div class="col bold-text middle-center-div">{{ entry.companyName }}</div>
              <div class="col bold-text middle-center-div">{{ entry.reportingPeriod }}</div>
              <div class="col bold-text middle-center-div">{{ getFrameworkTitle(entry.framework) }}</div>
              <a :href="entry.requestUrl.toString()" class="text-primary no-underline font-bold">
                <div class="text-right">
                  <span>VIEW REQUEST</span>
                  <span class="ml-3">></span>
                </div>
              </a>
            </div>
          </div>
        </AccordionTab>
        <AccordionTab>
          <template #header>
            <span class="flex align-items-center gap-2 w-full">
              <em class="material-icons info-icon red-text">error</em>
              <span class="summary-section-heading">REJECTED IDENTIFIERS</span>
              <Badge :value="rejectedCompanyIdentifiers.length" class="ml-auto mr-2" />
            </span>
          </template>
          <div class="text-center bg-gray-300 p-1 mt-1 mb-3">
            No company or companies are known on Dataland for the following company identifier(s)
          </div>
          <div data-test="rejectedCompanyIdentifiers">
            <div
              class="grid-container align-items-center"
              v-for="(entry, index) in rejectedCompanyIdentifiers"
              :key="index"
            >
              <div class="col bold-text middle-center-div">{{ entry }}</div>
            </div>
          </div>
        </AccordionTab>
      </Accordion>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Badge from 'primevue/badge';
import type { type BulkDataRequestResponse } from '@clients/communitymanager';
import { getFrameworkTitle } from '@/utils/StringFormatter';

const props = defineProps<{
  bulkDataRequestResponse: BulkDataRequestResponse;
  humanizedReportingPeriods: string;
  summarySectionReportingPeriodsHeading: string;
  humanizedSelectedFrameworks: string[];
  summarySectionFrameworksHeading: string;
}>();
const {
  acceptedDataRequests: createdRequests,
  alreadyExistingNonFinalRequests: existingRequests,
  alreadyExistingDatasets: existingDatasets,
  rejectedCompanyIdentifiers: rejectedCompanyIdentifiers,
} = props.bulkDataRequestResponse;
</script>

<style scoped lang="scss">
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
  color: $orange-prime;
}

.bold-text {
  font-weight: bold;
}

.grid-container {
  display: grid;
  grid-template-columns: 2fr 4fr 1fr 2fr 1fr;
  gap: 1px;
}
</style>
