<template>
  <TheContent class="flex">
    <div class="col-12 text-left pb-0">
      <h1>Specification Explorer</h1>
      <p class="text-lg">
        Browse Dataland's frameworks, their data points and underlying base types. This explorer is generated directly
        from the
        <a href="/specifications/swagger-ui/index.html" target="_blank" rel="noopener noreferrer"
          >specification service</a
        >, so it always reflects the current state &mdash; unlike static documentation.
      </p>
    </div>
    <div class="col-12 md:col-6 lg:col-4 text-left">
      <InputText
        v-model="searchTerm"
        placeholder="Search frameworks"
        class="w-full"
        data-test="specificationSearchInput"
      />
    </div>
    <div v-if="isPending" class="col-12 text-left">
      <p class="font-medium text-xl">Loading frameworks...</p>
      <DatalandProgressSpinner />
    </div>
    <FailMessage v-else-if="isError" message="Could not load the list of frameworks." />
    <div v-else class="grid m-0">
      <div
        v-for="framework in filteredFrameworks"
        :key="framework.framework.id"
        class="col-12 md:col-6 lg:col-4 text-left"
      >
        <router-link
          :to="`/specifications/frameworks/${framework.framework.id}`"
          class="specification-card block surface-card shadow-1 p-3 border-round-sm"
          :data-test="`specificationFrameworkCard-${framework.framework.id}`"
        >
          <div class="font-semibold text-lg">{{ framework.name }}</div>
          <div class="text-color-secondary">{{ framework.framework.id }}</div>
        </router-link>
      </div>
      <p v-if="filteredFrameworks.length === 0" class="col-12">No frameworks match your search.</p>
    </div>
  </TheContent>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import InputText from 'primevue/inputtext';
import TheContent from '@/components/generics/TheContent.vue';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import { useFrameworkListQuery } from '@/api-queries/specification/useFrameworkListQuery.ts';

const searchTerm = ref('');

const { data: frameworks, isPending, isError } = useFrameworkListQuery();

const filteredFrameworks = computed(() => {
  const term = searchTerm.value.trim().toLowerCase();
  const list = frameworks.value ?? [];
  if (!term) {
    return list;
  }
  return list.filter(
    (framework) => framework.name.toLowerCase().includes(term) || framework.framework.id.toLowerCase().includes(term)
  );
});
</script>

<style scoped>
.specification-card {
  text-decoration: none;
  color: inherit;
  height: 100%;
}
</style>
