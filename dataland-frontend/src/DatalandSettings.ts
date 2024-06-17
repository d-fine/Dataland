/**
 * Contains global constants that are sourced from envs to be accessible in the frontend code
 */

export const BYTE_TO_MEGABYTE_FACTOR = 1024 * 1024;

// ----------------------DATALAND SETTINGS----------------------
export const DATA_REQUEST_UPLOAD_MAX_FILE_SIZE_IN_BYTES =
  (parseInt(import.meta.env.VITE_DATA_REQUEST_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES) || 2) * BYTE_TO_MEGABYTE_FACTOR;

export const DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES =
  (parseInt(import.meta.env.VITE_DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES) || 100) * BYTE_TO_MEGABYTE_FACTOR;

export const MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY =
  parseInt(import.meta.env.VITE_MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY) || 3650;

export const MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER =
  parseInt(import.meta.env.VITE_MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER) || 10;
