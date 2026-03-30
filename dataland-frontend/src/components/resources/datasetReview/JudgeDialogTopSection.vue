<template>
  <section class="flex flex-column" :data-test="dataTest" style="padding: var(--spacing-xs); height: 100%">
    <div class="flex align-items-baseline justify-content-between gap-3" style="margin-bottom: var(--spacing-sm)">
      <h3 class="flex-1" style="margin-top: 0; margin-bottom: 0; white-space: nowrap">
        {{ title }}
        <span v-if="navCount !== undefined && navCount > 0" class="ml-2">
          ({{ (navIndex ?? 0) + 1 }} / {{ navCount }})
        </span>
        <span
          v-if="showOriginalCheckmark || showQaCheckmark"
          class="pi pi-check text-green-500 ml-2 text-xl font-bold accepted-check"
          aria-label="Accepted"
        />
        <span v-else-if="qaAcceptedInfoText" class="ml-2 text-sm text-green-600 font-semibold qa-accepted-info-text">
          ({{ qaAcceptedInfoText }})
        </span>
        <DatalandProgressSpinner
          v-if="isLoading && !data"
          class="judge-modal__title-spinner ml-2"
          fontSize="font-size-sm"
        />
        <span v-else-if="isLoadingError" class="ml-2 text-sm text-red-600">
          {{ errorMessage }}
        </span>
      </h3>

      <div
        class="flex align-items-baseline gap-2 flex-shrink-0"
        :style="{ visibility: showNav ? 'visible' : 'hidden' }"
      >
        <PrimeButton
          icon="pi pi-chevron-left"
          variant="text"
          :disabled="navIndex === 0 || !showNav"
          @click="emit('prev')"
          data-test="qa-prev-button"
        />
        <div>{{ navLabel }}</div>
        <PrimeButton
          icon="pi pi-chevron-right"
          variant="text"
          :disabled="(navCount !== undefined && navIndex === navCount - 1) || !showNav"
          @click="emit('next')"
          data-test="qa-next-button"
        />
      </div>
    </div>

    <div data-test="empty-text" v-if="!isLoading && !isLoadingError && !data && emptyText">
      {{ emptyText }}
    </div>

    <div v-else class="p-datatable p-component">
      <div class="p-datatable-wrapper">
        <table class="p-datatable-table judge-modal__datatable" :aria-label="title">
          <tbody class="p-datatable-body">
            <tr>
              <th scope="row" class="headers-bg">Value</th>
              <td>
                <div class="flex align-items-center gap-1" style="min-width: 0">
                  <span class="flex-1 white-space-nowrap overflow-hidden text-overflow-ellipsis" style="min-width: 0">
                    {{ data?.value || '—' }}
                  </span>
                  <PrimeButton
                    data-test="value-overflow-icon"
                    v-if="isOverflowing(String(data?.value || ''))"
                    label="+"
                    variant="text"
                    rounded
                    size="small"
                    class="judge-modal__overflow-btn flex-shrink-0"
                    @mouseenter="(e) => emit('showPopover', e, String(data?.value || ''))"
                    @mouseleave="emit('hidePopover')"
                    aria-label="Show full value"
                  />
                </div>
              </td>
            </tr>
            <tr>
              <th scope="row" class="headers-bg">Quality</th>
              <td>
                <div class="flex align-items-center gap-1" style="min-width: 0">
                  <span class="flex-1 white-space-nowrap overflow-hidden text-overflow-ellipsis" style="min-width: 0">
                    {{ data?.quality || '—' }}
                  </span>
                  <PrimeButton
                    data-test="quality-overflow-icon"
                    v-if="isOverflowing(String(data?.quality || ''))"
                    label="+"
                    variant="text"
                    rounded
                    size="small"
                    class="judge-modal__overflow-btn flex-shrink-0"
                    @mouseenter="(e) => emit('showPopover', e, String(data?.quality || ''))"
                    @mouseleave="emit('hidePopover')"
                    aria-label="Show full quality"
                  />
                </div>
              </td>
            </tr>
            <tr>
              <th scope="row" class="headers-bg">Document</th>
              <td>
                <div class="flex align-items-center gap-1" style="min-width: 0">
                  <span class="flex-1 white-space-nowrap overflow-hidden text-overflow-ellipsis" style="min-width: 0">
                    {{ data?.dataSource?.fileName || data?.dataSource?.fileReference || '—' }}
                  </span>
                  <PrimeButton
                    data-test="document-overflow-icon"
                    v-if="isOverflowing(String(data?.dataSource?.fileName || data?.dataSource?.fileReference || ''))"
                    label="+"
                    variant="text"
                    rounded
                    size="small"
                    class="judge-modal__overflow-btn flex-shrink-0"
                    @mouseenter="
                      (e) =>
                        emit(
                          'showPopover',
                          e,
                          String(data?.dataSource?.fileName || data?.dataSource?.fileReference || '')
                        )
                    "
                    @mouseleave="emit('hidePopover')"
                    aria-label="Show full document"
                  />
                </div>
              </td>
            </tr>
            <tr>
              <th scope="row" class="headers-bg">Page(s)</th>
              <td>
                <span class="flex-1 white-space-nowrap overflow-hidden text-overflow-ellipsis" style="min-width: 0">
                  {{ data?.dataSource?.page || '—' }}
                </span>
              </td>
            </tr>
            <tr>
              <th scope="row" class="headers-bg">Comment</th>
              <td>
                <div class="flex align-items-center gap-1" style="min-width: 0">
                  <span class="flex-1 white-space-nowrap overflow-hidden text-overflow-ellipsis" style="min-width: 0">
                    {{ data?.comment || '—' }}
                  </span>
                  <PrimeButton
                    data-test="comment-overflow-icon"
                    v-if="isOverflowing(String(data?.comment || ''))"
                    label="+"
                    variant="text"
                    rounded
                    size="small"
                    class="judge-modal__overflow-btn flex-shrink-0"
                    @mouseenter="(e) => emit('showPopover', e, String(data?.comment || ''))"
                    @mouseleave="emit('hidePopover')"
                    aria-label="Show full comment"
                  />
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="judge-modal__section-actions" style="margin-top: auto; padding-top: var(--spacing-xs)">
      <PrimeButton
        :label="acceptLabel"
        @click="emit('accept')"
        :disabled="acceptDisabled"
        :data-test="acceptDataTest"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import PrimeButton from 'primevue/button';
