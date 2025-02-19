<template>
  <PrimeDialog
    id="documentMetaDataDialog"
    :dismissable-mask="true"
    :modal="true"
    header="Header"
    class="col-6"
    v-model:visible="internalDialogVisible"
    @hide="closeDialog"
  >
    <template #header>
      <h2 class="m-0">Document XYZ</h2>
    </template>
  </PrimeDialog>
</template>

<script setup lang="ts">
import PrimeDialog from 'primevue/dialog';
import { ref, watch } from 'vue';

const props = defineProps<{
  dialogVisible: boolean;
}>();

const internalDialogVisible = ref(props.dialogVisible);
const emit = defineEmits(['update:dialogVisible']);

watch(internalDialogVisible, (newValue) => {
  emit('update:dialogVisible', newValue);
});

watch(
  () => props.dialogVisible,
  (newValue) => {
    if (internalDialogVisible.value !== newValue) {
      internalDialogVisible.value = newValue;
    }
  }
);

const closeDialog = () => {
  internalDialogVisible.value = false;
};
</script>

<style scoped lang="scss"></style>
