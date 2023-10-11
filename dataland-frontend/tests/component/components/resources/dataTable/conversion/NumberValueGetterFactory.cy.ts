import {
  EmptyDisplayValue,
  MLDTDisplayComponentName,
  type MLDTDisplayValue,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { numberValueGetterFactory } from "@/components/resources/dataTable/conversion/NumberValueGetterFactory";

describe("Unit test for the NumberValueGetterFactory", () => {
  it("An empty string should be displayed if the data point is undefined", () => {
    const dataset = { data: undefined };
    const value = numberValueGetterFactory("data")(dataset);
    expect(value).to.deep.equal(EmptyDisplayValue);
  });

  it("The value of the input should be displayed if it exists", () => {
    const dataset = { data: 10 };
    const value = numberValueGetterFactory("data")(dataset);
    expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponentName.StringDisplayComponent>>{
      displayComponent: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: "10",
    });
  });

  it("The value of the input should be displayed with a percent sign rounded to two decimal places", () => {
    const dataset = { data: 10.223 };
    const value = numberValueGetterFactory("data")(dataset);
    expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponentName.StringDisplayComponent>>{
      displayComponent: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: "10.22",
    });
  });

  it("The value of the input should contain thousands separators", () => {
    const dataset = { data: 1023 };
    const value = numberValueGetterFactory("data")(dataset);
    expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponentName.StringDisplayComponent>>{
      displayComponent: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: "1,023",
    });
  });
});
