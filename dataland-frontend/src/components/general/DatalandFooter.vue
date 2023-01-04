<template>
  <div>
    <div class="surface-ground pt-8">
      <div class="grid m-0">
        <div class="col-4 col-offset-1">
          <div class="grid text-left">
            <div class="col-12 m-0 p-0">
              <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pt-2" />
            </div>
            <div class="col-12">
              <p class="font-medium text-xl">
                Come together to create a dataset that nobody can create alone while sharing the costs.
              </p>
            </div>
          </div>
        </div>
        <div class="col-offset-3 col-3 justify-content-center">
          <div class="grid text-left">
            <div class="col-12">
              <p class="font-semibold m-0">Legal</p>
            </div>
            <div class="col-12">
              <router-link to="/imprint" class="no-underline">
                <span class="font-semibold underline text-primary cursor-pointer" title="imprint">Imprint</span>
              </router-link>
              <router-link to="/dataprivacy" class="no-underline">
                <p class="font-semibold underline text-primary cursor-pointer" title="data privacy">Data Privacy</p>
              </router-link>
            </div>
          </div>
        </div>
      </div>
      <div class="grid m-0">
        <div class="col-offset-1 col-11 text-left">
          <p>Copyright &#169; 2022 Dataland</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: "DatalandFooter",
};
</script>
