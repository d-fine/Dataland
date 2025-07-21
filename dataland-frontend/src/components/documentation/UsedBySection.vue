<template>
  <div class="surface-card shadow-1 p-4 border-round">
    <h3 class="text-900 font-medium text-lg mb-3">{{ title }}</h3>
    
    <div v-if="items && items.length > 0" class="used-by-container">
      <div class="used-by-content">
        <div
          v-for="item in items"
          :key="item.id"
          class="used-by-item mb-3 p-3 border-1 border-round surface-100"
        >
          <div class="flex align-items-center justify-content-between">
            <div class="flex-1">
              <SpecificationBadge :text="item.id" :uppercase="uppercaseBadges" />
            </div>
            <div class="flex-shrink-0">
              <button 
                @click="handleLinkClick(item.ref)" 
                class="text-blue-600 hover:text-blue-800 text-sm font-family-monospace bg-transparent border-none p-0 cursor-pointer"
              >
                {{ linkText }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div v-else class="text-center p-4">
      <p class="text-600">{{ emptyMessage }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import SpecificationBadge from './SpecificationBadge.vue';
import { handleSpecificationLink } from '@/utils/linkHandler';

interface UsedByItem {
  id: string;
  ref: string;
}

interface Props {
  title: string;
  items: UsedByItem[];
  linkText?: string;
  emptyMessage?: string;
  uppercaseBadges?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  linkText: 'View Specification →',
  emptyMessage: 'No items found',
  uppercaseBadges: false,
});

const handleLinkClick = (ref: string): void => {
  handleSpecificationLink(ref);
};
</script>