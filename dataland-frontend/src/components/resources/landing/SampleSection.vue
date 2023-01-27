<template>
  <div class="second-section">
    <div class="grid m-0">
      <div class="col-offset-3 col-3">
        <div class="grid">
          <div class="col-10 text-left">
            <h2 class="mb-0">Explore Dataland.</h2>
            <h2 class="mt-0">No preview account required.</h2>
          </div>
          <div class="col-10 text-left">
            <p>View the EU Taxonomy Data of <strong>BMW</strong>, before creating a preview account.</p>
          </div>
          <div class="col-10 text-left p-fluid">
            <router-link :to="/samples/+DataTypeEnum.EutaxonomyNonFinancials" class="text-primary no-underline font-bold">
              <PrimeButton
                class="bg-white border-primary border-2 text-900 mt-2 mb-3 h-3rem p-0 justify-content-center"
                name="eu_taxonomy_sample_button"
              >
                <span class="uppercase text-primary d-letters font-semibold"> EU Taxonomy sample data </span>
                <i class="material-icons ml-2 text-primary" aria-hidden="true">chevron_right</i>
              </PrimeButton>
            </router-link>
          </div>
        </div>
      </div>
      <div class="col-3">
        <img src="@/assets/images/elements/eu_screenshot.png" alt="Dataland Logo" class="pl-3 pt-2" />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { DataTypeEnum} from "@clients/backend";
export default {
  name: "SampleSection",
  components: { PrimeButton },
  data() {
    return {
      DataTypeEnum
    };
  },
};


</script>
