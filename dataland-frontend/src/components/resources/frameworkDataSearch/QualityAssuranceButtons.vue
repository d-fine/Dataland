<template>
  <PrimeButton
    class="uppercase p-button-outlined p-button-sm mr-3"
    @click="setQaStatusTo($event, QaStatus.Rejected)"
    :disabled="reviewSubmitted"
  >
    <i class="material-icons"> clear </i>
    <span class="d-letters pl-2"> Reject </span>
  </PrimeButton>
  <PrimeButton
    class="uppercase p-button p-button-sm"
    @click="setQaStatusTo($event, QaStatus.Accepted)"
    :disabled="reviewSubmitted"
  >
    <i class="material-icons"> done </i>
    <span class="d-letters pl-2"> Approve </span>
  </PrimeButton>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import PrimeButton from "primevue/button";
import type Keycloak from "keycloak-js";
import { QaStatus } from "@clients/qaservice";
import QADatasetModal from "@/components/general/QaDatasetModal.vue";
import { type DataMetaInformation } from "@clients/backend";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "QualityAssuranceButtons",
  components: { PrimeButton, QADatasetModal },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      reviewSubmitted: false,
      reviewSuccessful: false,
      QaStatus,
    };
  },
  props: {
    metaInfo: { type: Object, required: true },
  },
  methods: {
    /**
     * Sets dataset quality status to the given status
     * @param event the click event
     * @param qaStatus the QA status to be assigned
     */
    async setQaStatusTo(event: MouseEvent, qaStatus: QaStatus) {
      event.preventDefault();
      event.stopImmediatePropagation();

      const companyName = await this.getCompanyName();
      const { dataId, dataType, reportingPeriod } = this.metaInfo as DataMetaInformation;
      const message = `${qaStatus} ${dataType} data for ${companyName} for the reporting period ${reportingPeriod}.`;

      this.$dialog.open(QADatasetModal, {
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
          void this.$router.push("/qualityassurance");
        },
      });
    },
    /**
     * @returns a promise including the company name
     */
    async getCompanyName() {
      const companyDataControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)(),
      ).getCompanyDataControllerApi();
      const response = await companyDataControllerApi.getCompanyById((this.metaInfo as DataMetaInformation).companyId);
      return response.data.companyInformation.companyName;
    },
  },
});
</script>
