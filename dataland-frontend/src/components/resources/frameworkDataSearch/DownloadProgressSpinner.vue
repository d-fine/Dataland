<template>
  <transition
    name="spinner-transition"
    enter-active-class="spinner-transition-enter-active"
    leave-active-class="spinner-transition-leave-active"
  >
    <div v-if="displayAnything" style="position: relative; width: 1.5rem">
      <span v-if="displaySpinner" class="progress-spinner-container">
        <i class="pi pi-spin pi-spinner progress-spinner-spinner" />
        <div class="progress-spinner-value">{{ percentCompleted }}%</div>
      </span>
      <span v-if="displayCheckmark" class="progress-spinner-container">
        <i class="pi pi-check progress-completed-checkmark" />
      </span>
    </div>
  </transition>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "DownloadProgressSpinner",
  props: {
    percentCompleted: { type: Number, default: undefined },
  },
  data() {
    return {
      displaySpinner: false,
      displayCheckmark: false,
    };
  },
  watch: {
    percentCompleted(newValue) {
      if (newValue == undefined) {
        this.displaySpinner = false;
        this.displayCheckmark = false;
      } else if (newValue == 100) {
        this.displaySpinner = false;
        this.displayCheckmark = true;
      } else {
        this.displaySpinner = true;
        this.displayCheckmark = false;
      }
    },
  },
  computed: {
    displayAnything() {
      return this.displaySpinner || this.displayCheckmark;
    },
  },
});
</script>
<style lang="scss" scoped>
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
  font-size: 0.45rem;
  color: black;
}

.progress-spinner-spinner {
  font-size: 1.5rem;
  color: $orange-prime;
}

.progress-completed-checkmark {
  position: absolute;
  top: 55%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 1.2rem;
  color: $orange-prime;
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
