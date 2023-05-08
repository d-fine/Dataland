import { AxiosError, AxiosResponse } from "axios";

/**
 * Takes an AxiosError and attempts to return a formatted error message derived from the error values.
 * @param error AxiosError | unknown
 * @returns a string message using AxiosError values
 */
export function formatAxiosErrorMessage(error: AxiosError | unknown): string {
  if (!(error instanceof AxiosError)) return (error as Error).message;

  const err = error as AxiosError;
  const response = err.response as AxiosResponse<{ errors: { summary: string; message: string }[] }>;
  let errSummary = JSON.stringify(err);
  let errMessage = "";

  if (response.data.errors[0]) {
    errSummary = response.data.errors[0].summary;
    errMessage = response.data.errors[0].message;
  }

  return `${errSummary} ${errMessage}`;
}
