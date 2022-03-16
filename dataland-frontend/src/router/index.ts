import { createWebHistory, createRouter } from "vue-router";
import CreateData from "@/components/forms/CreateData.vue";
import SearchData from "@/components/forms/SearchData.vue";
import WelcomeDataland from "@/components/WelcomeDataland.vue";
import CompanyEU from "@/components/pages/taxonomy/TaxonomyData.vue";
import TaxonomyData from "@/components/pages/taxonomy/TaxonomyList.vue";
import CompanyInformation from "@/components/pages/company/CompanyInformation.vue";

const routes = [
    {
        path: "/",
        name: "Welcome to Dataland",
        component: WelcomeDataland,
    },
    {
        path: "/upload",
        name: "Create Data",
        component: CreateData,
    },
    {
        path: "/search",
        name: "Search Data",
        component: SearchData,
    },
    {
        path: "/eutaxonomies",
        props: true,
        name: "EU Taxonomy Data",
        component: TaxonomyData,
    },
    {
        path: "/eutaxonomies/:dataID",
        props: true,
        name: "EU Taxonomy",
        component: CompanyEU,
    },
    {
        path: "/companies/:companyID",
        props: true,
        name: "Company Info",
        component: CompanyInformation,
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;