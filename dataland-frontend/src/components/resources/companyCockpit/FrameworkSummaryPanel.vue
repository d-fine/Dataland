<template>
  <div class="summary-panel">
    <div class="summary-panel__titles">
      <p class="summary-panel__title">
        {{ title }}
      </p>
      <p class="summary-panel__subtitle">
        {{ subtitle }}
      </p>
    </div>
    <div class="summary-panel__separator" />
    <div>
      <span class="summary-panel__data" v-if="props.numberOfProvidedReportingPeriods != undefined">
        <span class="summary-panel__value">
          {{ props.numberOfProvidedReportingPeriods }}
        </span>
        <template v-if="props.numberOfProvidedReportingPeriods == 1"> Reporting Period</template>
        <template v-else> Reporting Periods</template>
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, ref} from "vue";
import {DataTypeEnum} from "@clients/backend";
import {humanizeStringOrNumber} from "@/utils/StringHumanizer";

const props = defineProps<{
  framework: DataTypeEnum;
  numberOfProvidedReportingPeriods?: number | null;
}>();
const framework = ref(props.framework);

const euTaxonomyFrameworks = new Set<DataTypeEnum>([DataTypeEnum.EutaxonomyFinancials, DataTypeEnum.EutaxonomyNonFinancials]);
const title = computed(() => {
  if(!euTaxonomyFrameworks.has(framework.value)) {
    return humanizeStringOrNumber(framework.value as string);
  } else {
    return "EU Taxonomy";
  }
});
const subtitle = computed(() => {
  if(!euTaxonomyFrameworks.has(framework.value)) {
    return "";
  } else if(framework.value == DataTypeEnum.EutaxonomyFinancials) {
    return "for financial companies";
  } else {
    return "for non-financial companies";
  }
})

</script>

<style scoped lang="scss">
.summary-panel {
  width: 339px;
  height: 282px;
  background-color: var(--surface-card);
  padding: 24px;
  border-radius: 8px;
  text-align: left;
  box-shadow: 0 0 12px #9494943d;
  &:hover {
    box-shadow: 0 0 32px 8px #1e1e1e14;
  }
  &:hover & {
    &__separator {
      border-bottom-color: var(--primary-color);
    }
  }

  &__titles {
  }

  &__title {
  }

  &__subtitle {
  }

  &__separator {
    width: 100%;
    border-bottom: #e0dfde solid 1px;
  }

  &__data {

  }

  &__value {

  }
}
</style>