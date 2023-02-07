<template>
  <div>
    <div class="surface-ground pt-8 footer">
      <div class="grid m-0">
        <div class="lg:col-4 md:col-4 col-12 legend">
          <div class="grid text-left">
            <div class="col-12">
              <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pt-2" />
            </div>
            <div class="col-12">
              <p
                :class="[!isMobile ? ['font-medium', 'text-xl'] : ['uppercase', 'font-light', 'd-letters', 'text-3xl']]"
              >
                Come together to create a dataset that nobody can create alone while sharing the costs.
              </p>
            </div>
          </div>
        </div>
        <div class="lg:col-3 md:col-3 col-12 justify-content-center legal">
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
        <div class="col-12 text-left">
          <p>Copyright &#169; 2023 Dataland</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: "DatalandFooter",
  props: {
    isMobile: {
      type: Boolean,
    },
  },
};
</script>
<style scoped lang="scss">
.footer {
  padding: 8vw;
  align-content: space-between;
  .legal {
    margin-left: auto;
  }
}
</style>
