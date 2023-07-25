import { SmeData } from "@clients/backend";
import { Category } from "@/utils/GenericFrameworkTypes";

export const smeDataModel = [
  {
    name: "general",
    label: "General",
    color: "orange",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "basicInformation",
        label: "Basic Information",
        fields: [
          {
            name: "sector",
            label: "Sector",
            description:
              "Please select the industry sector in which your company was mainly active in the relevant fiscal year.",
            component: "NaceCodeFormField",
            required: true,
            showIf: (): boolean => true,
            validation: "required|length:1",
          },
          {
            name: "addressOfHeadquarters",
            label: "Address Of Headquarters",
            description: "Please provide the full address of your company's headquarters in the relevant fiscal year.",
            component: "AddressFormField",
            required: true,
            showIf: (): boolean => true,
            validation: "required",
          },
          {
            name: "numberOfEmployees",
            label: "Number of Employees",
            description: "Please provide the number of workforce employed by your company in the relevant fiscal year.",
            component: "NumberFormField",
            required: true,
            showIf: (): boolean => true,
            validation: "required",
          },
          {
            name: "fiscalYearStart",
            label: "Fiscal Year Start",
            description: "Please provide the starting date of the company's fiscal year to which you refer.",
            component: "DateFormField",
            required: true,
            showIf: (): boolean => true,
            validation: "required",
          },
        ],
      },
      {
        name: "businessNumbers",
        label: "Business Numbers",
        fields: [
          {
            name: "revenueInEur",
            label: "Revenue in EUR",
            description: "Please provide your company's revenue in the relevant year in Euro.",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "operatingCostInEur",
            label: "Operating Cost in EUR",
            description: "Please provide your company's operating cost in the relevant fiscal year in Euro.",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "capitalAssetsInEur",
            label: "Capital assets in EUR",
            description:
              "Please provide the value of your company's capital assets in the relevant fiscal year in Euro.",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
    ],
  },
  {
    name: "production",
    label: "Production",
    color: "orange",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "sites",
        label: "Sites",
        fields: [
          {
            name: "listOfProductionSites",
            label: "List of Production Sites",
            description:
              "Please provide the percentage of revenue generated at the provided main branch/production site or your headquarters relative to your company's total revenue in the relevant fiscal year.",
            component: "ProductionSitesFormFieldSme",
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
      {
        name: "products",
        label: "Products",
        fields: [
          {
            name: "listOfProducts",
            label: "List of Products",
            description:
              "Please provide the HS-Code of the product category generating the highest revenue in your company in the relevant fiscal year. Please use the provided link to identify the product category code.",
            component: "ProductsFormFieldSme",
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
    ],
  },
  {
    name: "power",
    label: "Power",
    color: "yellow",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "investments",
        label: "Investments",
        fields: [
          {
            name: "percentageOfInvestmentsInEnhancingEnergyEfficiency",
            label: "Percentage of investments in enhancing energy efficiency",
            description:
              "Please provide the fraction of your company's total investments that was primarily spent to enhance energy efficiency in the last fiscal year.",
            component: "RadioButtonsFormField",
            options: [
              {
                label: "< 1%",
                value: "< 1%",
              },
              {
                label: "1-5%",
                value: "1-5%",
              },
              {
                label: "5-10%",
                value: "5-10%",
              },
              {
                label: "10-15%",
                value: "10-15%",
              },
              {
                label: "15-20%",
                value: "15-20%",
              },
              {
                label: "20-25%",
                value: "20-25%",
              },
              {
                label: "> 25%",
                value: "> 25%", // TODO the actual values are wrong
              },
            ],
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
      {
        name: "consumption",
        label: "Consumption",
        fields: [
          {
            name: "powerConsumptionInMwh",
            label: "Power consumption in MWh",
            description: "Please provide your company's power consumption in the relevant fiscal year in MWh.",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "powerFromRenewableSources",
            label: "power from renewable sources",
            description:
              "Please provide information whether your company has been primarily using power from renewable sources in the relevant fiscal year.",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "energyConsumptionHeatingAndHotWater",
            label: "Energy consumption heating and hot water",
            description:
              "Please provide your company's power consumption for heating and hot water generation in the relevant fiscal year in MWh.",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "primaryEnergySourceForHeatingAndHotWater",
            label: "Primary energy source for heating and hot water",
            description:
              "Please provide the energy source primarily used by your company for heating/hot water generation in the relevant fiscal year.",
            component: "RadioButtonsFormField",
            options: [
              {
                label: "Oil",
                value: "oil",
              },
              {
                label: "Gas",
                value: "gas",
              },
              {
                label: "Electric",
                value: "electric",
              },
              {
                label: "District Heating",
                value: "district heating",
              },
            ],
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "energyConsumptionCoveredByOwnRenewablePowerGeneration",
            label: "Energy consumption covered by own renewable power generation",
            description:
              "Please provide the portion of consumed power generated by your own renewable sources relative to your company's total power consumption in the relevant fiscal year.",
            component: "RadioButtonsFormField",
            options: [
              {
                label: "< 25%",
                value: "< 25%", // TODO adapt DD table
              },
              {
                label: "25-50%",
                value: "25-50%",
              },
              {
                label: "50-75%",
                value: "50-75%",
              },
              {
                label: "> 75%",
                value: "> 75%",
              },
            ],
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
    ],
  },
  {
    name: "insurances",
    label: "Insurances",
    color: "blue",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "naturalHazards",
        label: "Natural Hazards",
        fields: [
          {
            name: "insuranceAgainstNaturalHazards",
            label: "Insurance against natural hazards",
            description:
              "Please provide information whether your company has insurance against natural hazards at its branch/production site generating most revenue, or its headquarters.",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "amountCoveredByInsuranceAgainstNaturalHazards",
            label: "Amount covered by insurance against natural hazards",
            description:
              "Please provide the amount covered by the insurance in EUR. In case your company has different policies for different natural hazards please provide the average amount covered.",
            component: "NumberFormField",
            required: false,
            showIf: (dataModel: SmeData): boolean =>
              dataModel?.insurances?.naturalHazards?.insuranceAgainstNaturalHazards === "Yes",
          },
          {
            name: "naturalHazardsCovered",
            label: "Natural Hazards covered",
            description: "Please identify all natural hazards covered by your insurance.",
            component: "MultiSelectFormField",
            options: [
              {
                label: "Hail",
                value: "hail",
              },
              {
                label: "Wind",
                value: "wind",
              },
              {
                label: "Flooding",
                value: "flooding",
              },
              {
                label: "Earth Quakes",
                value: "earth quakes",
              },
              {
                label: "Avalanches",
                value: "avalanches",
              },
              {
                label: "Snow",
                value: "snow",
              },
            ],
            required: false,
            showIf: (dataModel: SmeData): boolean =>
              dataModel?.insurances?.naturalHazards?.insuranceAgainstNaturalHazards === "Yes",
            placeholder: "Select Options",
          },
        ],
      },
    ],
  },
] as Array<Category>;
