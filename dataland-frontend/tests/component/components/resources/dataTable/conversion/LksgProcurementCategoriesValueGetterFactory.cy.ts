import {
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  lksgProcurementCategoriesValueGetterFactory,
  type LksgProcurementType,
} from "@/components/resources/dataTable/conversion/lksg/LksgProcurementCategoriesValueGetterFactory";
import { ProcurementCategoryType } from "@/api-models/ProcurementCategoryType";

describe("Unit test for the LKSG ProcurementCategoriesValueGetterFactory", () => {
  const sampleField: Field = {
    name: "productsServicesCategoriesPurchased",
    label: "Products/Services Categories purchased",
    description: "Name their procurement categories (products, raw materials, services) (own operations)",
    unit: "",
    component: "ProcurementCategoriesFormField",
    evidenceDesired: false,
    required: false,
    showIf: () => true,
  };

  it("An empty string should be displayed if the data point is undefined", () => {
    const dataset = { data: undefined };
    const value = lksgProcurementCategoriesValueGetterFactory("data", sampleField)(dataset);
    expect(value).to.deep.equal(MLDTDisplayObjectForEmptyString);
  });

  it("Should display the name of the procurement category in a human-friendly name", () => {
    const procurementData: LksgProcurementType = {
      [ProcurementCategoryType.RawMaterials]: {
        procuredProductTypesAndServicesNaceCodes: [],
        numberOfSuppliersPerCountryCode: {},
        shareOfTotalProcurementInPercent: null,
      },
    };
    const dataset = { data: procurementData };
    const value = lksgProcurementCategoriesValueGetterFactory("data", sampleField)(dataset);

    expect(value).to.have.property("displayComponentName", MLDTDisplayComponentName.ModalLinkDisplayComponent);
    expect(value).to.have.nested.property("displayValue.label", "Show Products/Services Categories purchased");
    expect(value).to.have.deep.nested.property("displayValue.modalOptions.data.listOfRowContents", [
      {
        procurementCategory: "Raw Materials",
        procuredProductTypesAndServicesNaceCodes: [],
        suppliersAndCountries: [],
        totalProcurementInPercent: "",
      },
    ]);
  });

  it("Should display NACE codes correctly if specified", () => {
    const procurementData: LksgProcurementType = {
      [ProcurementCategoryType.RawMaterials]: {
        procuredProductTypesAndServicesNaceCodes: ["A"],
        numberOfSuppliersPerCountryCode: {},
        shareOfTotalProcurementInPercent: null,
      },
    };
    const dataset = { data: procurementData };
    const value = lksgProcurementCategoriesValueGetterFactory("data", sampleField)(dataset);

    expect(value).to.have.deep.nested.property("displayValue.modalOptions.data.listOfRowContents", [
      {
        procurementCategory: "Raw Materials",
        procuredProductTypesAndServicesNaceCodes: ["A - AGRICULTURE, FORESTRY AND FISHING"],
        suppliersAndCountries: [],
        totalProcurementInPercent: "",
      },
    ]);
  });

  it("Should display the number of production sites in the country correctly", () => {
    const procurementData: LksgProcurementType = {
      [ProcurementCategoryType.RawMaterials]: {
        procuredProductTypesAndServicesNaceCodes: [],
        numberOfSuppliersPerCountryCode: {
          DE: 3,
        },
        shareOfTotalProcurementInPercent: null,
      },
    };
    const dataset = { data: procurementData };
    const value = lksgProcurementCategoriesValueGetterFactory("data", sampleField)(dataset);

    expect(value).to.have.deep.nested.property(
      "displayValue.modalOptions.data.listOfRowContents[0].suppliersAndCountries",
      ["3 suppliers from Germany"],
    );
  });

  it("Should display that there production sites in a country when the exact number of sites is not specified", () => {
    const procurementData = {
      [ProcurementCategoryType.RawMaterials]: {
        procuredProductTypesAndServicesNaceCodes: [],
        numberOfSuppliersPerCountryCode: {
          DE: null,
        },
        shareOfTotalProcurementInPercent: null,
      },
    };
    const dataset = { data: procurementData };
    const value = lksgProcurementCategoriesValueGetterFactory("data", sampleField)(dataset);

    expect(value).to.have.deep.nested.property(
      "displayValue.modalOptions.data.listOfRowContents[0].suppliersAndCountries",
      ["There are suppliers from Germany"],
    );
  });

  it("Should display the percentage field correctly", () => {
    const procurementData = {
      [ProcurementCategoryType.RawMaterials]: {
        procuredProductTypesAndServicesNaceCodes: [],
        numberOfSuppliersPerCountryCode: {},
        shareOfTotalProcurementInPercent: 10.554,
      },
    };
    const dataset = { data: procurementData };
    const value = lksgProcurementCategoriesValueGetterFactory("data", sampleField)(dataset);

    expect(value).to.have.nested.property(
      "displayValue.modalOptions.data.listOfRowContents[0].totalProcurementInPercent",
      "10.55 %",
    );
  });
});
