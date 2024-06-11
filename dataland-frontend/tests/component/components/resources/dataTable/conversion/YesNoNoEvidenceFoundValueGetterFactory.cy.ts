import {
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { YesNoNoEvidenceFound } from "@clients/backend";
import { yesNoNoEvidenceFoundValueGetterFactory } from "@/components/resources/dataTable/conversion/YesNoNoEvidenceFoundValueGetterFactory";

describe("Unit test for the YesNoNoEvidenceFoundValueGetterFactory", () => {
  describe("Tests for the simple field", () => {
    it("An empty string should be displayed if the data point is undefined", () => {
      const dataset = { data: undefined };
      const value = yesNoNoEvidenceFoundValueGetterFactory("data")(dataset);
      expect(value).to.deep.equal(MLDTDisplayObjectForEmptyString);
    });

    it("'Yes' should be displayed if the value is Yes", () => {
      const dataset = { data: YesNoNoEvidenceFound.Yes };
      const value = yesNoNoEvidenceFoundValueGetterFactory("data")(dataset);
      expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: "Yes",
      });
    });

    it("'No' should be displayed if the value is No", () => {
      const dataset = { data: YesNoNoEvidenceFound.No };
      const value = yesNoNoEvidenceFoundValueGetterFactory("data")(dataset);
      expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: "No",
      });
    });

    it("'No evidence found' should be displayed if the value is NA", () => {
      const dataset = { data: YesNoNoEvidenceFound.NoEvidenceFound };
      const value = yesNoNoEvidenceFoundValueGetterFactory("data")(dataset);
      expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: "No evidence found",
      });
    });
  });
});
