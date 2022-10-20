import { SchemaGenerator } from "@/services/SchemaGenerator";

describe("Component test for SchemaGenerator", () => {
  it("checks if the schema can be generated automatically", () => {
    const testSchema = {
      required: ["listProp", "yesNoProp"],
      properties: {
        textProp: {
          type: "string",
        },
        numberProp: {
          type: "number",
        },
        dateProp: {
          type: "string",
          format: "date",
        },
        yesNoProp: {
          type: "string",
          enum: ["Yes", "No"],
        },
        listProp: {
          type: "string",
          enum: ["None", "Some", "Full"],
        },
        itemProp: {
          type: "string",
          item: {
            enum: ["None", "Some", "Full"],
          },
        },
      },
    };
    const dataStore = new SchemaGenerator(testSchema);
    expect(Object.keys(dataStore.generate()).length).to.equal(Object.keys(testSchema.properties).length);
  });
});
