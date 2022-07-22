import { createWebHistory, createRouter } from "vue-router";
import UploadData from "@/components/pages/UploadData.vue";
import SearchData from "@/components/pages/SearchData.vue";
import WelcomeDataland from "@/components/pages/WelcomeDataland.vue";
import TaxonomyPanel from "@/components/resources/taxonomy/TaxonomyPanel.vue";
import CompanyInformation from "@/components/resources/company/CompanyInformation.vue";
import SearchTaxonomy from "@/components/pages/SearchTaxonomy.vue";
import CompanyTaxonomy from "@/components/pages/CompanyTaxonomy.vue";
import CompanyTaxonomySample from "@/components/pages/CompanyTaxonomySample.vue";
import TheImprint from "@/components/pages/TheImprint.vue";
import DataPrivacy from "@/components/pages/DataPrivacy.vue";
import NoContentFound from "@/components/pages/NoContentFound.vue";

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
    path: "/taxonomysample",
    name: "Eu Taxonomy Sample",
    component: CompanyTaxonomySample,
  },
  {
    path: "/data/eutaxonomies/:dataID",
    props: true,
    name: "EU Taxonomy",
    component: TaxonomyPanel,
  },
  {
    path: "/companies/:companyID",
    props: true,
    name: "Company Info",
    component: CompanyInformation,
  },
  {
    path: "/companies/:companyID/eutaxonomies",
    props: true,
    name: "Company EU Taxonomy",
    component: CompanyTaxonomy,
  },
  {
    path: "/dataprivacy",
    name: "DataPrivacy",
    component: DataPrivacy,
  },
  {
    path: "/imprint",
    name: "LandingImprint",
    component: TheImprint,
  },
  {
    path: "/nocontent",
    name: "NoContentFound",
    component: NoContentFound
},
  {
    path: "/:notFound(.*)",
    redirect: "/nocontent",
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    window.scrollTo(0, 0);
  },
});

export { routes };
export default router;
