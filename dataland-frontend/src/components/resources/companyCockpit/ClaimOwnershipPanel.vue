<template>
  <Card
    :pt="{
      body: {
        style: {
          padding: 'var(--spacing-sm) var(--spacing-lg)',
          gap: 'var(--spacing-none)',
        },
      },
    }"
  >
    <template #title>
      <span data-test="claimOwnershipPanelHeading">Responsible for {{ companyName }}?</span>
      <PrimeButton
        data-test="claimOwnershipPanelLink"
        label="Claim company ownership."
        variant="link"
        @click="openDialog"
        :pt="{
          label: {
            style: {
              fontSize: 'var(--font-size-lg)',
              fontWeight: 'var(--font-weight-bold)',
            },
          },
        }"
      />
    </template>
  </Card>
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
import { computed, inject, ref, watch } from 'vue';
import { useGetCompanyInfoQuery } from '@/api-queries/backend/company-data/useGetCompanyInfoQuery';
import ClaimOwnershipDialog from '@/components/resources/companyCockpit/ClaimOwnershipDialog.vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import Card from 'primevue/card';
import PrimeButton from 'primevue/button';

const props = defineProps<{ companyId: string }>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const dialogIsOpen = ref(false);
const claimIsSubmitted = ref(false);

const { data: companyInformation } = useGetCompanyInfoQuery({ companyId: () => props.companyId });
const companyName = computed(() => companyInformation.value?.companyName ?? '');

watch(
  () => props.companyId,
  () => {
    dialogIsOpen.value = false;
    claimIsSubmitted.value = false;
  }
);

/**
 * handles the emitted claim event
 */
function onClaimSubmitted(): void {
  claimIsSubmitted.value = true;
}

/**
 * handles the close button click event of the dialog
 */
function onCloseDialog(): void {
  dialogIsOpen.value = false;
}

/**
 * opens the dialog
 */
async function openDialog(): Promise<void> {
  const isLoggedIn = await checkLoggedIn();
  if (isLoggedIn) {
    dialogIsOpen.value = true;
  } else {
    const keycloakInstance = await assertDefined(getKeycloakPromise)();
    await keycloakInstance.register();
  }
}

/**
 * checks if user is logged in
 * @returns boolean
 */
async function checkLoggedIn(): Promise<boolean | undefined> {
  if (getKeycloakPromise) {
    return (await getKeycloakPromise()).authenticated;
  }
  return false;
}
</script>
