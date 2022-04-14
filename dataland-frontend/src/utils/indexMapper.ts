/**
 * Map the backend indices to frontend indices
 * adopted from https://riptutorial.com/javascript/example/8628/merge-two-array-as-key-value-pair
 *
 */
import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";

export function stockIndexObject() {
    const frontend = [
        "CDAX",
        "DAX",
        "General Standards",
        "GEX",
        "MDAX",
        "Prime Standards",
        "SDAX",
        "TecDAX",
        "ScaleHDAX",
        "DAX 50 ESG"
    ];
    const backend = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum
    return frontend.reduce(function (result: any, field, index) {
        result[backend[index]] = field;
        return result;
    }, {})
}
