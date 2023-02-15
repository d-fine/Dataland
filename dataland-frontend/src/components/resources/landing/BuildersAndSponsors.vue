<template>
  <div class="grid center-divs-row-wrapper">
    <div class="center-divs-row">
      <div>
        <p class="uppercase mb-0">Built by</p>
        <img src="@/assets/images/elements/orange_short_line.svg" alt="short orange line" />
        <div class="imgs-in-line">
          <img src="@/assets/images/logos/pwc.svg" alt="pwc" />
          <img src="@/assets/images/logos/dfine.svg" alt="d-fine GmbH" class="d-small-logo" />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "BuildersAndSponsors",
});
</script>
<style scoped lang="scss">
.center-divs-row-wrapper {
  justify-content: center;
  .center-divs-row {
    display: flex;
    text-align: left;
    width: 80%;
    justify-content: space-around;
    max-width: 400px;
    margin: 1rem;
  }
}
.imgs-in-line {
  display: flex;
  justify-content: space-between;
  width: 100%;
  height: 60px;
  align-items: flex-end;
  img {
    margin: 0.5rem 2vw 0.5rem 0;
  }
}
</style>
