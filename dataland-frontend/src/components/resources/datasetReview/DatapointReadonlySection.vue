<template>
  <section class="judge-modal__section" :data-test="dataTest">
    <div class="judge-modal__section-header-with-nav">
      <h3 class="judge-modal__section-title">
        {{ title }}
        <span v-if="navCount !== undefined && navCount > 0" style="margin-left: var(--spacing-xs)">
          ({{ (navIndex ?? 0) + 1 }} / {{ navCount }})
        </span>
      </h3>

      <div class="judge-modal__qa-nav" :style="{ visibility: showNav ? 'visible' : 'hidden' }">
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
            :disabled="navCount !== undefined && navIndex === navCount - 1 || !showNav"
            @click="emit('next')"
            data-test="qa-next-button"
        />
      </div>
    </div>

    <div v-if="isLoading">
      Loading...
    </div>
    <div v-else-if="loadError">
      <Message severity="error">
        Failed to load datapoint.
      </Message>
    </div>
    <div v-else-if="!data && emptyText">
      {{ emptyText }}
    </div>
    <div v-else-if="data">
      <table class="judge-modal__datatable" :aria-label="title">
        <tbody>
        <tr>
          <th>Value</th>
          <td>
            <div class="judge-modal__cell-overflow">
              <span class="judge-modal__cell-text">{{ data.value ?? '—' }}</span>
              <PrimeButton
                  v-if="isOverflowing(String(data.value ?? ''))"
                  label="+"
                  variant="text"
                  rounded
                  size="small"
                  class="judge-modal__overflow-btn"
                  @mouseenter="(e) => emit('showPopover', e, String(data?.value ?? ''))"
                  @mouseleave="emit('hidePopover')"
                  aria-label="Show full value"
              />
            </div>
          </td>
        </tr>
        <tr>
          <th>Quality</th>
          <td>
            <div class="judge-modal__cell-overflow">
              <span class="judge-modal__cell-text">{{ data.quality ?? '—' }}</span>
              <PrimeButton
                  v-if="isOverflowing(String(data.quality ?? ''))"
                  label="+"
                  variant="text"
                  rounded
                  size="small"
                  class="judge-modal__overflow-btn"
                  @mouseenter="(e) => emit('showPopover', e, String(data?.quality ?? ''))"
                  @mouseleave="emit('hidePopover')"
                  aria-label="Show full quality"
              />
            </div>
          </td>
        </tr>
        <tr>
          <th>Document</th>
          <td>
            <div class="judge-modal__cell-overflow">
              <span class="judge-modal__cell-text">
                {{ data.dataSource?.fileName ?? data.dataSource?.fileReference ?? '—' }}
              </span>
              <PrimeButton
                  v-if="isOverflowing(String(data.dataSource?.fileName ?? data.dataSource?.fileReference ?? ''))"
                  label="+"
                  variant="text"
                  rounded
                  size="small"
                  class="judge-modal__overflow-btn"
                  @mouseenter="(e) => emit('showPopover', e, String(data?.dataSource?.fileName ?? data?.dataSource?.fileReference ?? ''))"
                  @mouseleave="emit('hidePopover')"
                  aria-label="Show full document"
              />
            </div>
          </td>
        </tr>
        <tr>
          <th>Page(s)</th>
          <td>
            <span class="judge-modal__cell-text">{{ data.dataSource?.page ?? '—' }}</span>
          </td>
        </tr>
        <tr>
          <th>Comment</th>
          <td>
            <div class="judge-modal__cell-overflow">
              <span class="judge-modal__cell-text">{{ data.comment ?? '—' }}</span>
              <PrimeButton
                  v-if="isOverflowing(String(data.comment ?? ''))"
                  label="+"
                  variant="text"
                  rounded
                  size="small"
                  class="judge-modal__overflow-btn"
                  @mouseenter="(e) => emit('showPopover', e, String(data?.comment ?? ''))"
                  @mouseleave="emit('hidePopover')"
                  aria-label="Show full comment"
              />
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="judge-modal__section-actions">
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
import Message from 'primevue/message';
import type { DataPointDetail } from '@/components/resources/datasetReview/JudgeDialogTypes.ts';

const OVERFLOW_THRESHOLD = 40;

const props = defineProps<{
  title: string;
  data: DataPointDetail | null;
  isLoading?: boolean;
  loadError?: unknown;
  emptyText?: string;
  acceptLabel: string;
  acceptDisabled?: boolean;
  acceptDataTest?: string;
  dataTest?: string;
  // Navigation (used for corrected datapoint)
  showNav?: boolean;
  navIndex?: number;
  navCount?: number;
  navLabel?: string;
}>();

const emit = defineEmits<{
  accept: [];
  prev: [];
  next: [];
  showPopover: [event: MouseEvent, text: string];
  hidePopover: [];
}>();

function isOverflowing(text: string): boolean {
  return text.length > OVERFLOW_THRESHOLD;
}
</script>

<style scoped lang="scss">
.judge-modal__section {
  padding: var(--spacing-xs);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.judge-modal__section-title {
  margin-top: 0;
  margin-bottom: var(--spacing-xs);
  white-space: nowrap;
}

.judge-modal__section-header-with-nav {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-sm);

  > .judge-modal__section-title {
    margin-bottom: 0;
    flex: 1;
  }
}

.judge-modal__qa-nav {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-xs);
  flex-shrink: 0;
}

.judge-modal__datatable {
  width: 100%;
  border-spacing: 0;
  border-collapse: collapse;
  table-layout: fixed;

  tr {

    th {
      width: 8rem;
      padding-right: var(--spacing-md);
      vertical-align: middle;
      text-align: left;
      flex-shrink: 0;
    }

    td {
      padding: 0.4rem 0;
      vertical-align: middle;
      max-width: 0;
    }
  }
}

.judge-modal__cell-overflow {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  min-width: 0;
}

.judge-modal__cell-text {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
}

.judge-modal__overflow-btn {
  flex-shrink: 0;
  width: 1.1rem;
  height: 1.1rem;
  padding: 0;
}

.judge-modal__section-actions {
  margin-top: auto;
  padding-top: 0.5rem;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}
</style>


