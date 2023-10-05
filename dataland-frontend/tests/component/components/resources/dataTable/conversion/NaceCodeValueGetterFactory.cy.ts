import {
  EmptyDisplayValue,
  MLDTDisplayComponents,
  type MLDTDisplayValue,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { naceCodeValueGetterFactory } from "@/components/resources/dataTable/conversion/NaceCodeValueGetterFactory";
import { type Field } from "@/utils/GenericFrameworkTypes";
import MultiSelectModal from "@/components/resources/dataTable/modals/MultiSelectModal.vue";

describe("Unit test for the MultiSelectValueGetterFactory", () => {
  const sampleMultiSelectFormField: Field = {
    name: "NaceCodeFormField-Test",
    label: "NaceCodeFormField-Test",
    component: "NaceCodeFormField",
    showIf: () => true,
    description: "Test-Field",
  };

  it("An empty string should be displayed if the data point is undefined", () => {
    const dataset = { data: undefined };
    const value = naceCodeValueGetterFactory("data", sampleMultiSelectFormField)(dataset);
    expect(value).to.deep.equal(EmptyDisplayValue);
  });

  it("An empty string should be displayed if the data point has no values", () => {
    const dataset = { data: [] };
    const value = naceCodeValueGetterFactory("data", sampleMultiSelectFormField)(dataset);
    expect(value).to.deep.equal(EmptyDisplayValue);
  });

  it("A Link to a MultiSelectModal should be displayed if there is one value to display", () => {
    const dataset = { data: ["A"] };
    const value = naceCodeValueGetterFactory("data", sampleMultiSelectFormField)(dataset);
    expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.ModalLinkDisplayComponent>>{
      displayComponent: MLDTDisplayComponents.ModalLinkDisplayComponent,
      displayValue: {
        label: "Show 1 NACE code",
        modalComponent: MultiSelectModal,
        modalOptions: {
          props: {
            header: "NaceCodeFormField-Test",
            modal: true,
            dismissableMask: true,
          },
          data: {
            label: "NaceCodeFormField-Test",
            values: ["A - AGRICULTURE, FORESTRY AND FISHING"],
          },
        },
      },
    });
  });

  it("A Link to a MultiSelectModal should be displayed with a plural s if there is more than 1 value", () => {
    const dataset = { data: ["A", "B"] };
    const value = naceCodeValueGetterFactory("data", sampleMultiSelectFormField)(dataset);
    expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.ModalLinkDisplayComponent>>{
      displayComponent: MLDTDisplayComponents.ModalLinkDisplayComponent,
      displayValue: {
        label: "Show 2 NACE codes",
        modalComponent: MultiSelectModal,
        modalOptions: {
          props: {
            header: "Test-Field",
            modal: true,
            dismissableMask: true,
          },
          data: {
            label: "Test-Field",
            values: ["A - AGRICULTURE, FORESTRY AND FISHING", "B - MINING AND QUARRYING"],
          },
        },
      },
    });
  });
});
