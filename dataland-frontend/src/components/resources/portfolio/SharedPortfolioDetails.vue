<template>
  <PortfolioDetailsBase
    :portfolio-id="props.portfolioId"
    empty-text="Currently there are no companies in this portfolio or no companies match your filters."
  >
    <template #actions="{ monitoredTagAttributes, resetFilters, openDownload }">
      <Button @click="openRemoveModal()" data-test="remove-portfolio" label="REMOVE PORTFOLIO" icon="pi pi-times" />
      <Button @click="openDownload" data-test="download-portfolio" label="DOWNLOAD PORTFOLIO" icon="pi pi-download" />
      <Tag v-bind="monitoredTagAttributes" data-test="is-monitored-tag" />
      <Tag v-bind="sharedByTagAttributes" severity="info" data-test="shared-by-tag" />
      <Button
        class="reset-button-align-right"
        data-test="reset-filter"
        @click="resetFilters"
        variant="text"
        label="RESET"
      />
    </template>

    <template #dialogs="{ reload }">
      <PortfolioRemoveSharing
        :visible="isRemoveDialogVisible"
        :portfolio-id="props.portfolioId"
        @close="isRemoveDialogVisible = false"
        @sharing-removed="() => onRemoveSharing(reload)"
      />
    </template>
  </PortfolioDetailsBase>
</template>

<script setup lang="ts">
import PortfolioDetailsBase from '@/components/resources/portfolio/PortfolioDetailsBase.vue';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import { computed, ref, toRef } from 'vue';
import PortfolioRemoveSharing from '@/components/resources/portfolio/PortfolioRemoveSharing.vue';
import { type PortfolioUserAccessRight, PortfolioUserAccessRightPortfolioAccessRoleEnum } from '@clients/userservice';
import { useGetPortfolioAccessRightsQuery } from '@/api-queries/user-service/portfolio/useGetPortfolioAccessRightsQuery.ts';

const props = defineProps<{
  portfolioId: string;
}>();

const portfolioIdRef = toRef(props, 'portfolioId');
const { data: accessRights, isLoading, isError } = useGetPortfolioAccessRightsQuery(portfolioIdRef);

const emit = defineEmits(['update:portfolio-overview']);

const isRemoveDialogVisible = ref(false);
const ownerEmail = ref<string | null>(null);

const owner = accessRights.value?.find(
  (user: PortfolioUserAccessRight) => user.portfolioAccessRole === PortfolioUserAccessRightPortfolioAccessRoleEnum.Owner
);
ownerEmail.value = owner?.userEmail ?? null;

const sharedByTagAttributes = computed(() => {
  if (isLoading.value) return { value: 'Loading owner…', icon: 'pi pi-spin pi-spinner' };
  if (isError.value || !ownerEmail.value) return { value: 'Shared with you', icon: 'pi pi-share-alt' };
  return { value: `Shared by ${ownerEmail.value}`, icon: 'pi pi-share-alt' };
});

/**
 * Opens the remove dialog for the current portfolio.
 */
function openRemoveModal(): void {
  isRemoveDialogVisible.value = true;
}

/**
 * Handles successful removal: closes dialog, reloads data and notifies overview.
 */
function onRemoveSharing(reload: () => void): void {
  isRemoveDialogVisible.value = false;
  reload();
  emit('update:portfolio-overview');
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
