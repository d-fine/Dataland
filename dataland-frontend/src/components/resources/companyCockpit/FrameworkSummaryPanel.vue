<template>
  <Card
    @click="onClickPanel"
    class="summary-panel"
    :class="{ 'summary-panel--highlight': (numberOfProvidedReportingPeriods ?? 0) > 0 }"
    :pt="{
      body: {
        style: {
          display: 'flex',
          flexDirection: 'column',
          flexGrow: 1,
        },
      },
      content: {
        style: {
          justifyContent: 'flex-start',
          alignItems: 'flex-start',
        },
      },
      footer: {
        style: {
          marginTop: 'auto',
          paddingTop: '3rem',
        },
      },
    }"
  >
    <template #title>{{ title }}</template>
    <template #subtitle>
      <template v-if="subtitle">
        {{ subtitle }}
      </template>
      <div class="summary-panel__subtitle-placeholder" v-if="!subtitle" />
    </template>
    <template #content>
      <div class="summary-panel__separator" />
      <div>
        <span class="summary-panel__data" v-if="numberOfProvidedReportingPeriods != null">
          <span class="summary-panel__value" :data-test="`${framework}-panel-value`">
            {{ numberOfProvidedReportingPeriods }}
          </span>
          <template v-if="numberOfProvidedReportingPeriods === 1"> Reporting Period</template>
          <template v-else> Reporting Periods</template>
        </span>
      </div>
    </template>
    <template #footer>
      <div class="stacked-buttons">
        <Button
          v-if="showViewDataButton && !useMobileView"
          @click="router.push(`/companies/${companyId}/frameworks/${framework}`)"
          :data-test="`${framework}-view-data-button`"
          label="VIEW DATA"
          variant="outlined"
        />
        <Button
          v-if="showProvideDataButton && !useMobileView"
          @click="router.push(`/companies/${companyId}/frameworks/${framework}/upload`)"
          @pointerenter="onCursorEnterProvideButton"
          @pointerleave="onCursorLeaveProvideButton"
          :data-test="`${framework}-provide-data-button`"
          label="PROVIDE DATA"
          variant="outlined"
        />
      </div>
    </template>
  </Card>
</template>

<script setup lang="ts">
import { computed, inject, ref } from 'vue';
import router from '@/router';
import { DataTypeEnum } from '@clients/backend';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { FRAMEWORKS_WITH_UPLOAD_FORM, FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import Card from 'primevue/card';
import Button from 'primevue/button';

const props = defineProps<{
  companyId: string;
  framework: DataTypeEnum;
  numberOfProvidedReportingPeriods?: number | null;
  isUserAllowedToUpload?: boolean;
}>();

const provideDataButtonHovered = ref(false);
const injectedUseMobileView = inject<{ value: boolean }>('useMobileView');
const useMobileView = computed(() => injectedUseMobileView?.value);

const euTaxonomyFrameworks = new Set<DataTypeEnum>([
  DataTypeEnum.EutaxonomyFinancials,
  DataTypeEnum.EutaxonomyNonFinancials,
  DataTypeEnum.NuclearAndGas,
]);

const title = computed(() => {
  return euTaxonomyFrameworks.has(props.framework) ? 'EU Taxonomy' : humanizeStringOrNumber(props.framework as string);
});

const subtitle = computed(() => {
  if (!euTaxonomyFrameworks.has(props.framework)) return '';
  switch (props.framework) {
    case DataTypeEnum.EutaxonomyFinancials:
      return 'for financial companies';
    case DataTypeEnum.NuclearAndGas:
      return 'for nuclear and gas';
    default:
      return 'for non-financial companies';
  }
});

const hasAccessibleViewPage = computed(
  () => FRAMEWORKS_WITH_VIEW_PAGE.includes(props.framework) && !!props.numberOfProvidedReportingPeriods
);

const showProvideDataButton = computed(
  () => props.isUserAllowedToUpload && FRAMEWORKS_WITH_UPLOAD_FORM.includes(props.framework)
);

const showViewDataButton = computed(() => hasAccessibleViewPage.value);

/**
 * Handles the panel click event. Navigates to the framework page if
 * the "Provide Data" button is not hovered and the framework has an accessible view page.
 */
function onClickPanel(): void {
  if (!provideDataButtonHovered.value && hasAccessibleViewPage.value) {
    void router.push(`/companies/${props.companyId}/frameworks/${props.framework}`);
  }
}

/**
 * Handles the pointer enter event on the "Provide Data" button.
 */
function onCursorEnterProvideButton(): void {
  provideDataButtonHovered.value = true;
}

/**
 * Handles the pointer leave event on the "Provide Data" button.
 */
function onCursorLeaveProvideButton(): void {
  provideDataButtonHovered.value = false;
}
</script>

<style scoped lang="scss">
.stacked-buttons {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.summary-panel {
  display: flex;
  flex-direction: column;
  height: 100%;

  .p-card-body {
    display: flex;
    flex-direction: column;
    flex-grow: 1;
    text-align: left;
  }

  .p-card-content {
    justify-content: flex-start;
    align-items: flex-start;
  }

  .p-card-footer {
    margin-top: auto;
    padding-top: 3rem;
  }

  &--highlight {
    box-shadow: 0 1px 10px var(--p-orange-600);
    transition: box-shadow 0.1s ease;
    cursor: pointer;

    /* on hover, make it even bigger */
    &:hover {
      box-shadow: 0 1px 18px var(--p-orange-600);

      .summary-panel__separator {
        border-bottom-color: var(--p-orange-500);
      }
    }
  }

  @media only screen and (max-width: 768px) {
    &--interactive {
      width: 339px;
      height: 282px;
      background-color: var(--surface-card);
      padding: 24px;
      border-radius: 8px;
      text-align: left;
      box-shadow: 0 0 12px hsl(from var(--primary-color) h 40% l);
      display: flex;
      flex-direction: column;
      cursor: pointer;
      justify-content: space-between;

      &:hover {
        box-shadow: 0 0 24px 2px hsl(from var(--primary-color) h 40% l);

        .summary-panel__separator {
          border-bottom-color: var(--primary-color);
        }
      }
    }
  }

  &__subtitle-placeholder {
    margin-top: 8px;
    height: 16px;
  }

  &__separator {
    width: 100%;
    border-bottom: #e0dfde solid 1px;
    margin-top: 8px;
    margin-bottom: 24px;
  }

  &__data {
    font-size: 16px;
    font-weight: 300;
    line-height: 21px;
  }

  &__value {
    font-weight: 600;
  }
}
</style>
