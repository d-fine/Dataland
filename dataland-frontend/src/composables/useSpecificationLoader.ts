import { ref, type Ref } from 'vue';

export interface UseSpecificationLoaderOptions {
  url: string | Ref<string>;
  transform?: (data: any) => any;
}

export function useSpecificationLoader<T = any>(options: UseSpecificationLoaderOptions) {
  const { url, transform } = options;
  
  const waitingForData = ref(true);
  const error = ref<string | null>(null);
  const specificationData = ref<T | null>(null);

  const loadSpecificationData = async (): Promise<void> => {
    try {
      waitingForData.value = true;
      error.value = null;

      const resolvedUrl = typeof url === 'string' ? url : url.value;
      const response = await fetch(resolvedUrl);

      if (!response.ok) {
        throw new Error(`Failed to load specification data: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      specificationData.value = transform ? transform(data) : data as T;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred';
      console.error('Error loading specification data:', err);
    } finally {
      waitingForData.value = false;
    }
  };

  return {
    waitingForData,
    error,
    specificationData,
    loadSpecificationData,
  };
}