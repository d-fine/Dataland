<template>
  <div class="grid m-0">
    <div class="col-5 col-offset-1 pt-5 text-left">
      <h2 class="uppercase font-light d-letters text-4xl">Learn about our vision</h2>
    </div>
    <div class="col-1 pt-6 text-right">
      <img src="@/assets/images/elements/orange_short_line.svg" alt="short orange line" class="pl-2" />
    </div>
    <div class="col-3 text-left pt-6 font-medium d-letters text-xl">
      Our platform enables organisations to get all the data they need by requesting it directly from data owners while
      giving them a fair compensation.
    </div>
  </div>
  <div class="grid align-items-center m-0">
    <div class="col-offset-1 col-10">

    </div>
  </div>
  <div class="grid m-0 pt-6">
    <div class="col-offset-1 col-5 p-0 top-left-blue-border">
      <img src="@/assets/images/elements/flow_diagramm.svg" alt="Flow Diagramm" class="pl-0 w-full" />
    </div>
    <div class="col-5 p-0 bg-primary">
      <div class="grid mt-6">
        <div class="col-8 col-offset-1 align-content-center">
          <h3 class="text-left d-letters text-xl">Bring together who needs the data with who owns the data.</h3>
          <p class="text-left d-letters">
            Dataland is a permissionless platform for data exchange. We provide a protocol for allowing data users to
            purchase the data directly from its owners, open to use by any organizations.
          </p>
        </div>
      </div>
    </div>
  </div>
  <div class="grid m-0 pb-6">
    <div class="col-5 col-offset-1 p-0 surface-700">
      <div class="grid mt-6">
        <div class="col-8 col-offset-1 align-content-center">
          <h3 class="text-white text-left d-letters text-xl">
            Maximize data coverage by requesting data collectively.
          </h3>
          <p class="text-white text-left d-letters">
            Our platform enables organizations to directly request data from companies, customers and suppliers. By
            joining many other data users requesting data through Dataland, you can obtain data that is unavailable fom
            data providers and fill all your gaps.
          </p>
        </div>
      </div>
    </div>
    <div class="col-5 p-0 right-bottom-blue-border">
      <img src="@/assets/images/elements/data_workflow.svg" alt="Data Workflow" class="pl-0 pt-0 w-full" />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "MarketingSection",
});
</script>
