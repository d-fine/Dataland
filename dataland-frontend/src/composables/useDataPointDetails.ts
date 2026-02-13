import { ref, type Ref } from 'vue';
import type { DataPointTypeSpecification } from '@clients/specificationservice';

/**
 * Options for the useDataPointDetails composable following Dataland pattern.
 * Fetch function is injected for testability.
 */
export interface UseDataPointDetailsOptions {
  /**
   * Function to fetch data point type details by ID.
   * Should return a promise resolving to the data point type specification.
   * @param id - The data point type ID
   */
  fetchDataPointDetails: (id: string) => Promise<DataPointTypeSpecification>;
}

/**
 * Composable for managing data point detail fetching and state.
 * Used by the data point details modal to load and display technical specifications.
 * 
 * Following Dataland patterns:
 * - Accepts fetch function as parameter for testability
 * - Provides loading and error states
 * - Includes error recovery mechanism
 * 
 * @param options - Configuration options with fetch function
 */
export function useDataPointDetails({ fetchDataPointDetails }: UseDataPointDetailsOptions) {
  // Reactive state
  const dataPointDetails = ref<DataPointTypeSpecification | null>(null);
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  /**
   * Load data point type details by ID.
   * Sets loading state and handles errors.
   * 
   * @param dataPointTypeId - The ID of the data point type to load
   */
  async function loadDetails(dataPointTypeId: string): Promise<void> {
    try {
      isLoading.value = true;
      error.value = null;

      const details = await fetchDataPointDetails(dataPointTypeId);
      dataPointDetails.value = details;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load data point details';
      error.value = message;
      console.error(`Error loading data point details for ${dataPointTypeId}:`, err);
      
      // Clear details on error
      dataPointDetails.value = null;
      
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  /**
   * Clear the current data point details and reset state.
   * Useful when closing the modal or switching data points.
   */
  function clearDetails(): void {
    dataPointDetails.value = null;
    error.value = null;
  }

  /**
   * Retry loading the current data point details.
   * Only works if there was a previous error.
   * 
   * @param dataPointTypeId - The ID of the data point type to retry loading
   */
  async function retryLoad(dataPointTypeId: string): Promise<void> {
    if (!dataPointTypeId) {
      console.warn('Cannot retry without a data point type ID');
      return;
    }
    await loadDetails(dataPointTypeId);
  }

  return {
    // State
    dataPointDetails: dataPointDetails as Ref<DataPointTypeSpecification | null>,
    isLoading,
    error,

    // Methods
    loadDetails,
    clearDetails,
    retryLoad,
  };
}
