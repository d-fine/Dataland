<template>
  <PrimeButton
    class="uppercase p-button-outlined p-button-sm mr-3"
    @click="setQaStatusTo($event, QaStatus.Rejected)"
    :disabled="reviewSubmitted"
    data-test="qaRejectButton"
  >
    <i class="material-icons"> clear </i>
    <span class="d-letters pl-2"> Reject </span>
  </PrimeButton>
  <PrimeButton
    class="uppercase p-button p-button-sm"
    @click="setQaStatusTo($event, QaStatus.Accepted)"
    :disabled="reviewSubmitted"
    data-test="qaApproveButton"
  >
    <i class="material-icons"> done </i>
    <span class="d-letters pl-2"> Approve </span>
  </PrimeButton>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import PrimeButton from 'primevue/button';
import { QaStatus } from '@clients/qaservice';
import QaDatasetModal from '@/components/general/QaDatasetModal.vue';
import { type DataMetaInformation } from '@clients/backend';
import router from '@/router';

export default defineComponent({
  name: 'QualityAssuranceButtons',
  components: { PrimeButton },
  data() {
    return {
      reviewSubmitted: false,
      QaStatus,
    };
  },
  props: {
    metaInfo: {
      type: Object as () => DataMetaInformation,
      required: true,
    },
    companyName: { type: String, required: true },
  },
  methods: {
    /**
     * Sets dataset quality status to the given status
     * @param event the click event
     * @param qaStatus the QA status to be assigned
     */
    setQaStatusTo(event: MouseEvent, qaStatus: QaStatus) {
      const { dataId, dataType, reportingPeriod } = this.metaInfo;
      const message = `${qaStatus} ${dataType} data for ${this.companyName} for the reporting period ${reportingPeriod}.`;

      this.$dialog.open(QaDatasetModal, {
        props: {
          header: qaStatus,
          modal: true,
          dismissableMask: false,
        },
        data: {
          dataId,
          qaStatus,
          message,
        },
        onClose: () => {
          void router.push('/qualityassurance');
        },
      });
    },
  },
});
</script>
<style scoped>
.d-letters {
  letter-spacing: 0.05em;
}

.p-button {
  white-space: nowrap;
  cursor: pointer;
  font-weight: var(--button-fw);
  text-decoration: none;
  min-width: 10em;
  width: fit-content;
  justify-content: center;
  display: inline-flex;
  align-items: center;
  vertical-align: bottom;
  flex-direction: row;
  letter-spacing: 0.05em;
  font-family: inherit;
  transition: all 0.2s;
  border-radius: 0;
  text-transform: uppercase;
  font-size: 0.875rem;

  &:enabled:hover {
    color: white;
    background: hsl(from var(--btn-primary-bg) h s calc(l - 20));
    border-color: hsl(from var(--btn-primary-bg) h s calc(l - 20));
  }

  &:enabled:active {
    background: hsl(from var(--btn-primary-bg) h s calc(l - 10));
    border-color: hsl(from var(--btn-primary-bg) h s calc(l - 10));
  }

  &:disabled {
    background-color: transparent;
    border: 0;
    color: var(--btn-disabled-color);
    cursor: not-allowed;
  }

  &:focus {
    outline: 0 none;
    outline-offset: 0;
    box-shadow: 0 0 0 0.2rem var(--btn-focus-border-color);
  }
}

.p-button {
  color: var(--btn-primary-color);
  background: var(--btn-primary-bg);
  border: 1px solid var(--btn-primary-bg);
  padding: var(--spacing-xs) var(--spacing-md);
  line-height: 1rem;
  margin: var(--spacing-xxs);

  &.p-button-sm {
    font-size: var(--font-size-sm);
    padding: var(--spacing-xs) var(--spacing-sm);
  }
}
</style>
