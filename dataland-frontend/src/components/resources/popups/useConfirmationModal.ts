import { ref, type Ref } from 'vue';

/**
 * Confirmation modal state.
 */
export interface ConfirmationModalState {
  visible: boolean;
  header: string;
  message: string;
  errorMessage?: string;
  isLoading?: boolean;
  onConfirm?: () => void;
}

/**
 * Returned shape from `useConfirmationModal`.
 */
export interface UseConfirmationModalReturn {
  confirmationModal: Ref<ConfirmationModalState>;
  openConfirmationModal: (header: string, message: string, onConfirm?: () => void) => void;
  closeConfirmationModal: () => void;
  setConfirmationError: (errorMessage: string) => void;
}

/**
 * Hook providing a reactive confirmation modal and simple controls.
 *
 * Returns the modal state ref and helpers to open/close the modal and set an error.
 */
export function useConfirmationModal(): UseConfirmationModalReturn {
  const confirmationModal = ref<ConfirmationModalState>({
    visible: false,
    header: '',
    message: '',
    errorMessage: '',
    isLoading: false,
    onConfirm: () => {},
  });

  /**
   * Open the confirmation modal with header, message and optional confirm callback.
   */
  const openConfirmationModal = (header: string, message: string, onConfirm?: () => void): void => {
    confirmationModal.value = {
      ...confirmationModal.value,
      visible: true,
      header,
      message,
      errorMessage: '',
      onConfirm,
    };
  };

  /**
   * Close the confirmation modal.
   */
  const closeConfirmationModal = (): void => {
    confirmationModal.value.visible = false;
  };

  /**
   * Set an error message on the confirmation modal.
   */
  const setConfirmationError = (errorMessage: string): void => {
    confirmationModal.value.errorMessage = errorMessage;
  };

  return {
    confirmationModal,
    openConfirmationModal,
    closeConfirmationModal,
    setConfirmationError,
  };
}
