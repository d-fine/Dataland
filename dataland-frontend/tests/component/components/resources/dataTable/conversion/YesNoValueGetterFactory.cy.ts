import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  EmptyDisplayValue,
  MLDTDisplayComponents,
  type MLDTDisplayValue,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { type BaseDataPointYesNoNa, type DataPointOneValueYesNo, QualityOptions, YesNoNa } from "@clients/backend";
import { yesNoValueGetterFactory } from "@/components/resources/dataTable/conversion/YesNoValueGetterFactory";

describe("Unit test for the YesNoValueGetterFactory", () => {
  describe("Tests for the simple field", () => {
    const field: Field = {
      name: "environmentalManagementSystemNationalCertification",
      label: "Environmental Management System National Certification",
      description: "Is the environmental management system nationally recognized and certified?",
      unit: "",
      component: "YesNoNaFormField",
      evidenceDesired: false,
      required: false,
      showIf: (): boolean => true,
      certificateRequiredIfYes: false,
    };

    it("An empty string should be displayed if the data point is undefined", () => {
      const dataset = { data: undefined };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(EmptyDisplayValue);
    });

    it("'Yes' should be displayed if the value is Yes", () => {
      const dataset = { data: YesNoNa.Yes };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>>{
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "Yes",
      });
    });

    it("'No' should be displayed if the value is No", () => {
      const dataset = { data: YesNoNa.No };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>>{
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "No",
      });
    });

    it("'N/A' should be displayed if the value is NA", () => {
      const dataset = { data: YesNoNa.Na };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>>{
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "N/A",
      });
    });
  });

  describe("Tests when the field has the certificateRequiredIfYes property set", () => {
    const field: Field = {
      name: "environmentalManagementSystemNationalCertification",
      label: "Environmental Management System National Certification",
      description: "Is the environmental management system nationally recognized and certified?",
      unit: "",
      component: "YesNoNaFormField",
      evidenceDesired: false,
      required: false,
      showIf: (): boolean => true,
      certificateRequiredIfYes: true,
    };

    it("An empty string should be displayed if the data point is undefined", () => {
      const dataset = { data: undefined };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(EmptyDisplayValue);
    });

    it("'Yes' should be displayed if no certificate is provided", () => {
      const datapoint: BaseDataPointYesNoNa = {
        value: YesNoNa.Yes,
      };
      const dataset = { data: datapoint };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>>{
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "Yes",
      });
    });

    it("'N/A' should be displayed if no certificate is provided", () => {
      const datapoint: BaseDataPointYesNoNa = {
        value: YesNoNa.Na,
      };
      const dataset = { data: datapoint };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>>{
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "N/A",
      });
    });

    it("'Uncertified' should be displayed if no certificate is provided and the value is No", () => {
      const datapoint: BaseDataPointYesNoNa = {
        value: YesNoNa.No,
      };
      const dataset = { data: datapoint };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>>{
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "Uncertified",
      });
    });

    it("'Certified' with a link to the document should be displayed if a certificate is provided and the value is Yes", () => {
      const datapoint: BaseDataPointYesNoNa = {
        value: YesNoNa.Yes,
        dataSource: {
          name: "Hello",
          reference: "TestReference",
        },
      };
      const dataset = { data: datapoint };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.DocumentLinkDisplayComponent>>{
        displayComponent: MLDTDisplayComponents.DocumentLinkDisplayComponent,
        displayValue: {
          label: "Certified",
          reference: {
            name: "Hello",
            reference: "TestReference",
          },
        },
      });
    });
  });

  describe("Tests when the field has the evidenceDesired property set", () => {
    const field: Field = {
      name: "environmentalManagementSystemNationalCertification",
      label: "Environmental Management System National Certification",
      description: "Is the environmental management system nationally recognized and certified?",
      unit: "",
      component: "YesNoNaFormField",
      evidenceDesired: true,
      required: false,
      showIf: (): boolean => true,
      certificateRequiredIfYes: false,
    };

    it("An empty string should be displayed if the data point is undefined", () => {
      const dataset = { data: undefined };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(EmptyDisplayValue);
    });

    it("An empty string should be displayed if the data point's value is undefined", () => {
      const datapoint: DataPointOneValueYesNo = {
        quality: QualityOptions.Na,
      };
      const dataset = { data: datapoint };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(EmptyDisplayValue);
    });

    it("'Yes' should be displayed if the value is Yes", () => {
      const datapoint: DataPointOneValueYesNo = {
        value: YesNoNa.Yes,
        quality: QualityOptions.Na,
      };
      const dataset = { data: datapoint };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>>{
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "Yes",
      });
    });

    it("'No' should be displayed if the value is No", () => {
      const datapoint: DataPointOneValueYesNo = {
        value: YesNoNa.No,
        quality: QualityOptions.Na,
      };
      const dataset = { data: datapoint };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>>{
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "No",
      });
    });

    it("'N/A' should be displayed if the value is NA", () => {
      const datapoint = {
        value: YesNoNa.Na,
        quality: QualityOptions.Na,
      };
      const dataset = { data: datapoint };
      const value = yesNoValueGetterFactory("data", field)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>>{
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "N/A",
      });
    });
  });
});