import type { DataPointDetail } from '@/components/resources/datasetReview/JudgeDialogTypes.ts';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import { computed } from 'vue';

const OVERFLOW_THRESHOLD = 40;

const props = defineProps<{
  title: string;
  data: DataPointDetail | null;
  isLoading?: boolean;
  isLoadingError?: boolean;
  loadingErrorObject?: Error | null;
  emptyText?: string;
  acceptLabel: string;
  acceptDisabled?: boolean;
  acceptDataTest?: string;
  dataTest?: string;
  showNav?: boolean;
  navIndex?: number;
  navCount?: number;
  navLabel?: string;
  isAccepted?: boolean;
  indexOfAcceptedQaReport?: number;
}>();

const emit = defineEmits<{
  accept: [];
  prev: [];
  next: [];
  showPopover: [event: MouseEvent, text: string];
  hidePopover: [];
}>();

const isQaSection = computed(() => props.showNav);
const isOriginalSection = computed(() => !props.showNav);

const showOriginalCheckmark = computed(() => {
  return isOriginalSection.value && props.isAccepted;
});

const showQaCheckmark = computed(() => {
  return (
    isQaSection.value &&
    props.isAccepted &&
    props.indexOfAcceptedQaReport != null &&
    props.navIndex === props.indexOfAcceptedQaReport
  );
});

const qaAcceptedInfoText = computed(() => {
  if (
    !isQaSection.value ||
    props.indexOfAcceptedQaReport == null ||
    props.indexOfAcceptedQaReport < 0 ||
    props.navCount == null ||
    props.navCount <= 1
  ) {
    return '';
  }

  if (props.navIndex === props.indexOfAcceptedQaReport) {
    return '';
  }

  const acceptedNumber = props.indexOfAcceptedQaReport + 1;
  const total = props.navCount;

  return `report ${acceptedNumber}/${total} accepted`;
});

const errorMessage = computed(() => {
  if (!props.isLoadingError || !props.loadingErrorObject) {
    return 'Failed to load datapoint. Please try again later!';
  }
  const err = props.loadingErrorObject;
  if (!err) {
    return 'Failed to load datapoint. Please try again later!';
  }
  const backendMessage = err.message;
  return backendMessage
    ? `Failed to load datapoint: ${backendMessage}`
    : 'Failed to load datapoint. Please try again later!';
});

/**
 * Checks if the given text exceeds the defined overflow threshold.
 * @param text The text to check for overflow.
 * @returns True if the text length exceeds the overflow threshold, false otherwise.
 */
function isOverflowing(text: string): boolean {
  return text.length > OVERFLOW_THRESHOLD;
}
</script>

<style scoped lang="scss">
.judge-modal__datatable {
  width: 100%;

  tr {
    th,
    td {
      padding: var(--spacing-xxs) var(--spacing-xs);
      vertical-align: middle;
    }

    th {
      width: 6rem;
    }

    td {
      min-width: 0;
      max-width: 0;
    }
  }
}

.judge-modal__overflow-btn {
  width: var(--spacing-md);
  height: var(--spacing-md);
  padding: var(--spacing-none);
}
</style>
