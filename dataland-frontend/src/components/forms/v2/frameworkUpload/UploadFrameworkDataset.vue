<template>
  <TheHeader />
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>Upload Dataset - LKSG</template>
    <template #content>
      <FormKit
        id="myForm"
        :key="'formkit-form-' + formKitKey"
        type="form"
        v-model="formData"
        :plugins="[datalandCustomComponentsPlugin]"
      >
        <!--        <FormKitSchema :schema="UploadConfigV2.formKitSchema" :data="schemaData" />-->

        <!--        <FormKit type="dataSource" :name="'fk-' + id" v-for="id in 500" />-->
        <FormKit type="multi-step" tab-style="tab">
          <FormKit type="step" :name="'step-' + sid" v-for="sid in 10">
            <FormKit type="dataSource" :name="'fk-' + id" v-for="id in 100" />
          </FormKit>
        </FormKit>
      </FormKit>
      <div class="flex">
        <textarea rows="25" cols="50" :value="JSON.stringify(formData, null, 2)"></textarea>
        <textarea rows="25" cols="50" :value="JSON.stringify(postProcessFormData(formData), null, 2)"></textarea>
        <textarea rows="25" cols="50" v-model="textAreaContents"></textarea>
        <button @click="resetFormData">Load</button>
      </div>
    </template>
  </Card>
</template>

<script setup lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import Card from "primevue/card";
import { FormKit, FormKitSchema } from "@formkit/vue";
import { provide, ref, reactive } from "vue";
import { createFrameworkDocumentStore } from "./FrameworkDocumentStore";
import UploadConfigV2 from "@/frameworks/lksg/UploadConfigV2";
import DatePicker from "@/components/forms/v2/fields/DatePicker.vue";
import DataSource from "@/components/forms/v2/fields/DataSource.vue";
import { type FormKitNode, type FormKitTypeDefinition, reset } from "@formkit/core";
import ToggleGroup from "@/components/forms/v2/fields/ToggleGroup.vue";
import { isObjectEmpty } from "@/utils/TypeScriptUtils";
import { deepCopyObject } from "@/utils/UpdateObjectUtils";

function postProcessFormData(data: Record<string, unknown>): Record<string, unknown> | undefined {
  const deepCopy = deepCopyObject(data);
  clean(deepCopy);
  return deepCopy;
}

function clean(data: Record<string, unknown>) {
  const keys = Object.keys(data);
  for (const key of keys) {
    if (data[key] == null || data[key] == undefined) {
      delete data[key];
    } else if (typeof data[key] === "object") {
      clean(data[key] as Record<string, unknown>);
      if (isObjectEmpty(data[key] as Record<string, unknown>)) {
        delete data[key];
      }
    }
  }
}

function resetFormData() {
  formKitKey.value += 1;
  reset("myForm", JSON.parse(textAreaContents.value));
}

const datalandComponentLibrary: { [key: string]: FormKitTypeDefinition<unknown> } = {
  datePicker: {
    type: "input",
    props: ["todayAsMax", "placeholder"],
    component: DatePicker,
  },
  dataSource: {
    type: "input",
    props: [],
    component: DataSource,
  },
  toggleGroup: {
    type: "group",
    props: [],
    component: ToggleGroup,
  },
};

const datalandCustomComponentsPlugin = () => {};
datalandCustomComponentsPlugin.library = (node: FormKitNode) => {
  if (node.props.type in datalandComponentLibrary) {
    node.define(datalandComponentLibrary[node.props.type]);
  }
};

const formData = ref({});
const formKitKey = ref(1);
const textAreaContents = ref("");
const schemaData = reactive({
  getYearFromDate: (date: string | undefined) => "ASD",
});

const formDocumentStore = createFrameworkDocumentStore();
provide("formDocumentStore", formDocumentStore);
</script>
