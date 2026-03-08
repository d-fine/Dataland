import { ref } from 'vue';
import type { Ref } from 'vue';

const isOpen = ref(false);

/**
 * Opens the contact inquiry modal.
 */
function openModal(): void {
  isOpen.value = true;
}

/**
 * Closes the contact inquiry modal.
 */
function closeModal(): void {
  isOpen.value = false;
}

/**
 * Provides reactive state and controls for the contact inquiry modal.
 */
export function useContactModal(): { isOpen: Ref<boolean>; openModal: () => void; closeModal: () => void } {
  return { isOpen, openModal, closeModal };
}