import { createWebHistory, createRouter } from "vue-router";
import UploadData from "@/components/pages/UploadData.vue";
import SearchData from "@/components/pages/SearchData.vue";
import WelcomeDataland from "@/components/pages/WelcomeDataland.vue";
import CompanyEU from "@/components/resources/taxonomy/TaxonomyPanel.vue";
import CompanyInformation from "@/components/resources/company/CompanyInformation.vue";
import SearchTaxonomy from "@/components/pages/SearchTaxonomy.vue";
import CompanyTaxonomy from "@/components/pages/CompanyTaxonomy.vue";

const routes = [
    {
        path: "/",
        name: "Welcome to Dataland",
        component: WelcomeDataland,
    },
    {
        path: "/upload",
        name: "Create Data",
        component: UploadData,
    },
    {
        path: "/search",
        name: "Search Data",
        component: SearchData,
    },
    {
        path: "/searchtaxonomy",
        name: "Search Eu Taxonomy",
        component: SearchTaxonomy,
    },
    {
        path: "/data/eutaxonomies/:dataID",
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
    {
        path: "/companies/:companyID/eutaxonomies",
        props(route:any) {
            return {
                companyID: parseInt(route.params.companyID)
            }
        },
        name: "Company EU Taxonomy",
        component: CompanyTaxonomy,
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export { routes }
export default router;