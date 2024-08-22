import { TIME_DELAY_BETWEEN_SUBMIT_AND_NEXT_ACTION_IN_MS } from '@/utils/Constants';
import { type Router } from 'vue-router';

/**
 * function that redirects from the current page to the myDatasets page.
 * It is called after successfully submitting a newly created dataset
 * @param [router] the router of the Vue Component that calls the function
 */
export function redirectToMyDatasets(router: Router): void {
  setTimeout(() => {
    void router.push(`/datasets`);
  }, TIME_DELAY_BETWEEN_SUBMIT_AND_NEXT_ACTION_IN_MS);
}
