<template>
  <CompanyRolesCard v-for="group in companyRoles" :key="group.role" :companyId="companyId" :group="group" />
</template>

<script setup lang="ts">
import CompanyRolesCard from '@/components/resources/companyCockpit/CompanyRolesCard.vue';
import { CompanyRole } from '@clients/communitymanager';

defineProps<{ companyId: string }>();

const companyRoles = [
  {
    role: CompanyRole.MemberAdmin,
    title: 'Admins',
    icon: 'pi pi-shield',
    info: 'The User Admin has the rights to add or remove other user admins and members. Admins manage other users and can control who has access to what data or features within Dataland.',
  },
  {
    role: CompanyRole.Member,
    title: 'Members',
    icon: 'pi pi-users',
    info: 'Members have the ability to request unlimited data. They are key users on Dataland, utilising the data available to make informed decisions or produce reports.',
  },
  {
    role: CompanyRole.CompanyOwner,
    title: 'Company Owners',
    icon: 'pi pi-crown',
    info: "Company owners have the highest level of access and can add other users as company owners. They are responsible for the governance of the company's profile on Dataland. The company owner is also the one accountable for the KYC process.",
  },
  {
    role: CompanyRole.DataUploader,
    title: 'Uploaders',
    icon: 'pi pi-cloud-upload',
    info: 'Uploaders have the responsibility of ensuring all relevant data is uploaded to the platform for analysis and interpretation.',
  },
] as const;
</script>
