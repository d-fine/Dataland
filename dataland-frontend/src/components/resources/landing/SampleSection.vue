<template>
  <div class="surface-800">
    <div class="grid m-0">
      <div class="col-offset-3 col-3">
        <div class="grid">
          <div class="col-10 text-left text-gray-100">
            <h2 class="mb-0">Explore Dataland</h2>
            <h2 class="mt-0">No registration required.</h2>
          </div>
          <div class="col-10 text-left text-gray-100">
            <p>Preview EU Taxonomy Data of <strong>BMW</strong>, before joining Dataland.</p>
          </div>
          <div class="col-10 text-left p-fluid">
            <router-link to="/taxonomysample" class="text-primary no-underline font-bold">
              <Button
                class="bg-white border-primary border-2 text-900 mt-2 mb-3 h-3rem p-0 justify-content-center"
                name="eu_taxonomy_sample_button"
              >
                <span class="uppercase text-primary d-letters font-semibold"> EU Taxonomy sample data </span>
                <i class="material-icons ml-2 text-primary" aria-hidden="true">chevron_right</i>
              </Button>
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

<script>
import Button from "primevue/button";
export default {
  name: "SampleSection",
  components: { Button },
};
</script>
