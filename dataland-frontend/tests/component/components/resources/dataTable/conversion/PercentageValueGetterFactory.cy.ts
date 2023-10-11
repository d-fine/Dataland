import {
  EmptyDisplayValue,
  MLDTDisplayComponentName,
  type MLDTDisplayValue,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { percentageValueGetterFactory } from "@/components/resources/dataTable/conversion/PercentageValueGetterFactory";

describe("Unit test for the PercentageValueGetterFactory", () => {
  it("An empty string should be displayed if the data point is undefined", () => {
    const dataset = { data: undefined };
    const value = percentageValueGetterFactory("data")(dataset);
    expect(value).to.deep.equal(EmptyDisplayValue);
  });

  it("The value of the input should be displayed with a percent sign if it exists", () => {
    const dataset = { data: 10 };
    const value = percentageValueGetterFactory("data")(dataset);
    expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponentName.StringDisplayComponent>>{
      displayComponent: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: "10 %",
    });
  });

  it("The value of the input should be displayed with a percent sign rounded to two decimal places", () => {
    const dataset = { data: 10.223 };
    const value = percentageValueGetterFactory("data")(dataset);
    expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponentName.StringDisplayComponent>>{
      displayComponent: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: "10.22 %",
    });
  });
});
