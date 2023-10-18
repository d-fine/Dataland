<template>
  <div>
    <span class="mr-2">Language:</span>
    <Dropdown v-model="selectedLanguage" :options="languageOptions" optionLabel="label" optionValue="value" />
  </div>
  <div class="mt-2">
    <slot :name="selectedLanguage" />
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import { type DropdownOption } from "@/utils/PremadeDropdownDatasets";
import Dropdown from "primevue/dropdown";

const availableLanguages = ["english", "german"];
const languageOptions = ref(
  availableLanguages.map(
    (language): DropdownOption => ({
      label: humanizeStringOrNumber(language),
      value: language,
    }),
  ),
);
const selectedLanguage = ref<string>(availableLanguages[0]);
</script>
