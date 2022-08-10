import { createWebHistory, createRouter } from "vue-router";
import UploadEuTaxonomyDataForNonFinancials from "@/components/pages/UploadEuTaxonomyDataForNonFinancials.vue";
import SearchCompany from "@/components/pages/SearchCompany.vue";
import WelcomeDataland from "@/components/pages/WelcomeDataland.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import SearchTaxonomy from "@/components/pages/SearchTaxonomy.vue";
import CompanyAssociatedEuTaxonomyData from "@/components/pages/CompanyAssociatedEuTaxonomyData.vue";
import CompanyTaxonomySample from "@/components/pages/CompanyAssociatedEuTaxonomyDataSample.vue";
import TheImprint from "@/components/pages/TheImprint.vue";
import DataPrivacy from "@/components/pages/DataPrivacy.vue";
import NoContentFound from "@/components/pages/NoContentFound.vue";
import UploadEuTaxonomyDataForFinancials from "@/components/pages/UploadEuTaxonomyDataForFinancials.vue";
import UploadCompany from "@/components/pages/UploadCompany.vue";

const routes = [
  {
    path: "/",
    name: "Welcome to Dataland",
    component: WelcomeDataland,
  },
  {
    path: "/samples/eutaxonomy-non-financials",
    name: "Eu Taxonomy For Non-Financials Sample",
    component: CompanyTaxonomySample,
  },
  {
    path: "/companies/upload",
    name: "Upload Company",
    component: UploadCompany,
  },
  {
    path: "/companies/:companyID/frameworks/eutaxonomy-non-financials/upload",
    name: "Upload Eu Taxonomy Data For Non-Financials",
    component: UploadEuTaxonomyDataForNonFinancials,
  },
  {
    path: "/companies/:companyID/frameworks/eutaxonomy-financials/upload",
    name: "Upload Eu Taxonomy Data For Financials",
    component: UploadEuTaxonomyDataForFinancials,
  },
  {
    path: "/search/eutaxonomy",          //companies?frameworks=eutaxonomy
    name: "Search Eu Taxonomy Data",
    component: SearchTaxonomy,
  },
  {
    path: "/companies/:companyID",
    props: true,
    name: "Company Info",
    component: CompanyInformation,
  },
  {
    path: "/companies/:companyID/frameworks/eutaxonomy",
    props: true,
    name: "Company EU Taxonomy",
    component: CompanyAssociatedEuTaxonomyData,
  },
  {
    path: "/companies-only-search",
    name: "Search Company",
    component: SearchCompany,
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
    component: NoContentFound,
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
