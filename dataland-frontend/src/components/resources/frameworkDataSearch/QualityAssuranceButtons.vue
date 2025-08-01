<template>
  <PrimeButton
    @click="setQaStatusTo($event, QaStatus.Rejected)"
    :disabled="reviewSubmitted"
    data-test="qaRejectButton"
    icon="pi pi-times"
    label="Reject"
    variant="outlined"
  />
  <PrimeButton
    @click="setQaStatusTo($event, QaStatus.Accepted)"
    :disabled="reviewSubmitted"
    data-test="qaApproveButton"
    icon="pi pi-check"
    label="Approve"
  />
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
