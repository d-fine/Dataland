<template>
  <div class="container">
    <h2>New Dataset - PCAF</h2>
    <Divider />
    <div v-if="waitingForData">
      <h2>Loading PCAF data...</h2>
      <DatalandProgressSpinner />
    </div>
    <Form
      v-slot="$form"
      :initial-values="companyAssociatedPcafData"
      @submit="postNuclearAndGasData"
      v-else
      class="uploadFormWrapper form-container"
    >
      <div class="form-content">
        <div class="subcategory-container">
          <div class="label-container">
            <h4 class="subcategory-label">Reporting Period</h4>
          </div>
          <div class="form-field-container">
            <UploadFormHeader
              label='Reporting Period'
              description='The year for which the data is reported.'
            />
            <DatePicker
              id="reporting-period-picker"
              v-model="reportingPeriod"
              :showIcon="true"
              view="year"
              dateFormat="yy"
              validation="required"
            />
          </div>
        </div>

        <div v-for="category in pcafDataModel" :key="category.name">
          <div v-for="subcategory in category.subcategories" :key="subcategory.name">
            <div v-if="subcategoryVisibilityMap.get(subcategory) ?? true">
              <div class="subcategory-container">
                <div class="label-container">
                  <h4 :id="subcategory.name" class="subcategory-label">{{ subcategory.label }}</h4>
                  <Tag :value="category.label.toUpperCase()" severity="secondary" />
                </div>
                <div class="form-field-container">
                  <div v-for="field in subcategory.fields" :key="field.name">
                    <component
                      v-if="field.showIf(companyAssociatedPcafData?.data as NuclearAndGasData)"
                      :is="getComponentByName(field.component)"
                      :label="field.label"
                      :placeholder="field.placeholder"
                      :description="field.description"
                      :name="field.name"
                      :options="field.options"
                      :required="field.required"
                      :validation="field.validation"
                      :validation-label="field.validationLabel"
                      :data-test="field.name"
                      :unit="field.unit"
                      class="form-field"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="sidebar">
        <PrimeButton type="submit" label="SUBMIT DATA" fluid />
        <div v-if="isInputValidated" class="message-container">
          <Message v-if="isInputValid" severity="success">Upload successfully executed.</Message>
          <Message v-else severity="error">{{ errorMessage }}</Message>
        </div>

        <h4>On this page</h4>
        <ul>
          <li v-for="category in pcafDataModel" :key="category.name">
            <ul>
              <li v-for="subcategory in category.subcategories" :key="subcategory.name">
                <a v-if="subcategoryVisibilityMap.get(subcategory) ?? true"
                   :href='`#${subcategory.name}`'
                >
                  {{ subcategory.label }}
                </a>
              </li>
            </ul>
          </li>
        </ul>
      </div>
    </Form>
  </div>
</template>

<script setup lang="ts">
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { getComponentByName } from '@/components/forms/UploadPageComponentDictionary.ts';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { pcafDataModel } from '@/frameworks/pcaf/UploadConfig.ts';
import { ApiClientProvider } from '@/services/ApiClients';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { getFilledKpis } from '@/utils/DataPoint.ts';
import type { Subcategory } from '@/utils/GenericFrameworkTypes.ts';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { objectDropNull } from '@/utils/UpdateObjectUtils.ts';
import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils.ts';
import { type CompanyAssociatedDataPcafData, DataTypeEnum, type NuclearAndGasData, type PcafData } from '@clients/backend';
import { Form } from '@primevue/forms';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import DatePicker from 'primevue/datepicker';
import Divider from 'primevue/divider';
import Message from 'primevue/message';
import Tag from 'primevue/tag';
import { computed, inject, onMounted, ref } from 'vue';
import { type LocationQueryValue, useRoute } from 'vue-router';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const props = defineProps({
  companyID: {
    type: String,
    required: true
  },
});
const emits = defineEmits(['datasetCreated']);
const route = useRoute();

