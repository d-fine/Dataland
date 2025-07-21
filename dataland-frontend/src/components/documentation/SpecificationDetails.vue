<template>
  <div class="surface-card shadow-1 p-4 border-round mb-4">
    <h2 class="text-900 font-medium text-xl mb-3">{{ title }}</h2>
    
    <div class="specification-details">
      <slot name="fields" :specification-data="specificationData">
        <!-- Default fields for basic specifications -->
        <div v-if="specificationData.id" class="specification-field mb-3">
          <label class="field-label text-900 font-semibold block mb-1">ID:</label>
          <span class="field-value text-600">{{ specificationData.id }}</span>
        </div>

        <div v-if="specificationData.name" class="specification-field mb-3">
          <label class="field-label text-900 font-semibold block mb-1">Name:</label>
          <span class="field-value text-600">{{ specificationData.name }}</span>
        </div>

        <div v-if="specificationData.businessDefinition" class="specification-field mb-3">
          <label class="field-label text-900 font-semibold block mb-1">Business Definition:</label>
          <p class="field-value text-600 line-height-3">{{ specificationData.businessDefinition }}</p>
        </div>

        <div v-if="specificationData.validatedBy" class="specification-field mb-3">
          <label class="field-label text-900 font-semibold block mb-1">Validated By:</label>
          <span class="field-value text-600 font-family-monospace">{{ specificationData.validatedBy }}</span>
        </div>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title: string;
  specificationData: any;
}

defineProps<Props>();
</script>

<style lang="scss" scoped>
@use '@/assets/scss/variables';

.specification-field {
  .field-label {
    font-size: variables.$fs-base;
  }
  
  .field-value {
    font-size: variables.$fs-base;
    line-height: 1.5;
  }
}
</style>