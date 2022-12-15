import { DataTypeEnum } from "@clients/backend";

/**
 * Contains global constants
 */

export const ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS = Object.values(DataTypeEnum).filter(
  (frameworkName) => ["lksg", "sfdr", "sme"].indexOf(frameworkName) === -1
) as Array<DataTypeEnum>;

export const EXCEL_TEMPLATE_FILE_NAME = "Dataland_Request_Template.xlsx";

export const UPLOAD_FILE_SIZE_DISPLAY_DECIMALS = 2;

export const UPLOAD_MAX_FILE_SIZE_IN_BYTES = 10000000;
