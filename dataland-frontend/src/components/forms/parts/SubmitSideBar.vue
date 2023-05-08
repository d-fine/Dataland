<template>
  <div data-test="submitSideBar" ref="submitSideBar" class="col-3 p-3 text-left jumpLinks">
    <slot></slot>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "SubmitSideBar",
  data: () => ({ elementPosition: 0, scrollListener: (): null => null }),
  mounted() {
    const submitSideBar = this.$refs.submitSideBar as HTMLElement;
    this.elementPosition = submitSideBar.getBoundingClientRect().top;
    this.scrollListener = (): null => {
      if (this.elementPosition == 0) {
        this.elementPosition = submitSideBar.getBoundingClientRect().top;
      }
      console.log(window.scrollY);
      if (window.scrollY > this.elementPosition) {
        submitSideBar.style.position = "fixed";
        submitSideBar.style.top = "60px";
      } else {
        submitSideBar.style.position = "relative";
        submitSideBar.style.top = "0px";
      }
      return null;
    };
    window.addEventListener("scroll", this.scrollListener);
  },
  unmounted() {
    window.removeEventListener("scroll", this.scrollListener);
  },
});
</script>

<style scoped>
:deep(h4) {
  margin-block-start: 0.5rem;
  margin-block-end: 0.5rem;
}

:deep(button.p-message-close) {
  min-width: 8px;
}

:deep(.p-message-wrapper) {
  padding: 0.5rem;
}
</style>
