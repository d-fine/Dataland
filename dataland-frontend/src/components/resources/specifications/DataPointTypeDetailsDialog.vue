<script setup lang="ts">
import { computed } from 'vue';
import type { DataPointTypeSpecification } from '@clients/specificationservice';
import { humanizeDataPointBaseType } from '@/utils/StringFormatter';
import Dialog from 'primevue/dialog';
import Accordion from 'primevue/accordion';
import AccordionPanel from 'primevue/accordionpanel';
import ProgressSpinner from 'primevue/progressspinner';
import Message from 'primevue/message';
import PrimeButton from 'primevue/button';

/**
 * Props for DataPointTypeDetailsDialog.
 */
const props = defineProps<{
  visible: boolean;
  dataPointTypeId: string | null;
  dataPointDetails: DataPointTypeSpecification | null;
  isLoading: boolean;
  error: string | null;
}>();

/**
 * Emits for DataPointTypeDetailsDialog.
 */
const emit = defineEmits<{
  'update:visible': [value: boolean];
  'close': [];
  'retry': [dataPointTypeId: string];
}>();

/**
 * Computed property to sync visibility with parent.
 */
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
});

/**
 * Handle dialog close.
 */
function handleClose(): void {
  emit('close');
}

/**
 * Handle retry button click.
 */
function handleRetry(): void {
  if (props.dataPointTypeId) {
    emit('retry', props.dataPointTypeId);
  }
}

/**
 * Format constraints array for display.
 */
function formatConstraints(constraints: string[] | undefined): string {
  if (!constraints || constraints.length === 0) {
    return 'No constraints defined';
  }
  return constraints.join('\n');
}

/**
 * Format frameworks list for display.
 */
function getFrameworksList(usedBy: Array<{ id: string; ref: string }> | undefined): string {
  if (!usedBy || usedBy.length === 0) {
    return 'Not used by any framework';
  }
  return usedBy.map(f => f.id).join(', ');
}
</script>

<template>
  <Dialog
    v-model:visible="dialogVisible"
    modal
    :header="dataPointDetails?.name || 'Data Point Details'"
    :style="{ width: '50rem', maxWidth: '90vw' }"
    :dismissable-mask="true"
    aria-modal="true"
    aria-label="Data point details"
    @hide="handleClose"
  >
    <!-- Loading State -->
    <div v-if="isLoading" class="dialog-loading">
      <ProgressSpinner />
      <p>Loading data point details...</p>
    </div>

    <!-- Error State -->
    <Message v-else-if="error" severity="error" class="dialog-error">
      <div class="error-content">
        <p>{{ error }}</p>
        <PrimeButton
          label="Retry"
          icon="pi pi-refresh"
          text
          @click="handleRetry"
        />
      </div>
    </Message>

    <!-- Content -->
    <div v-else-if="dataPointDetails" class="dialog-content">
      <!-- Overview Section (always visible) -->
      <div class="overview-section">
        <dl class="details-list">
          <!-- Name -->
          <div class="detail-item">
            <dt class="detail-label">Name</dt>
            <dd class="detail-value detail-value-title">{{ dataPointDetails.name }}</dd>
          </div>

          <!-- Business Definition -->
          <div class="detail-item">
            <dt class="detail-label">Business Definition</dt>
            <dd class="detail-value detail-value-prominent">{{ dataPointDetails.businessDefinition }}</dd>
          </div>

          <!-- Data Type Display (human-readable + technical) -->
          <div class="detail-item">
            <dt class="detail-label">Data Type</dt>
            <dd class="detail-value">
              <div class="data-type-display">
                <span class="type-primary">{{ humanizeDataPointBaseType(dataPointDetails.dataPointBaseType.id) }}</span>
                <span class="type-secondary">({{ dataPointDetails.dataPointBaseType.id }})</span>
              </div>
            </dd>
          </div>
        </dl>
      </div>

      <!-- Technical Details Section (collapsible) -->
      <Accordion :value="undefined" class="technical-accordion">
        <AccordionPanel value="technical">
          <template #header>
            <div class="accordion-header">
              <i class="pi pi-code"></i>
              <span>Technical Details</span>
            </div>
          </template>
          
          <dl class="details-list">
            <!-- Data Point Type ID -->
            <div class="detail-item">
              <dt class="detail-label">Data Point Type ID</dt>
              <dd class="detail-value detail-value-code">{{ dataPointDetails.dataPointType.id }}</dd>
            </div>

            <!-- Base Type with Link -->
            <div class="detail-item">
              <dt class="detail-label">Base Type ID</dt>
              <dd class="detail-value">
                <span class="detail-value-code">{{ dataPointDetails.dataPointBaseType.id }}</span>
                <a
                  :href="dataPointDetails.dataPointBaseType.ref"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="detail-link"
                >
                  <i class="pi pi-external-link"></i>
                  View Base Type
                </a>
              </dd>
            </div>

            <!-- Constraints -->
            <div class="detail-item">
              <dt class="detail-label">Constraints</dt>
              <dd class="detail-value">
                <pre class="detail-code">{{ formatConstraints(dataPointDetails.constraints) }}</pre>
              </dd>
            </div>

            <!-- Frameworks -->
            <div class="detail-item">
              <dt class="detail-label">Used By Frameworks</dt>
              <dd class="detail-value detail-value-code">{{ getFrameworksList(dataPointDetails.usedBy) }}</dd>
            </div>

            <!-- API Reference -->
            <div class="detail-item">
              <dt class="detail-label">API Reference</dt>
              <dd class="detail-value">
                <a
                  :href="dataPointDetails.dataPointType.ref"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="detail-link"
                >
                  {{ dataPointDetails.dataPointType.ref }}
                  <i class="pi pi-external-link"></i>
                </a>
              </dd>
            </div>
          </dl>
        </AccordionPanel>
      </Accordion>
    </div>

    <!-- No Data State -->
    <div v-else class="dialog-empty">
      <p>No data point details available</p>
    </div>

    <template #footer>
      <PrimeButton
        label="Close"
        icon="pi pi-times"
        :data-test="'close-dialog'"
        @click="handleClose"
      />
    </template>
  </Dialog>
