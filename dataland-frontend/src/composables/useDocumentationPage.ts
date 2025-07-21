import { computed, onMounted, watch } from 'vue';
import { useSpecificationLoader } from './useSpecificationLoader';

export type DocumentationPageType = 'frameworks' | 'data-point-types' | 'data-point-base-types';

interface UseDocumentationPageOptions<T> {
  pageType: DocumentationPageType;
  entityId: string;
  titleSuffix?: string;
}

export function useDocumentationPage<T = any>({ 
  pageType, 
  entityId, 
  titleSuffix = 'Specification' 
}: UseDocumentationPageOptions<T>) {
  
  const pageTitle = computed(() => {
    const formattedId = pageType === 'frameworks' 
      ? entityId.toUpperCase() 
      : entityId;
    
    const typeLabel = pageType === 'frameworks' 
      ? 'Framework'
      : pageType === 'data-point-types'
      ? 'Data Point Type'
      : 'Data Point Base Type';
    
    return `${formattedId} ${typeLabel} ${titleSuffix}`;
  });

  const apiUrl = computed(() => `/specifications/${pageType}/${entityId}`);

  const { waitingForData, error, specificationData, loadSpecificationData } = 
    useSpecificationLoader<T>({
      url: apiUrl,
    });

  const loadData = () => {
    loadSpecificationData();
  };

  onMounted(() => {
    loadData();
  });

  watch(() => entityId, () => {
    loadData();
  });

  return {
    pageTitle,
    waitingForData,
    error,
    specificationData,
    loadData,
  };
}