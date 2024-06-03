<template>
  <div
    :class="` summary-panel ${hasAccessibleViewPage && !useMobileView && isUserAllowedToView ? 'summary-panel--interactive' : ''}`"
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
    <a
      v-if="showProvideDataButton && !useMobileView"
      class="summary-panel__provide-button"
      @click="$router.push(`/companies/${props.companyId}/frameworks/${props.framework}/upload`)"
      @pointerenter="onCursorEnterProvideButton"
      @pointerleave="onCursorLeaveProvideButton"
      :data-test="`${framework}-provide-data-button`"
    >
      PROVIDE DATA
    </a>
    <a
      v-if="!showProvideDataButton && !useMobileView && isUserUploader"
      class="summary-panel__provide-button"
      @click="openDialog"
      @pointerenter="onCursorEnterProvideButton"
      @pointerleave="onCursorLeaveProvideButton"
      :data-test="`${framework}-claim-data-ownership-button`"
    >
      CLAIM OWNERSHIP
    </a>
  </div>
  <ClaimOwnershipDialog
    :dialog-is-open="dialogIsOpen"
    :company-name="companyName"
    :company-id="companyId"
    :claim-is-submitted="claimIsSubmitted"
    @claim-submitted="onClaimSubmitted"
    @close-dialog="onCloseDialog"
  />
</template>

<script setup lang="ts">
import { computed, inject, ref, watch } from "vue";
import { DataTypeEnum } from "@clients/backend";
import { humanizeStringOrNumber } from "@/utils/StringFormatter";
import { ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM, ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import { useRouter } from "vue-router";
import ClaimOwnershipDialog from "@/components/resources/companyCockpit/ClaimOwnershipDialog.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import type Keycloak from "keycloak-js";

const router = useRouter();

const props = defineProps<{
  companyId: string;
  framework: DataTypeEnum;
  numberOfProvidedReportingPeriods?: number | null;
  isUserAllowedToView: boolean | undefined;
  isUserAllowedToUpload: boolean | undefined;
  isUserUploader: boolean | undefined;
}>();

const euTaxonomyFrameworks = new Set<DataTypeEnum>([
  DataTypeEnum.EutaxonomyFinancials,
  DataTypeEnum.EutaxonomyNonFinancials,
]);
const title = computed(() => {
  if (!euTaxonomyFrameworks.has(props.framework)) {
    return humanizeStringOrNumber(props.framework as string);
  } else {
    return "EU Taxonomy";
  }
});
const subtitle = computed(() => {
  if (!euTaxonomyFrameworks.has(props.framework)) {
    return "";
  } else if (props.framework == DataTypeEnum.EutaxonomyFinancials) {
    return "for financial companies";
  } else {
    return "for non-financial companies";
  }
});

const injectedUseMobileView = inject<{ value: boolean }>("useMobileView");
const useMobileView = computed<boolean | undefined>(() => injectedUseMobileView?.value);

const getKeycloakPromise = inject<() => Promise<Keycloak>>("getKeycloakPromise");

const companyName = ref("");
const dialogIsOpen = ref(false);
const claimIsSubmitted = ref(false);
const showProvideDataButton = computed(() => {
  return props.isUserAllowedToUpload && ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM.includes(props.framework);
});

const authenticated = inject<{ value: boolean }>("authenticated");
const hasAccessibleViewPage = computed(() => {
  return (
    authenticated?.value &&
    ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE.includes(props.framework) &&
    props.numberOfProvidedReportingPeriods
  );
});

let provideDataButtonHovered: boolean = false;

/**
 * If no other clickable component on the panel is hovered and the user can access the viewpage of the provided framework
 * the view page is visited
 */
function onClickPanel(): void {
  if (!provideDataButtonHovered && hasAccessibleViewPage.value && props.isUserAllowedToView) {
    void router.push(`/companies/${props.companyId}/frameworks/${props.framework}`);
  }
}
/**
 * handles the emitted claim event
 * @returns the claimIsSubmitted value
 */
function onClaimSubmitted(): boolean {
  claimIsSubmitted.value = true;
  return claimIsSubmitted.value;
}
/**
 * handles the close button click event of the dialog
 * @returns the dialogIsOpen value
 */
function onCloseDialog(): boolean {
  dialogIsOpen.value = false;
  return dialogIsOpen.value;
}
/**
 * Uses the dataland API to retrieve information about the company identified by the local
 * companyId object.
 * @returns the company name
 */
async function getCompanyName(): Promise<string> {
  try {
    if (props.companyId != undefined) {
      const companyDataControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).backendClients
        .companyDataController;
      companyName.value = (await companyDataControllerApi.getCompanyInfo(props.companyId)).data.companyName;
      return companyName.value;
    }
  } catch (error) {
    console.error(error);
  }
}

watch([(): string => props.companyId], async () => dummyName(), { immediate: true });

/**
 * This method controls if the claim dialog is shown and tracks whether a claim request has been made and retrievs
 * the company name necessary for the claim request
 */
async function dummyName(): Promise<void> {
  try {
    dialogIsOpen.value = false;
    claimIsSubmitted.value = false;
    await getCompanyName();
  } catch (err) {
    console.error(err);
  }
}
/**
 * opens the dialog
 * @returns the dialogIsOpen value
 */
function openDialog(): boolean {
  if (authenticated) {
    dialogIsOpen.value = true;
    return dialogIsOpen.value;
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
.summary-panel {
  width: 100%;
  background-color: var(--surface-card);
  padding: $spacing-md;
  border-radius: $radius-xxs;
  text-align: left;
  box-shadow: 0 0 12px var(--gray-300);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 282px;

  &--interactive {
    cursor: pointer;

    &:hover {
      box-shadow: 0 0 32px 8px #1e1e1e14;

      .summary-panel__separator {
        border-bottom-color: var(--primary-color);
      }
    }
  }

  @media only screen and (max-width: $small) {
    &--interactive {
      width: 339px;
      height: 282px;
      background-color: var(--surface-card);
      padding: 24px;
      border-radius: 8px;
      text-align: left;
      box-shadow: 0 0 12px #9494943d;
      display: flex;
      flex-direction: column;
      cursor: pointer;
      justify-content: space-between;

      &:hover {
        box-shadow: 0 0 32px 8px #1e1e1e14;

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

  &__provide-button {
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
