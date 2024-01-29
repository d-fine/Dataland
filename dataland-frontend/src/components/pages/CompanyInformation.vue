<template>
  <div>
    <div v-if="waitingForData" class="inline-loading text-center">
      <p class="font-medium text-xl">Loading company information...</p>
      <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f"/>
    </div>
    <div v-else-if="companyInformation && !waitingForData" class="company-details">
      <div class="company-details__headline">
        <div class="left-elements">
          <h1 data-test="companyNameTitle">{{ companyInformation.companyName }}
          </h1>
          <div class="p-badge badge-light-green outline" v-if="isUserDataOwner">
            <span class="material-icons-outlined">verified</span>
            Verified Data Owner
          </div>
        </div>
        <div>
          <ContextMenuButton :menu-items="contextMenuItems"/>
        </div>
      </div>
      <div class="company-details__separator"/>

      <div class="company-details__info-holder">
        <div class="company-details__info">
          <span>Sector: </span>
          <span class="font-semibold">{{ displaySector }}</span>
        </div>
        <div class="company-details__info">
          <span>Headquarter: </span>
          <span class="font-semibold">{{ companyInformation.headquarters }}</span>
        </div>
        <div class="company-details__info">
          <span>ISIN: </span>
          <span class="font-semibold">{{ displayIsin }}</span>
        </div>
      </div>
    </div>
    <div v-else-if="companyIdDoesNotExist" class="col-12">
      <h1 class="mb-0" data-test="noCompanyWithThisIdErrorIndicator">No company with this ID present</h1>
    </div>
  </div>
</template>

<script lang="ts">
import {ApiClientProvider} from "@/services/ApiClients";
import {defineComponent, inject} from "vue";
import {type CompanyInformation, IdentifierType} from "@clients/backend";
import type Keycloak from "keycloak-js";
import {assertDefined} from "@/utils/TypeScriptUtils";
import {getUserId} from "@/utils/KeycloakUtils";
import ContextMenuButton from "@/components/general/ContextMenuButton.vue";
import {AxiosError} from "axios";

export default defineComponent({
  name: "CompanyInformation",
  components: {ContextMenuButton},
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  emits: ["fetchedCompanyInformation", "fetchedDataOwnerInformation", "claimDataOwnership"],
  data() {
    return {
      companyInformation: null as CompanyInformation | null,
      waitingForData: true,
      companyIdDoesNotExist: false,
      isUserDataOwner: false,
    };
  },
  computed: {
    displaySector() {
      if (this.companyInformation?.sector) {
        return this.companyInformation?.sector;
      } else {
        return "—";
      }
    },
    displayIsin() {
      return this.companyInformation?.identifiers?.[IdentifierType.Isin]?.[0] ?? "—";
    },
    userId() {
      return getUserId(assertDefined(this.getKeycloakPromise));
    },
    contextMenuItems() {
      const listOfItems = [
        {
          label: 'Manage Company Details',
          command: () => {
            console.log("I dont know where to route this ? #TODO TODO")
          }

        }];
      if (!this.isUserDataOwner) {
        listOfItems.push(
            {
              label: 'Claim Company Dataset Ownership',
              command: () => {
                this.$emit('claimDataOwnership');
              }
            }
        )
      }
      return listOfItems;
    }
  },
  props: {
    companyId: {
      type: String,
      required: true,
    },
  },
  mounted() {
    void this.getCompanyInformation();
    void this.getDataOwnerInformation();
  },
  watch: {
    companyId() {
      void this.getCompanyInformation();
    },


  },
  methods: {
    /**
     * Uses the dataland API to retrieve information about the company identified by the local
     * companyId object.
     */
    async getCompanyInformation() {
      try {
        this.waitingForData = true;
        if (this.companyId !== undefined) {
          const companyDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)())
              .backendClients.companyDataController;
          this.companyInformation = (await companyDataControllerApi.getCompanyInfo(this.companyId)).data;
          this.waitingForData = false;
          this.$emit("fetchedCompanyInformation", this.companyInformation);

        }
      } catch (error) {
        console.error(error);
        if (this.getErrorMessage(error).includes("404")) {
          this.companyIdDoesNotExist = true;
        }
        this.waitingForData = false;
        this.companyInformation = null;
      }
    },
    /**
     * Tries to find a message in an error
     * @param error the error to extract a message from
     * @returns the extracted message
     */
    getErrorMessage(error: unknown) {
      const noStringMessage = error instanceof Error ? error.message : "";
      return typeof error === "string" ? error : noStringMessage;
    },
    /**
     * Get the Information about Data-ownership
     */
    async getDataOwnerInformation() {

      const companyDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)())
          .backendClients.companyDataController;
      try {
        const axiosResponse = (await companyDataControllerApi.isUserDataOwnerForCompany(
                this.companyId, assertDefined(await this.userId))
        );
        if (axiosResponse.status == 200) {
          this.isUserDataOwner = true;
        }
      } catch (error: AxiosError) {
        if (assertDefined((error as AxiosError).response).status == 404) {
          this.isUserDataOwner = false;
        } else {
          console.error(error);
        }
      }
      this.$emit('fetchedDataOwnerInformation', this.isUserDataOwner);
    }
  },
});
</script>

<style scoped lang="scss">
.inline-loading {
  width: 450px;
}

.company-details {
  display: flex;
  flex-direction: column;
  width: 100%;

  &__headline {
    display: flex;
    justify-content: space-between;
    flex-direction: row;
    align-items: center;
  }

  &__separator {
    @media only screen and (max-width: $small) {
      width: 100%;
      border-bottom: #e0dfde 1px solid;
      margin-bottom: 0.5rem;
    }
  }

  &__info-holder {
    display: flex;
    flex-direction: row;
    @media only screen and (max-width: $small) {
      flex-direction: column;
    }
  }

  &__info {
    padding-top: 0.3rem;
    @media only screen and (min-width: $small) {
      padding-right: 40px;
    }
  }
}

.left-elements {
  display: flex;
  align-items: center;
}

</style>
