<template>
  <div
    :class="`summary-panel ${showViewDataButton && !useMobileView ? 'summary-panel--interactive' : ''}`"
    @click="onClickPanel"
  >
    <div>
      <div class="summary-panel__title">
        {{ title }}
      </div>
      <div class="summary-panel__subtitle" v-if="subtitle">
        {{ subtitle }}
      </div>
      <div class="summary-panel__subtitle-placeholder" v-else />
      <div class="summary-panel__separator" />
      <div>
        <span class="summary-panel__data" v-if="props.numberOfProvidedReportingPeriods != undefined">
          <span class="summary-panel__value" :data-test="`${framework}-panel-value`">
            {{ props.numberOfProvidedReportingPeriods }}
          </span>
          <template v-if="props.numberOfProvidedReportingPeriods == 1"> Reporting Period</template>
          <template v-else> Reporting Periods</template>
        </span>
      </div>
    </div>
    <div>
      <a
        v-if="showViewDataButton && !useMobileView"
        class="summary-panel__button"
        @click="router.push(`/companies/${props.companyId}/frameworks/${props.framework}`)"
        :data-test="`${framework}-view-data-button`"
      >
        VIEW DATA
      </a>
      <a
        v-if="showProvideDataButton && !useMobileView"
        class="summary-panel__button mt-2"
        @click="router.push(`/companies/${props.companyId}/frameworks/${props.framework}/upload`)"
        @pointerenter="onCursorEnterProvideButton"
        @pointerleave="onCursorLeaveProvideButton"
        :data-test="`${framework}-provide-data-button`"
      >
        PROVIDE DATA
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue';
import { DataTypeEnum } from '@clients/backend';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { FRAMEWORKS_WITH_UPLOAD_FORM, FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import router from '@/router';

const props = defineProps<{
  companyId: string;
  framework: DataTypeEnum;
  numberOfProvidedReportingPeriods?: number | null;
  isUserAllowedToUpload: boolean | undefined;
}>();

const euTaxonomyFrameworks = new Set<DataTypeEnum>([
  DataTypeEnum.EutaxonomyFinancials,
  DataTypeEnum.EutaxonomyNonFinancials,
  DataTypeEnum.NuclearAndGas,
]);
const title = computed(() => {
  if (!euTaxonomyFrameworks.has(props.framework)) {
    return humanizeStringOrNumber(props.framework as string);
  } else {
    return 'EU Taxonomy';
  }
});
const subtitle = computed(() => {
  if (!euTaxonomyFrameworks.has(props.framework)) {
    return '';
  } else if (props.framework == DataTypeEnum.EutaxonomyFinancials) {
    return 'for financial companies';
  } else if (props.framework == DataTypeEnum.NuclearAndGas) {
    return 'for nuclear and gas';
  } else {
    return 'for non-financial companies';
  }
});

const injectedUseMobileView = inject<{ value: boolean }>('useMobileView');
const useMobileView = computed<boolean | undefined>(() => injectedUseMobileView?.value);

const showProvideDataButton = computed(() => {
  return props.isUserAllowedToUpload && FRAMEWORKS_WITH_UPLOAD_FORM.includes(props.framework);
});

const showViewDataButton = computed(() => {
  return !!hasAccessibleViewPage.value;
});

const hasAccessibleViewPage = computed(() => {
  return FRAMEWORKS_WITH_VIEW_PAGE.includes(props.framework) && props.numberOfProvidedReportingPeriods;
});

let provideDataButtonHovered: boolean = false;

/**
 * If no other clickable component on the panel is hovered and the user can access the viewpage of the provided framework
 * the view page is visited
 */
function onClickPanel(): void {
  if (!provideDataButtonHovered && hasAccessibleViewPage.value) {
    void router.push(`/companies/${props.companyId}/frameworks/${props.framework}`);
  }
}

/**
 * Sets flag for tracking the cursor hovered state of the provide data button to true
 */
function onCursorEnterProvideButton(): void {
  provideDataButtonHovered = true;
}

/**
 * Sets flag for tracking the cursor hovered state of the provide data button to false
 */
function onCursorLeaveProvideButton(): void {
  provideDataButtonHovered = false;
}
</script>

<style scoped lang="scss">
@use '@/assets/scss/newVariables';
@use '@/assets/scss/variables';

.summary-panel {
  width: 100%;
  background-color: var(--surface-card);
  padding: variables.$spacing-md;
  border-radius: variables.$radius-xxs;
  text-align: left;
  box-shadow: 0 0 12px var(--gray-300);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 282px;

  &--interactive {
    cursor: pointer;
    box-shadow: 0 0 12px hsl(from var(--primary-color) h 40% l);

    &:hover {
      box-shadow: 0 0 24px 2px hsl(from var(--primary-color) h 40% l);

      .summary-panel__separator {
        border-bottom-color: var(--primary-color);
      }
    }
  }

  @media only screen and (max-width: newVariables.$small) {
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

  &__title {
    font-size: 21px;
    font-weight: 700;
    line-height: 27px;
  }

  &__subtitle {
    font-size: 16px;
    font-weight: 400;
    line-height: 21px;

    margin-top: 8px;
  }

  &__subtitle-placeholder {
    margin-top: 8px;
    height: 21px;
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

  &__button {
    display: block;
    cursor: pointer;
    width: 100%;
    color: var(--primary-color);
    border: var(--primary-color) solid 2px;
    text-decoration-line: none;
    font-size: 16px;
    font-weight: 600;
    line-height: 20px;
    padding-top: 10px;
    padding-bottom: 10px;
    text-align: center;
  }
}
</style>
