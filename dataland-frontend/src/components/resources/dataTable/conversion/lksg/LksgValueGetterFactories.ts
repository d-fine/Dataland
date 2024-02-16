import {
    AvailableMLDTDisplayObjectTypes, MLDTDisplayComponentName, MLDTDisplayObject,
    MLDTDisplayObjectForEmptyString
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import {LksgProduct} from "@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model/lksg-product";

export const lksgModalColumnHeaders = {
    listOfProductionSites: {
        nameOfProductionSite: "Name",
        addressOfProductionSite: "Address",
        listOfGoodsOrServices: "List of Goods or Services",
    },
    mostImportantProducts: {
        name: "Product Name",
        productionSteps: "Production Steps",
        relatedCorporateSupplyChain: "Related Corporate Supply Chain",
    },
    procurementCategories: {
        procurementCategory: "Procurement Category",
        procuredProductTypesAndServicesNaceCodes: "Procured Products/Services",
        suppliersAndCountries: "Number of Direct Suppliers and Countries",
        totalProcurementInPercent: "Order Volume",
    },
};


/**
 * Generate a ModalLinkDisplayComponent that displays the most important products for Lksg
 * @returns a ModalLinkDisplayComponent to the modal (if any data is present).
 * @param input List of Lksg Products
 * @param fieldLabel Field Label for the corresponding object
 */
export function formatLksgMostImportantProductsForDisplay(
    input: LksgProduct[] | null | undefined,
    fieldLabel: string,
    ): AvailableMLDTDisplayObjectTypes {
    if (!input) {
        return MLDTDisplayObjectForEmptyString;
    }

    return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
        displayValue: {
            label: `Show ${fieldLabel}`,
            modalComponent: DetailsCompanyDataTable,
            modalOptions: {
                props: {
                    header: fieldLabel,
                    modal: true,
                    dismissableMask: true,
                },
                data: {
                    listOfRowContents: input,
                    kpiKeyOfTable: "mostImportantProducts",
                    columnHeaders: lksgModalColumnHeaders,
                },
            },
        },
    };
}