<!-- INFO: This file exists to help you understand the new data table and experiment. TODO: Remove -->

<template>
  <MultiLayerDataTable :config="viewConfiguration" :datasets="datasets" style="width: 100%" />
</template>

<style scoped></style>

<script lang="ts">
import { defineComponent } from "vue";
import MultiLayerDataTable from "@/components/resources/dataTable/MultiLayerDataTable.vue";
import { type MLDTDataset } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";

import { convertDataModel } from "@/components/resources/dataTable/conversion/MultiLayerDataTableConfigurationConverter";
import { lksgDataModel } from "@/components/resources/frameworkDataSearch/lksg/LksgDataModel";
import { type LksgData, type SfdrData } from "@clients/backend";
import { sfdrDataModel } from "@/components/resources/frameworkDataSearch/sfdr/SfdrDataModel";

const demoDataset: MLDTDataset<SfdrData> = {
  headerLabel: "Testing",
  dataset: {
    general: {
      general: {
        dataDate: "2023-01-20",
        fiscalYearDeviation: "NoDeviation",
        fiscalYearEnd: "2023-06-12",
        referencedReports: {
          SustainabilityReport: {
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
            isGroupLevel: "Yes",
            reportDate: "2023-05-12",
            currency: "STN",
          },
          ESEFReport: {
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
            isGroupLevel: "No",
            reportDate: "2022-12-23",
            currency: "BND",
          },
        },
        scopeOfEntities: "No",
      },
    },
    environmental: {
      greenhouseGasEmissions: {
        scope1: null,
        scope2: {
          quality: "Estimated",
          dataSource: {
            report: "ESEFReport",
            page: 86,
            tagName: "initiatives",
          },
          comment: "program haptic pixel",
          value: 85111.23,
          unit: null,
        },
        scope3: {
          quality: "Audited",
          dataSource: {
            report: "SustainabilityReport",
            page: 581,
            tagName: "mindshare",
          },
          comment: "reboot primary alarm",
          value: 51909.07,
          unit: null,
        },
        enterpriseValue: {
          quality: "Audited",
          dataSource: {
            report: "SustainabilityReport",
            page: 1197,
            tagName: "schemas",
          },
          comment: "quantify virtual bandwidth",
          value: 7361.56,
          unit: null,
        },
        totalRevenue: {
          quality: "Incomplete",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: 88582.66,
          unit: null,
        },
        fossilFuelSectorExposure: null,
      },
      energyPerformance: {
        renewableEnergyProduction: {
          quality: "Reported",
          dataSource: {
            report: "SustainabilityReport",
            page: 1133,
            tagName: "metrics",
          },
          comment: "reboot back-end driver",
          value: 80879.01,
          unit: null,
        },
        renewableEnergyConsumption: {
          quality: "NA",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: null,
          unit: null,
        },
        nonRenewableEnergyConsumption: {
          quality: "Reported",
          dataSource: {
            report: "ESEFReport",
            page: 600,
            tagName: "platforms",
          },
          comment: "override solid state array",
          value: 87582.13,
          unit: null,
        },
        nonRenewableEnergyProduction: {
          quality: "Incomplete",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: 66182.73,
          unit: null,
        },
        highImpactClimateSectorEnergyConsumptionNaceA: {
          quality: "Audited",
          dataSource: {
            report: "ESEFReport",
            page: 390,
            tagName: "methodologies",
          },
          comment: "connect virtual capacitor",
          value: 89903.52,
          unit: null,
        },
        highImpactClimateSectorEnergyConsumptionNaceB: {
          quality: "NA",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: null,
          unit: null,
        },
        highImpactClimateSectorEnergyConsumptionNaceC: {
          quality: "NA",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: null,
          unit: null,
        },
        highImpactClimateSectorEnergyConsumptionNaceD: {
          quality: "Estimated",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: 81301.25,
          unit: null,
        },
        highImpactClimateSectorEnergyConsumptionNaceE: {
          quality: "Reported",
          dataSource: {
            report: "ESEFReport",
            page: 265,
            tagName: "solutions",
          },
          comment: "transmit multi-byte program",
          value: 17278.05,
          unit: null,
        },
        highImpactClimateSectorEnergyConsumptionNaceF: null,
        highImpactClimateSectorEnergyConsumptionNaceG: {
          quality: "NA",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: null,
          unit: null,
        },
        highImpactClimateSectorEnergyConsumptionNaceH: {
          quality: "Incomplete",
          dataSource: {
            report: "SustainabilityReport",
            page: 435,
            tagName: "initiatives",
          },
          comment: "connect virtual card",
          value: 65606.56,
          unit: null,
        },
        highImpactClimateSectorEnergyConsumptionNaceL: {
          quality: "Incomplete",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: 91876.52,
          unit: null,
        },
        totalHighImpactClimateSectorEnergyConsumption: {
          quality: "Estimated",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: 27863.47,
          unit: null,
        },
        nonRenewableEnergyConsumptionFossilFuels: {
          quality: "Incomplete",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: 48734.58,
          unit: null,
        },
        nonRenewableEnergyConsumptionCrudeOil: {
          quality: "Audited",
          dataSource: {
            report: "ESEFReport",
            page: 116,
            tagName: "action-items",
          },
          comment: "parse optical interface",
          value: 80224.72,
          unit: null,
        },
        nonRenewableEnergyConsumptionNaturalGas: {
          quality: "Reported",
          dataSource: {
            report: "ESEFReport",
            page: 307,
            tagName: "methodologies",
          },
          comment: "reboot back-end system",
          value: 86030.01,
          unit: null,
        },
        nonRenewableEnergyConsumptionLignite: null,
        nonRenewableEnergyConsumptionCoal: {
          quality: "NA",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: null,
          unit: null,
        },
        nonRenewableEnergyConsumptionNuclearEnergy: {
          quality: "Incomplete",
          dataSource: {
            report: "ESEFReport",
            page: 104,
            tagName: "content",
          },
          comment: "quantify wireless sensor",
          value: 49202.82,
          unit: null,
        },
        nonRenewableEnergyConsumptionOther: {
          quality: "Estimated",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: 47470.84,
          unit: null,
        },
      },
      biodiversity: {
        primaryForestAndWoodedLandOfNativeSpeciesExposure: null,
        protectedAreasExposure: {
          quality: "Reported",
          dataSource: {
            report: "SustainabilityReport",
            page: 721,
            tagName: "methodologies",
          },
          comment: "reboot virtual protocol",
          value: "Yes",
        },
        rareOrEndangeredEcosystemsExposure: {
          quality: "Reported",
          dataSource: {
            report: "ESEFReport",
            page: 269,
            tagName: "synergies",
          },
          comment: "hack open-source protocol",
          value: "No",
        },
      },
      water: {
        emissionsToWater: {
          quality: "Estimated",
          dataSource: {
            report: "SustainabilityReport",
            page: 537,
            tagName: "platforms",
          },
          comment: "synthesize online alarm",
          value: 56299.51,
          unit: null,
        },
        waterConsumption: {
          quality: "Audited",
          dataSource: {
            report: "SustainabilityReport",
            page: 300,
            tagName: "e-markets",
          },
          comment: "index bluetooth alarm",
          value: 21928.67,
          unit: null,
        },
        waterReused: {
          quality: "Incomplete",
          dataSource: {
            report: "SustainabilityReport",
            page: 1199,
            tagName: "technologies",
          },
          comment: "compress bluetooth driver",
          value: 77201.22,
          unit: null,
        },
        waterManagementPolicy: {
          quality: "Reported",
          dataSource: {
            report: "ESEFReport",
            page: 309,
            tagName: "synergies",
          },
          comment: "reboot haptic interface",
          value: "Yes",
        },
        waterStressAreaExposure: {
          quality: "NA",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: null,
        },
      },
      waste: {
        hazardousWaste: {
          quality: "Audited",
          dataSource: {
            report: "ESEFReport",
            page: 528,
            tagName: "mindshare",
          },
          comment: "quantify haptic protocol",
          value: 74513.97,
          unit: null,
        },
        manufactureOfAgrochemicalPesticidesProducts: null,
        landDegradationDesertificationSoilSealingExposure: {
          quality: "Audited",
          dataSource: {
            report: "SustainabilityReport",
            page: 1052,
            tagName: "users",
          },
          comment: "transmit cross-platform application",
          value: "No",
        },
        sustainableAgriculturePolicy: {
          quality: "Estimated",
          dataSource: {
            report: "SustainabilityReport",
            page: 666,
            tagName: "applications",
          },
          comment: "back up back-end program",
          value: "Yes",
        },
        sustainableOceansAndSeasPolicy: {
          quality: "Incomplete",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: "No",
        },
        wasteNonRecycled: null,
        threatenedSpeciesExposure: {
          quality: "Audited",
          dataSource: {
            report: "SustainabilityReport",
            page: 236,
            tagName: "eyeballs",
          },
          comment: "calculate multi-byte interface",
          value: "No",
        },
        biodiversityProtectionPolicy: {
          quality: "Estimated",
          dataSource: {
            report: "ESEFReport",
            page: 7,
            tagName: "niches",
          },
          comment: "copy mobile port",
          value: "Yes",
        },
        deforestationPolicy: {
          quality: "NA",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: null,
        },
      },
      emissions: {
        inorganicPollutants: {
          quality: "Audited",
          dataSource: {
            report: "SustainabilityReport",
            page: 416,
            tagName: "eyeballs",
          },
          comment: "hack wireless pixel",
          value: 89596.11,
          unit: null,
        },
        airPollutants: {
          quality: "NA",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: null,
          unit: null,
        },
        ozoneDepletionSubstances: {
          quality: "Audited",
          dataSource: {
            report: "SustainabilityReport",
            page: 989,
            tagName: "action-items",
          },
          comment: "reboot auxiliary card",
          value: 14101.92,
          unit: null,
        },
        carbonReductionInitiatives: null,
      },
    },
    social: {
      socialAndEmployeeMatters: {
        humanRightsLegalProceedings: {
          quality: "Incomplete",
          dataSource: {
            report: "SustainabilityReport",
            page: 50,
            tagName: "portals",
          },
          comment: "input primary bandwidth",
          value: "Yes",
        },
        iloCoreLabourStandards: null,
        environmentalPolicy: null,
        corruptionLegalProceedings: {
          quality: "Incomplete",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: "No",
        },
        transparencyDisclosurePolicy: {
          quality: "NA",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: null,
        },
        humanRightsDueDiligencePolicy: null,
        childForcedDiscriminationPolicy: {
          quality: "Reported",
          dataSource: {
            report: "ESEFReport",
            page: 310,
            tagName: "action-items",
          },
          comment: "quantify solid state firewall",
          value: "Yes",
        },
        iso14001Certificate: {
          quality: "Incomplete",
          dataSource: {
            report: "SustainabilityReport",
            page: 668,
            tagName: "systems",
          },
          comment: "program neural port",
          value: "No",
        },
        briberyCorruptionPolicy: {
          quality: "Incomplete",
          dataSource: {
            report: "ESEFReport",
            page: 797,
            tagName: "functionalities",
          },
          comment: "generate online alarm",
          value: "No",
        },
        fairBusinessMarketingAdvertisingPolicy: {
          quality: "Incomplete",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: "No",
        },
        technologiesExpertiseTransferPolicy: null,
        fairCompetitionPolicy: {
          quality: "Audited",
          dataSource: {
            report: "ESEFReport",
            page: 872,
            tagName: "e-business",
          },
          comment: "quantify online bandwidth",
          value: "Yes",
        },
        violationOfTaxRulesAndRegulation: null,
        unGlobalCompactPrinciplesCompliancePolicy: {
          quality: "NA",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: null,
        },
        oecdGuidelinesForMultinationalEnterprisesPolicy: null,
        averageGrossHourlyEarningsMaleEmployees: null,
        averageGrossHourlyEarningsFemaleEmployees: {
          quality: "NA",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: null,
          unit: null,
        },
        femaleBoardMembers: {
          quality: "Reported",
          dataSource: {
            report: "ESEFReport",
            page: 1138,
            tagName: "schemas",
          },
          comment: "connect auxiliary system",
          value: 37796.93,
          unit: null,
        },
        maleBoardMembers: {
          quality: "Audited",
          dataSource: {
            report: "SustainabilityReport",
            page: 404,
            tagName: "e-markets",
          },
          comment: "index mobile bandwidth",
          value: 90102.92,
          unit: null,
        },
        controversialWeaponsExposure: {
          quality: "Estimated",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: "Yes",
        },
        workplaceAccidentPreventionPolicy: {
          quality: "Incomplete",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: "Yes",
        },
        rateOfAccidents: {
          quality: "NA",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: null,
          unit: null,
        },
        workdaysLost: {
          quality: "Incomplete",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: 62580.49,
          unit: null,
        },
        supplierCodeOfConduct: {
          quality: "Incomplete",
          dataSource: {
            report: "ESEFReport",
            page: 955,
            tagName: "ROI",
          },
          comment: "compress wireless protocol",
          value: "No",
        },
        grievanceHandlingMechanism: {
          quality: "Incomplete",
          dataSource: {
            report: "SustainabilityReport",
            page: 966,
            tagName: "action-items",
          },
          comment: "hack neural transmitter",
          value: "No",
        },
        whistleblowerProtectionPolicy: {
          quality: "Reported",
          dataSource: {
            report: "ESEFReport",
            page: 1166,
            tagName: "models",
          },
          comment: "reboot back-end capacitor",
          value: "Yes",
        },
        reportedIncidentsOfDiscrimination: {
          quality: "Estimated",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: 30537.22,
          unit: null,
        },
        sanctionsIncidentsOfDiscrimination: null,
        ceoToEmployeePayGap: null,
      },
      greenSecurities: {
        securitiesNotCertifiedAsGreen: {
          quality: "Incomplete",
          dataSource: {
            report: "",
            page: null,
            tagName: null,
          },
          comment: null,
          value: "Yes",
        },
      },
      humanRights: {
        humanRightsPolicy: null,
        humanRightsDueDiligence: null,
        traffickingInHumanBeingsPolicy: {
          quality: "Audited",
          dataSource: {
            report: "ESEFReport",
            page: 365,
            tagName: "markets",
          },
          comment: "calculate back-end alarm",
          value: "Yes",
        },
        reportedChildLabourIncidents: {
          quality: "Reported",
          dataSource: {
            report: "SustainabilityReport",
            page: 156,
            tagName: "markets",
          },
          comment: "quantify redundant protocol",
          value: "Yes",
        },
        reportedForcedOrCompulsoryLabourIncidents: {
          quality: "Estimated",
          dataSource: {
            report: "SustainabilityReport",
            page: 1156,
            tagName: "supply-chains",
          },
          comment: "navigate haptic panel",
          value: "Yes",
        },
        reportedIncidentsOfHumanRights: null,
      },
      antiCorruptionAndAntiBribery: {
        reportedCasesOfBriberyCorruption: {
          quality: "Reported",
          dataSource: {
            report: "ESEFReport",
            page: 503,
            tagName: "networks",
          },
          comment: "index neural protocol",
          value: 86305.63,
          unit: null,
        },
        reportedConvictionsOfBriberyCorruption: {
          quality: "Reported",
          dataSource: {
            report: "SustainabilityReport",
            page: 639,
            tagName: "convergence",
          },
          comment: "index haptic firewall",
          value: 10110.31,
          unit: null,
        },
        reportedFinesOfBriberyCorruption: {
          quality: "Estimated",
          dataSource: {
            report: "SustainabilityReport",
            page: 1123,
            tagName: "schemas",
          },
          comment: "synthesize cross-platform capacitor",
          value: 9577.85,
          unit: null,
        },
      },
    },
  },
};

const viewConfiguration = convertDataModel(sfdrDataModel);
console.log(viewConfiguration);

export default defineComponent({
  name: "TestPage3",
  components: { MultiLayerDataTable },
  data() {
    return {
      viewConfiguration,
      datasets: [demoDataset],
    };
  },
});
</script>
