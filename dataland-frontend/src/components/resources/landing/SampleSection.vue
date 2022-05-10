<template>
  <div class="surface-section">
    <div class="grid">
      <div class="col-offset-4 col-2">
        <div class="grid">
          <div class="col-12"><h2>Explore Dataland. No registration required.</h2></div>
          <div class="col-12"><p>Preview EU Taxonomy Data of <strong>Adidas</strong>, before joining Dataland.</p></div>
          <div class="col-12">
            <Button class="bg-white border-primary border-2 text-900 mt-2 mb-3 h-3rem p-0">
            <span class="uppercase text-primary">
            EU Taxonomy sample data
          </span>
              <i class="material-icons ml-2 text-primary " aria-hidden="true">chevron_right</i>
            </Button>
          </div>
        </div>
      </div>
      <div class="col-2">
        <img src="@/assets/images/elements/eu_screenshot.png" alt="Dataland Logo" class="pl-3 pt-2"/>

      </div>
    </div>
  </div>
</template>

<script>
import Button from "primevue/button";
export default {
  name: "SampleSection",
  components: {Button}
}
</script>

<style scoped>

</style>