import { TIME_DELAY_BETWEEN_SUBMIT_AND_NEXT_ACTION_IN_MS } from '@/utils/Constants';
import router from '@/router';

/**
 * function that redirects from the current page to the myDatasets page.
 * It is called after successfully submitting a newly created dataset
 */
export function redirectToMyDatasets(): void {
  setTimeout(() => {
    void router.push(`/datasets`);
  }, TIME_DELAY_BETWEEN_SUBMIT_AND_NEXT_ACTION_IN_MS);
}
