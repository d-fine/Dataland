import { Category } from "@/utils/GenericFrameworkTypes";


export const sfdrDataModel = [{
      name: "environmental",
    label: "Environmental",
    color: "green",
    subcategories: [{
      name: "energyPerformance",
      label: "Energy performance",
      fields: [{
        name: "highImpactClimateSectorEnergyConsumptionNaceH",
        label: "High Impact Climate Sector Energy Consumption NACE H",
        description: "Total energy consumption per high impact climate sector",
        component: "DataPointFormField",
        required: true,
        showIf: (): boolean => true,
        validation: "required"
      }]
    }]

}] as Array<Category>;