</template>

<style scoped lang="scss">
.dialog-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 3rem 2rem;
  color: var(--p-text-secondary-color);
}

.dialog-error {
  margin: 0;

  .error-content {
    display: flex;
    flex-direction: column;
    gap: 1rem;

    p {
      margin: 0;
    }
  }
}

.dialog-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  color: var(--p-text-secondary-color);

  p {
    margin: 0;
  }
}

.dialog-content {
  .overview-section {
    margin-bottom: 1.5rem;
  }

  .data-type-display {
    display: flex;
    align-items: baseline;
    gap: 0.5rem;
    flex-wrap: wrap;

    .type-primary {
      font-size: 1.125rem;
      font-weight: 600;
      color: var(--p-text-color);
    }

    .type-secondary {
      font-family: monospace;
      font-size: 0.9375rem;
      color: var(--p-text-secondary-color);
    }
  }

  .technical-accordion {
    margin-top: 1rem;

    .accordion-header {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-weight: 600;

      i {
        color: var(--p-text-secondary-color);
      }
    }
  }

  .details-list {
    display: flex;
    flex-direction: column;
    gap: 1.75rem;
    margin: 0;

    .detail-item {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;

      .detail-label {
        font-weight: 600;
        color: var(--p-text-color);
        font-size: 0.875rem;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        margin: 0;
      }

      .detail-value {
        margin: 0;
        color: var(--p-text-color);
        line-height: 1.6;

        &.detail-value-title {
          font-size: 1.25rem;
          font-weight: 600;
          color: var(--p-text-color);
        }

        &.detail-value-prominent {
          font-size: 1rem;
          line-height: 1.7;
          padding: 1rem;
          background: var(--p-surface-100);
          border-radius: var(--p-border-radius);
          border-left: 3px solid var(--p-primary-color);
        }

        &.detail-value-code {
          font-family: monospace;
          font-size: 0.875rem;
          color: var(--p-text-secondary-color);
        }
      }

      .detail-code {
        margin: 0;
        font-family: monospace;
        font-size: 0.875rem;
        color: var(--p-text-color);
        background: var(--p-surface-50);
        padding: 1rem;
        border-radius: var(--p-border-radius);
        white-space: pre-wrap;
        word-break: break-word;
        overflow-x: auto;
      }

      .detail-link {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        color: var(--p-primary-color);
        text-decoration: none;
        font-size: 0.875rem;
        margin-top: 0.5rem;

        &:hover {
          text-decoration: underline;
        }

        i {
          font-size: 0.75rem;
        }
      }
    }
  }
}
</style>
