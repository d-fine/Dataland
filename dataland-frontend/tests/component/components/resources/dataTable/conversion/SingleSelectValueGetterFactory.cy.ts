import {
  EmptyDisplayValue,
  MLDTDisplayComponents,
  type MLDTDisplayValue,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { type Field } from "@/utils/GenericFrameworkTypes";
import { singleSelectValueGetterFactory } from "@/components/resources/dataTable/conversion/SingleSelectValueGetterFactory";
describe("Unit test for the SingleSelectValueGetterFactory", () => {
  const field: Field = {
    name: "fiscalYearDeviation",
    label: "Fiscal Year Deviation",
    description: "Does the fiscal year deviate from the calender year?",
    unit: "",
    component: "RadioButtonsFormField",
    evidenceDesired: false,
    options: [
      {
        label: "Deviation",
        value: "Deviation",
      },
      {
        label: "No Deviation",
        value: "NoDeviation",
      },
    ],
    required: true,
    showIf: (): boolean => true,
    validation: "required",
  };

  it("An empty string should be displayed if the data point is undefined", () => {
    const dataset = { data: undefined };
    const value = singleSelectValueGetterFactory("data", field)(dataset);
    expect(value).to.deep.equal(EmptyDisplayValue);
  });

  it("The human-readable name of the field should be displayed otherwise", () => {
    const dataset = { data: "NoDeviation" };
    const value = singleSelectValueGetterFactory("data", field)(dataset);
    expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>>{
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: "No Deviation",
    });
  });
  it("The raw value of the input should be displayed as a string if the option is unknown", () => {
    const dataset = { data: "Hello there" };
    const value = singleSelectValueGetterFactory("data", field)(dataset);
    expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>>{
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: "Hello there",
    });
  });
});
