<template>
  <PortfolioDetailsBase :portfolio-id="portfolioId">
    <template
        #actions="{
        enrichedPortfolio,
        isMonitored,
        monitoredTagAttributes,
        resetFilters,
        openDownload,
        reload,
      }"
    >
      <Button
          @click="openEditModal(enrichedPortfolio, reload)"
          data-test="edit-portfolio"
          label="EDIT PORTFOLIO"
          icon="pi pi-pencil"
      />
      <Button
          @click="openDownload"
          data-test="download-portfolio"
          label="DOWNLOAD PORTFOLIO"
          icon="pi pi-download"
      />
      <div :title="!isUserDatalandMemberOrAdmin ? 'Only Dataland members can activate monitoring' : ''">
        <Button
            @click="openMonitoringModal(enrichedPortfolio, isMonitored, reload)"
            data-test="monitor-portfolio"
            :disabled="!isUserDatalandMemberOrAdmin"
            icon="pi pi-bell"
            label="ACTIVE MONITORING"
        />
      </div>

      <Tag v-bind="monitoredTagAttributes" data-test="is-monitored-tag" />
      <Button
          class="reset-button-align-right"
          data-test="reset-filter"
          @click="resetFilters"
          variant="text"
          label="RESET"
      />
    </template>

    <template #dialogs="{ isMonitored }">
      <SuccessDialog
          :visible="isSuccessDialogVisible"
          :message="successDialogMessage"
          @close="isSuccessDialogVisible = false"
      />
    </template>
  </PortfolioDetailsBase>
</template>

<script setup lang="ts">
import PortfolioDetailsBase from '@/components/resources/portfolio/PortfolioDetailsBase.vue';
import PortfolioDialog from '@/components/resources/portfolio/PortfolioDialog.vue';
import PortfolioMonitoring from '@/components/resources/portfolio/PortfolioMonitoring.vue';
import SuccessDialog from '@/components/general/SuccessDialog.vue';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import { computed, inject, onMounted, ref } from 'vue';
import { useDialog } from 'primevue/usedialog';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils.ts';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles.ts';

const props = defineProps<{
  portfolioId: string;
}>();

const emit = defineEmits(['update:portfolio-overview']);

const dialog = useDialog();
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const isUserDatalandMemberOrAdmin = ref(false);
const isSuccessDialogVisible = ref(false);
const isMonitoredAfterChange = ref<boolean>(false);

/**
 * Success message depending on "is monitored" after change
 */
const successDialogMessage = computed(() =>
    isMonitoredAfterChange.value
        ? 'Portfolio monitoring updated successfully.\nData requests will be created automatically overnight.'
        : 'Portfolio monitoring updated successfully.'
);

/**
 * Checks whether the logged-in User is Dataland member or Admin
 */
onMounted(async () => {
  const keycloak = await assertDefined(getKeycloakPromise)();
  const keycloakUserId = keycloak.idTokenParsed?.sub;

  if (keycloakUserId === undefined) {
    isUserDatalandMemberOrAdmin.value = false;
    return;
  }

  const response = await apiClientProvider.apiClients.inheritedRolesController.getInheritedRoles(keycloakUserId);
  const inheritedRolesMap = response.data;

  isUserDatalandMemberOrAdmin.value =
      Object.values(inheritedRolesMap).flat().includes('DatalandMember') ||
      (await checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise));
});

/**
 * Opens the PortfolioDialog with the current portfolio's data for editing.
 * Once the dialog is closed, it reloads the portfolio data and emits an update event
 * to refresh the portfolio overview.
 */
function openEditModal(enrichedPortfolio: any, reload: () => void): void {
  dialog.open(PortfolioDialog, {
    props: {
      header: 'Edit Portfolio',
      modal: true,
    },
    data: {
      portfolio: enrichedPortfolio,
      isMonitoring: enrichedPortfolio?.isMonitored ?? false,
    },
    onClose(options) {
      if (!options?.data?.isDeleted) {
        reload();
      }
      emit('update:portfolio-overview');
    },
  });
}

/**
 * Opens the PortfolioMonitoring with the current portfolio's data.
 * Once the dialog is closed, it reloads the portfolio data and shows the portfolio overview again.
 */
function openMonitoringModal(enrichedPortfolio: any, isMonitored: boolean, reload: () => void): void {
  const fullName = 'Monitoring of ' + enrichedPortfolio?.portfolioName;
  dialog.open(PortfolioMonitoring, {
    props: {
      modal: true,
      header: fullName,
      pt: {
        title: {
          style: {
            maxWidth: '18rem',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          },
        },
      },
    },
    data: {
      portfolio: enrichedPortfolio,
    },
    onClose(options) {
      if (options?.data?.monitoringSaved) {
        // reflect the new monitoring state in the success dialog message
        // isMonitoredAfterChange.value = options.data.isMonitored ?? isMonitored;
        isSuccessDialogVisible.value = true;
        reload();
        emit('update:portfolio-overview');
      }
    },
  });
}
</script>

<style scoped>
label {
  margin-left: 0.5em;
}

.reset-button-align-right {
  margin-left: auto;
}
</style>
