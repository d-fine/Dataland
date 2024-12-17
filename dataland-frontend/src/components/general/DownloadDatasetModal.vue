<template>
  <PrimeDialog
    v-model:visible="isModalVisible"
    style="text-align: center; width: 20%"
    :show-header="true"
    header="Download dataset"
    :closable="true"
    :dismissable-mask="true"
    @hide="closeModal"
  >
    My Modal is open

    <div>
      <PrimeButton data-test="downloadDataButton" @click="downloadData()" style="width: 100%; justify-content: center">
        <span class="d-letters" style="text-align: center" data-test="downloadButton"> DOWNLOAD </span>
      </PrimeButton>
    </div>
  </PrimeDialog>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from 'vue';
import PrimeDialog from 'primevue/dialog';
import PrimeButton from 'primevue/button';

export default defineComponent({
  components: { PrimeDialog, PrimeButton },
  name: 'DownloadDatasetModal',
  props: {
    isDownloadModalOpen: {
      type: Boolean,
      required: true,
    },
  },
  setup(props, { emit }) {
    const isModalVisible = ref(props.isDownloadModalOpen);

    watch(
      () => props.isDownloadModalOpen,
      (newValue) => {
        isModalVisible.value = newValue;
      }
    );

    const downloadData = (): void => {
      closeModal();
    };

    const closeModal = (): void => {
      isModalVisible.value = false;
      emit('update:isDownloadModalOpen', false);
    };

    return {
      isModalVisible,
      downloadData,
      closeModal,
    };
  },
});
</script>