const companyAssociatedPcafData = ref<CompanyAssociatedDataPcafData>();
const errorMessage = ref('');
const isInputValid = ref(false);
const isInputValidated = ref(false);
const listOfFilledKpis = ref<string[]>();
const reportingPeriod = ref<Date | undefined>(undefined);
const templateDataId: LocationQueryValue | LocationQueryValue[] = route.query.templateDataId;
const templateReportingPeriod: LocationQueryValue | LocationQueryValue[] = route.query.reportingPeriod;
const waitingForData = ref(false);

const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const frameworkDefinition = getBasePublicFrameworkDefinition(DataTypeEnum.Pcaf);
const pcafDataApi: PublicFrameworkDataApi<PcafData> | undefined =
  frameworkDefinition?.getPublicFrameworkApiClient(undefined, apiClientProvider.axiosInstance);

const subcategoryVisibilityMap = computed((): Map<Subcategory, boolean> => {
  if (companyAssociatedPcafData.value) {
    return createSubcategoryVisibilityMap(pcafDataModel, companyAssociatedPcafData.value?.data);
  }
  return new Map<Subcategory, boolean>();
});

onMounted(() => {
  if (
    (templateDataId && typeof templateDataId === 'string') ||
    (templateReportingPeriod && typeof templateReportingPeriod === 'string')
  ) {
    console.log('templateDataId: ', templateDataId);
    console.log('templateReportingPeriod: ', templateReportingPeriod);

    void loadPcafData();
  }
});

/**
 * Loads the PCAF dataset identified either by the provided reportingPeriod and companyId,
 * or the dataId, and pre-configures the form to contain the data from the dataset
 */
async function loadPcafData(): Promise<void> {
  waitingForData.value = true;
  let pcafData;
  try {
    if (templateDataId) {
      pcafData = (await pcafDataApi!.getFrameworkData(templateDataId.toString())).data;
    } else if (templateReportingPeriod) {
      pcafData = (
        await pcafDataApi!.getCompanyAssociatedDataByDimensions(templateReportingPeriod.toString(), props.companyID)
      )?.data;
    }
    if (!pcafData) {
      throw ReferenceError('DataResponse from PcafDataController invalid.');
    }
    listOfFilledKpis.value = getFilledKpis(pcafData);
    companyAssociatedPcafData.value = objectDropNull(pcafData) as CompanyAssociatedDataPcafData;
  } catch (e) {
    console.error("Error while loading PCAF data", e)
  } finally {
    waitingForData.value = false;
  }
}

/**
 * Sends data to add NuclearAndGas data
 */
async function postNuclearAndGasData(): Promise<void> {
  try {

  } catch (e) {

  } finally {
    isInputValidated.value = true;
  }
}

</script>

<style scoped>
.container {
  background-color: var(--p-surface-50);
  padding: var(--spacing-xl) var(--spacing-xxl);
  text-align: start;
}

.form-container {
  display: flex;
  gap: var(--spacing-xl);

  .form-content {
    flex-grow: 5;

    .subcategory-container {
      margin-bottom: var(--spacing-xl);
      display: flex;

      .label-container {
        flex-basis: 25%;

        h4 {
          margin-bottom: var(--spacing-xxs);
        }
      }

      .form-field-container {
        flex: auto;
        padding: var(--spacing-md);
        background-color: var(--p-surface-0);
      }
    }
  }

  .sidebar {
    margin-left: auto;
    text-align: start;
    flex-grow: 1;

    .message-container {
      margin: var(--spacing-sm) 0;
    }

    ul {
      margin: 0;
      padding: 0;

      li {
        list-style: none;
        margin: 0.5rem 0;

        a {
          color: var(--p-surface-500);
          text-decoration: none;

          &:hover {
            color: var(--p-primary-color);
            cursor: pointer;
          }
        }
      }
    }
  }
}
</style>
