

<template>
  <div class="col-8 col-offset-2 bg-white mt-4">
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
          <template v-for="entry in createdRequests" :key="entry">
            <div class="grid-container align-items-center">
              <div class="col bold-text middle-center-div">{{ entry.companyIdentifier }}</div>
              <div class="col bold-text middle-center-div">{{ entry.companyName }}</div>
              <div class="col bold-text middle-center-div">{{ entry.reportingPeriod }}</div>
              <div class="col bold-text middle-center-div">{{ entry.framework }}</div>
            </div>
          </template>
        </AccordionTab>
      </Accordion>
    </div>
    <div class="summary-section border-bottom py-4">
      <Accordion :active-index="0">
        <AccordionTab>
          <template #header>
                          <span class="flex align-items-center gap-2 w-full">
                            <em class="material-icons info-icon new-color">info</em>
                            <span class="summary-section-heading">SKIPPED REQUESTS (data already exists)</span>
                            <Badge :value="notCreatedRequests.length" class="ml-auto mr-2" />
                          </span>
          </template>
          <div class="text-center bg-gray-300 py-1 mt-1 mb-3">
            If you believe that a dataset is incomplete or deprecated, you can still request it by submitting a single
            data request on the corresponding dataset page.
          </div>
          <template v-for="entry in notCreatedRequests" :key="entry">
            <div class="grid-container align-items-center">
              <div class="col bold-text middle-center-div">{{ entry.companyIdentifier }}</div>
              <div class="col bold-text middle-center-div">{{ entry.companyName }}</div>
              <div class="col bold-text middle-center-div">{{ entry.reportingPeriod }}</div>
              <div class="col bold-text middle-center-div">{{ entry.framework }}</div>
              <router-link :to=entry.url.toString() class="text-primary no-underline font-bold">
                <div class="text-right">
                  <span>VIEW</span>
                  <span class="ml-3">></span>
                </div>
              </router-link>
            </div>
          </template>
        </AccordionTab>
      </Accordion>
    </div>
    <div class="summary-section border-bottom py-4">
      <Accordion>
        <AccordionTab>
          <template #header>
                          <span class="flex align-items-center gap-2 w-full">
                            <em class="material-icons info-icon red-text">error</em>
                            <span class="summary-section-heading">REJECTED IDENTIFIERS</span>
                            <Badge :value="rejectedCompanyIdentifiers.length" class="ml-auto mr-2" />
                          </span>
          </template>
          <template v-for="entry in rejectedCompanyIdentifiers" :key="entry">
            <div class="grid-container align-items-center">
              <div class="col bold-text middle-center-div">{{ entry }}</div>
            </div>
          </template>
        </AccordionTab>
      </Accordion>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps } from 'vue';
import type {ExistingDataResponse} from "@/utils/RequestUtils.ts";
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Badge from 'primevue/badge';

const props = defineProps<{
  humanizedReportingPeriods: string;
  summarySectionReportingPeriodsHeading: string;
  humanizedSelectedFrameworks: string[];
  summarySectionFrameworksHeading: string;
  rejectedCompanyIdentifiers: string[];
  notCreatedRequests:Array<ExistingDataResponse>;
  createdRequests:Array<ExistingDataResponse>;
}>();
</script>

<style scoped lang="scss">
.uploadFormWrapper {
  min-height: calc(100vh - 200px);

  .status {
    .status-text {
      font-weight: 700;
      font-size: 48px;
      line-height: 48px;
      letter-spacing: 0.25px;
    }
    .info-icon {
      font-size: 48px;
    }
  }

  div.summary-section {
    &.border-bottom {
      border-bottom: 1px solid #dadada;
    }
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

      .identifier {
        font-weight: 400;
        font-size: 16px;
        line-height: 26px;
        letter-spacing: 0.44px;
      }
    }
  }
}

.summary-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px; /* Adjust the gap as needed */
}

.status-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.status-container {
  display: flex;
  align-items: center;
}

.new-color {
  color: $orange-prime;
}

.bold-text {
  font-weight: bold;
}

.no-framework {
  display: flex;
  justify-content: center;
  align-items: center;
}

.grid-container {
  display: grid;
  grid-template-columns: 2fr 4fr 1fr 2fr 1fr;
  gap: 1px;
}
</style>
