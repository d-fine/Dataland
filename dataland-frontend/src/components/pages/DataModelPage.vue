<template>
  <TheContent class="container">
    <main class="data-model-page">
      <div class="header-row">
        <h1>Data Model — {{ selectedModelLabel }}</h1>
        <div class="model-select">
          <label for="modelSelect">Model</label>
          <select id="modelSelect" v-model="selectedModelId">
            <option v-for="m in AVAILABLE_DATA_MODELS" :key="m.id" :value="m.id">{{ m.label }}</option>
          </select>
          <span v-if="loadingModel">Loading…</span>
        </div>
      </div>

      <div class="search-row">
        <input
          v-model="q"
          type="search"
          placeholder="Search object path, field name, label, component, description..."
          aria-label="Search data model"
        />
        <button @click="q = ''">Clear</button>
      </div>

      <div class="panel">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th class="col-objectPath">Object Path</th>
                <th class="col-fieldName">Field Name</th>
                <th class="col-label">Label</th>
                <th class="col-component">Component / Type</th>
                <th class="col-description">Description</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in filteredRows" :key="row.id">
                <td class="col-objectPath">{{ row.objectPath }}</td>
                <td class="col-fieldName">{{ row.fieldName }}</td>
                <td class="col-label">{{ row.label }}</td>
                <td class="col-component">{{ row.component ?? row.type ?? '' }}</td>
                <td class="col-description">{{ row.description ?? '' }}</td>
              </tr>
              <tr v-if="filteredRows.length === 0">
                <td colspan="5">No results</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  </TheContent>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import TheContent from '@/components/generics/TheContent.vue';
import { AVAILABLE_DATA_MODELS, extractDataModel } from '@/frameworks/availableDataModels';

interface Row {
  id: string;
  objectPath: string;
  fieldName: string;
  label?: string;
  component?: string;
  description?: string;
}

const q = ref('');

const selectedModelId = ref<string>(AVAILABLE_DATA_MODELS[0]?.id ?? '');
const selectedModelLabel = ref<string>(AVAILABLE_DATA_MODELS[0]?.label ?? 'Data Model');
const selectedDataModel = ref<any[]>([]);
const loadingModel = ref(false);

async function loadModel(id: string) {
  const entry = AVAILABLE_DATA_MODELS.find((e) => e.id === id);
  if (!entry) {
    selectedDataModel.value = [];
    selectedModelLabel.value = 'Data Model';
    return;
  }
  selectedModelLabel.value = entry.label;
  loadingModel.value = true;
  try {
    if (entry.dataModel) {
      selectedDataModel.value = entry.dataModel as any[];
    } else if (entry.loader) {
      const mod = await entry.loader();
      const dm = await extractDataModel(mod);
      selectedDataModel.value = dm ?? [];
    } else {
      selectedDataModel.value = [];
    }
  } catch (err) {
    // swallow errors and present empty model
    selectedDataModel.value = [];
  } finally {
    loadingModel.value = false;
  }
}

onMounted(() => {
  if (selectedModelId.value) loadModel(selectedModelId.value);
});

watch(selectedModelId, (v) => loadModel(v));

const rows = computed<Row[]>(() => {
  const out: Row[] = [];
  (selectedDataModel.value as any[]).forEach((category: any) => {
    const catName = category.name ?? category.label ?? 'category';
    (category.subcategories || []).forEach((subcat: any) => {
      const subName = subcat.name ?? subcat.label ?? 'subcategory';
      (subcat.fields || []).forEach((field: any) => {
        out.push({
          id: `${catName}.${subName}.${field.name}`,
          objectPath: `${catName}.${subName}`,
          fieldName: field.name,
          label: field.label,
          component: field.component,
          description: field.description,
        });
      });
    });
  });
  return out;
});

const filteredRows = computed(() => {
  const term = q.value.trim().toLowerCase();
  if (!term) return rows.value;
  return rows.value.filter((r) => {
    return (
      String(r.objectPath).toLowerCase().includes(term) ||
      String(r.fieldName).toLowerCase().includes(term) ||
      String(r.label ?? '').toLowerCase().includes(term) ||
      String(r.component ?? '').toLowerCase().includes(term) ||
      String(r.description ?? '').toLowerCase().includes(term)
    );
  });
});
</script>

<style scoped>
.container > main.data-model-page {
  max-width: 1100px;
  margin: 1.5rem auto;
}
.data-model-page {
  padding: 1rem;
}
.search-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.75rem;
}
.model-select { display:flex; align-items:center; gap:0.5rem; }
.model-select label { font-size:0.9rem; }
.table-wrap {
  overflow-x: auto;
}
table {
  border-collapse: collapse;
  width: 100%;
  table-layout: fixed; /* use fixed layout so column widths are respected and rows wrap */
}
th, td {
  text-align: left;
  padding: 0.5rem;
  border-bottom: 1px solid var(--input-separator, #e6e6e6);
  vertical-align: top;
}
/* Column sizing: percentages chosen to match typical content lengths */
.col-objectPath { width: 18%; min-width: 120px; }
.col-fieldName { width: 12%; min-width: 100px; }
.col-label { width: 20%; min-width: 140px; }
.col-component { width: 15%; min-width: 120px; }
.col-description { width: 35%; min-width: 200px; }

/* Allow long text to wrap and break to fit the column */
.col-description, .col-label, .col-objectPath, .col-component, .col-fieldName {
  word-break: break-word;
  white-space: normal;
}

/* Improve readability on small screens */
@media (max-width: 900px) {
  .col-objectPath, .col-fieldName, .col-component { display: none; }
  table { table-layout: auto; }
}
</style>
