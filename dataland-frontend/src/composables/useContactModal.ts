import { ref } from 'vue';

const isOpen = ref(false);

function openModal(): void {
  isOpen.value = true;
}

function closeModal(): void {
  isOpen.value = false;
}

export function useContactModal() {
  return { isOpen, openModal, closeModal };
}