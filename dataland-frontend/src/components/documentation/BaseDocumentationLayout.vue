<template>
  <TheHeader />
  
  <div class="static-content">
    <main role="main" class="w-12 flex justify-content-center">
      <div class="lg:w-8 md:w-10 sm:w-10 w-12 text-left">
        <div class="mb-4">
          <BackButton />
          <h1 class="text-900 font-medium text-xl mb-4">{{ pageTitle }}</h1>
        </div>

        <div v-if="waitingForData" class="text-center p-6">
          <p class="font-medium text-xl text-600 mb-3">Loading specification data...</p>
          <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
        </div>

        <div v-else-if="error" class="text-center p-6">
          <Message severity="error" :closable="false">
            <p class="text-600">Error loading specification data: {{ error }}</p>
          </Message>
        </div>

        <div v-else-if="specificationData" class="specification-content">
          <slot name="content" :specification-data="specificationData" />
        </div>
      </div>
    </main>
    
    <TheFooter :is-light-version="false" />
  </div>
</template>

<script setup lang="ts">
import TheHeader from '@/components/generics/TheNewHeader.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import BackButton from '@/components/general/BackButton.vue';
import Message from 'primevue/message';

interface Props {
  pageTitle: string;
  waitingForData: boolean;
  error: string | null;
  specificationData: any;
}

defineProps<Props>();
</script>

<style lang="scss" scoped>
@use '@/assets/scss/newVariables';

.static-content {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 100vh;
}

main {
  margin-top: 122px;
  margin-bottom: 52px;
  @media only screen and (max-width: newVariables.$small) {
    margin-top: 82px;
  }
}

.specification-content {
  max-width: 100%;
}
</style>