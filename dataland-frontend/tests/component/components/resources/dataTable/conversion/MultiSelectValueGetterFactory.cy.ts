import {
  EmptyDisplayValue,
  MLDTDisplayComponents,
  type MLDTDisplayValue,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { multiSelectValueGetterFactory } from "@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory";
import { type Field } from "@/utils/GenericFrameworkTypes";
import MultiSelectModal from "@/components/resources/dataTable/modals/MultiSelectModal.vue";

describe("Unit test for the MultiSelectValueGetterFactory", () => {
  const sampleMultiSelectFormField: Field = {
    name: "MultiSelectFormField-Test",
    label: "MultiSelectFormField-Test",
    component: "MultiSelectFormField",
    showIf: () => true,
    description: "Test-Field",
    options: [
      {
        label: "Option A Label",
        value: "A",
      },
      {
        label: "Option B Label",
        value: "B",
      },
    ],
  };

  it("An empty string should be displayed if the data point is undefined", () => {
    const dataset = { data: undefined };
    const value = multiSelectValueGetterFactory("data", sampleMultiSelectFormField)(dataset);
    expect(value).to.deep.equal(EmptyDisplayValue);
  });

  it("An empty string should be displayed if the data point has no values", () => {
    const dataset = { data: [] };
    const value = multiSelectValueGetterFactory("data", sampleMultiSelectFormField)(dataset);
    expect(value).to.deep.equal(EmptyDisplayValue);
  });

  it("A Link to a MultiSelectModal should be displayed if there is one value to display", () => {
    const dataset = { data: ["A"] };
    const value = multiSelectValueGetterFactory("data", sampleMultiSelectFormField)(dataset);
    expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.ModalLinkDisplayComponent>>{
      displayComponent: MLDTDisplayComponents.ModalLinkDisplayComponent,
      displayValue: {
        label: "Show 1 value",
        modalComponent: MultiSelectModal,
        modalOptions: {
          props: {
            header: "MultiSelectFormField-Test",
            modal: true,
            dismissableMask: true,
          },
          data: {
            label: "MultiSelectFormField-Test",
            values: ["Option A Label"],
          },
        },
      },
    });
  });

  it("A Link to a MultiSelectModal should be displayed with a plural s if there is more than 1 value", () => {
    const dataset = { data: ["A", "B"] };
    const value = multiSelectValueGetterFactory("data", sampleMultiSelectFormField)(dataset);
    expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.ModalLinkDisplayComponent>>{
      displayComponent: MLDTDisplayComponents.ModalLinkDisplayComponent,
      displayValue: {
        label: "Show 2 values",
        modalComponent: MultiSelectModal,
        modalOptions: {
          props: {
            header: "MultiSelectFormField-Test",
            modal: true,
            dismissableMask: true,
          },
          data: {
            label: "MultiSelectFormField-Test",
            values: ["Option A Label", "Option B Label"],
          },
        },
      },
    });
  });
});
