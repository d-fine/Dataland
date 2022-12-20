import { humanizeString } from "@/utils/StringHumanizer";

export interface SchemaInterface {
  required: string[];
  properties: { [key: string]: { format?: string; type: string; enum?: string[]; items?: { enum: string[] } } };
}

interface ProcessedSchemaInterface {
  $formkit: string;
  label: string;
  placeholder?: string;
  name: string;
  validation: string;
  options?: Record<string, unknown>;
  classes?: { inner: object; outer?: object; input: object };
}

export class SchemaGenerator {
  private readonly rawSchema: SchemaInterface;
  private readonly hiddenIndices: Array<string>;

  constructor(rawSchema: SchemaInterface, hiddenIndices: Array<string> = []) {
    this.rawSchema = rawSchema;
    this.hiddenIndices = hiddenIndices;
  }

  private getType(param: string): string {
    const propertiesSchema = this.rawSchema.properties;
    if ("format" in propertiesSchema[param]) {
      const format = propertiesSchema[param].format;
      if (format == "date") {
        return "date_format:DD.MM.YYYY|date_format:DD/MM/YYYY";
      }
    }
    if ("type" in propertiesSchema[param]) {
      return propertiesSchema[param].type;
    }
    return "";
  }

  private getEnumProperties(rawEnumProperties: string[] | undefined): Record<string, unknown> {
    const enumProperties: Record<string, unknown> = {};
    if (rawEnumProperties !== undefined) {
      for (const enumItem of rawEnumProperties) {
        enumProperties[enumItem] = humanizeString(enumItem);
      }
    }
    return enumProperties;
  }

  private getValidationStatus(param: string): string {
    const required = this.rawSchema.required.includes(param) ? "required" : "";
    const type = this.getType(param);
    return `${required}|${type}`;
  }

  generate(): object {
    const propertiesSchema = this.rawSchema.properties;
    const processedSchema = [] as Array<ProcessedSchemaInterface>;
    for (const index in propertiesSchema) {
      if (this.hiddenIndices.indexOf(index) >= 0) continue;
      const validation = this.getValidationStatus(index);
      if ("enum" in propertiesSchema[index]) {
        this.processEnum(propertiesSchema, index, processedSchema, validation);
      } else if (this.getType(index).includes("date")) {
        /* create a date form */
        this.processDate(processedSchema, index, validation);
      } else if (this.getType(index) == "array") {
        /* create a checkbox form */
        this.processArray(propertiesSchema, index, processedSchema, validation);
      } else {
        this.processText(processedSchema, index, validation);
      }
    }
    return processedSchema;
  }

  private processEnum(
    propertiesSchema: { [p: string]: { format?: string; type: string; enum?: string[]; items?: { enum: string[] } } },
    index: string,
    processedSchema: Array<ProcessedSchemaInterface>,
    validation: string
  ): void {
    const enumProperties = this.getEnumProperties(propertiesSchema[index].enum);
    if (Object.keys(enumProperties).length > 2) {
      /* create a select form */
      processedSchema.push({
        $formkit: "select",
        label: humanizeString(index),
        placeholder: "Please Choose",
        name: index,
        validation: validation,
        options: enumProperties,
      });
    } else {
      /* create a radio form */
      processedSchema.push({
        $formkit: "radio",
        label: humanizeString(index),
        name: index,
        validation: validation,
        classes: {
          outer: { "formkit-outer": false },
          inner: { "formkit-inner": false },
          input: { "formkit-input": false, "p-radiobutton:": true },
        },
        options: enumProperties,
      });
    }
  }

  private processText(processedSchema: Array<ProcessedSchemaInterface>, index: string, validation: string): void {
    /* create a text form */
    processedSchema.push({
      $formkit: "text",
      label: humanizeString(index),
      placeholder: humanizeString(index),
      name: index,
      validation: validation,
      classes: {
        inner: { "formkit-inner": false, "p-inputwrapper": true },
        input: { "formkit-input": false, "p-inputtext": true },
      },
    });
  }

  private processArray(
    propertiesSchema: { [p: string]: { format?: string; type: string; enum?: string[]; items?: { enum: string[] } } },
    index: string,
    processedSchema: Array<ProcessedSchemaInterface>,
    validation: string
  ): void {
    const items = propertiesSchema[index]?.items;
    if (items !== undefined && "enum" in items) {
      const enumProperties = this.getEnumProperties(items.enum);
      processedSchema.push({
        $formkit: "checkbox",
        label: humanizeString(index),
        placeholder: humanizeString(index),
        name: index,
        validation: validation,
        options: enumProperties,
        classes: {
          outer: { "formkit-outer": false },
          inner: { "formkit-inner": false },
          input: { "formkit-input": false },
        },
      });
    }
  }

  private processDate(processedSchema: Array<ProcessedSchemaInterface>, index: string, validation: string): void {
    processedSchema.push({
      $formkit: "date",
      label: humanizeString(index),
      name: index,
      validation: validation,
      classes: {
        inner: { "formkit-inner": false, "p-inputwrapper": true },
        input: { "formkit-input": false, "p-inputtext": true },
      },
    });
  }
}
