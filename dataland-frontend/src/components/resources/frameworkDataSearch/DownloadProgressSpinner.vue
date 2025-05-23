<template>
  <transition
    name="spinner-transition"
    enter-active-class="spinner-transition-enter-active"
    leave-active-class="spinner-transition-leave-active"
  >
    <div v-if="displayAnything" style="position: relative; width: 1.5rem">
      <span v-if="displaySpinner" class="progress-spinner-container">
        <i class="pi pi-spin pi-spinner progress-spinner-spinner" data-test="spinner-icon" />
        <span :class="progressSpinnerValueClass" data-test="percentage-text">{{ percentCompleted }}%</span>
      </span>
      <span v-if="displayCheckmark" class="progress-spinner-container">
        <i class="pi pi-check progress-completed-checkmark" data-test="checkmark-icon" />
      </span>
    </div>
  </transition>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'DownloadProgressSpinner',

  props: {
    percentCompleted: { type: Number, default: undefined },
    whiteSpinner: { type: Boolean, required: false, default: false },
  },

  computed: {
    displayAnything() {
      return this.displaySpinner || this.displayCheckmark;
    },

    displaySpinner() {
      return this.percentCompleted !== undefined && this.percentCompleted < 100;
    },

    displayCheckmark() {
      return this.percentCompleted === 100;
    },

    progressSpinnerValueClass() {
      return `progress-spinner-value ${this.whiteSpinner ? 'white' : 'black'}`;
    },
  },
});
</script>

<style lang="scss" scoped>
@use '@/assets/scss/variables';

.progress-spinner-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  flex: 1 0 auto;
  width: 1.5rem;
  height: 1.5rem;
}

.progress-spinner-value {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 0.45em;
  color: black;
}

.black {
  color: black;
}

.white {
  color: white;
}

.progress-spinner-spinner {
  font-size: 1.5rem;
  color: variables.$orange-prime;
}

.progress-completed-checkmark {
  position: absolute;
  top: 55%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 1.2rem;
  color: variables.$orange-prime;
}

.spinner-transition-enter-active {
  opacity: 1;
}

@keyframes spinner-transition {
  0% {
    opacity: 1;
  }
  75% {
    opacity: 1;
  }
  100% {
    opacity: 0;
  }
}

.spinner-transition-leave-active {
  animation: spinner-transition 2s;
}
</style>
