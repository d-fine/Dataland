import { type LksgProcurementCategory } from "@clients/backend";
import { type ProcurementCategoryType } from "@/api-models/ProcurementCategoryType";

export type LksgProcurementType = { [key in ProcurementCategoryType]?: LksgProcurementCategory };
