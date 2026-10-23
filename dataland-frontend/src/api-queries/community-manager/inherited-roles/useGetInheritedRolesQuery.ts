import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { inheritedRolesKeys } from '@/api-queries/community-manager/inherited-roles/inheritedRolesKeys.ts';
import { computed, type Ref } from 'vue';

/**
 * Vue Query hook that fetches inherited roles for a given keycloak user id.
 *
 * @param {Ref<string|undefined>} keycloakUserId - Reactive reference containing the keycloak user id; query runs when truthy.
 * @returns {UseQueryReturnType<{[key: string]: string[]}, Error>} Query result containing the inherited roles.
 */
export function useGetInheritedRolesQuery(
  keycloakUserId: Ref<string | undefined>
): UseQueryReturnType<{ [key: string]: string[] }, Error> {
  const apiClientProvider = useApiClient();
  const queryKey = computed(() => inheritedRolesKeys.byUserId(keycloakUserId.value));
  const enabled = computed(() => !!keycloakUserId.value);

  return useQuery<{ [key: string]: string[] }, Error>({
    queryKey,
    enabled,
    queryFn: async () => {
      if (!keycloakUserId.value) {
        throw new Error('keycloakUserId is undefined');
      }
      const response = await apiClientProvider.apiClients.inheritedRolesController.getInheritedRoles(
        keycloakUserId.value
      );
      return response.data;
    },
  });
}
