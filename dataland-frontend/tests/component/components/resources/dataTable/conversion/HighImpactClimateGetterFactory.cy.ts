import {
  highImpactClimateGetterFactory,
  type HighImpactClimateValueObject,
} from "@/components/resources/dataTable/conversion/HighImpactClimateGetterFactory";
import { type Field } from "@/utils/GenericFrameworkTypes";
import { MLDTDisplayComponentName } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { HighImpactClimateSector } from "@/api-models/HighImpactClimateSector";
import { type ExtendedDataPointBigDecimal } from "@clients/backend";

describe("Unit test for the HighImpactClimateGetterFactory", () => {
  const field: Field = {
    name: "applicableHighImpactClimateSectors",
    label: "Applicable High Impact Climate Sectors",
    description: "Total energy consumption per high impact climate sector",
    unit: "",
    component: "HighImpactClimateSectorsFormField",
    required: false,
    showIf: (): boolean => true,
  };

  it("Should display the name of sectors if they exist", () => {
    const highImpactClimateData: HighImpactClimateValueObject = {
      [HighImpactClimateSector.NaceCodeA]: {
        highImpactClimateSectorEnergyConsumptionInGWh: {
          value: 12345,
          quality: "Estimated",
        } as ExtendedDataPointBigDecimal,
        highImpactClimateSectorEnergyConsumptionInGWhPerMillionEURRevenue: {
          value: 54,
          quality: "Estimated",
        } as ExtendedDataPointBigDecimal,
      },
    };

    const dataset = { data: highImpactClimateData };
    const value = highImpactClimateGetterFactory("data", field)(dataset);

    expect(value).to.have.property("displayComponentName", MLDTDisplayComponentName.ModalLinkDisplayComponent);
    expect(value).to.have.nested.property("displayValue.label", "Applicable High Impact Climate Sectors");
    expect(value).to.have.deep.nested.property("displayValue.modalOptions.data.listOfRowContents", [
      {
        sector: "AGRICULTURE, FORESTRY AND FISHING",
        energyConsumption: { value: "12345 GWh", dataSource: undefined, quality: "Estimated", comment: undefined },
        relativeEnergyConsumption: {
          value: "12345 GWh / €M revenue",
          dataSource: undefined,
          quality: "Estimated",
          comment: undefined,
        },
      },
    ]);
  });
});
