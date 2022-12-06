import { createWebHistory, createRouter } from "vue-router";
import UploadEuTaxonomyDataForNonFinancials from "@/components/pages/UploadEuTaxonomyDataForNonFinancials.vue";
import WelcomeDataland from "@/components/pages/WelcomeDataland.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import SearchCompaniesForFrameworkData from "@/components/pages/SearchCompaniesForFrameworkData.vue";
import ViewEuTaxonomyNonFinancialsSample from "@/components/pages/ViewEuTaxonomyNonFinancialsSample.vue";
import TheImprint from "@/components/pages/TheImprint.vue";
import DataPrivacy from "@/components/pages/DataPrivacy.vue";
import NoContentFound from "@/components/pages/NoContentFound.vue";
import UploadEuTaxonomyDataForFinancials from "@/components/pages/UploadEuTaxonomyDataForFinancials.vue";
import UploadCompany from "@/components/pages/UploadCompany.vue";
import SearchCompanies from "@/components/pages/SearchCompanies.vue";
import ViewEuTaxonomyFinancials from "@/components/pages/ViewEuTaxonomyFinancials.vue";
import ViewEuTaxonomyNonFinancials from "@/components/pages/ViewEuTaxonomyNonFinancials.vue";
import ApiKeysPage from "@/components/pages/ApiKeysPage.vue";

const routes = [
  {
    path: "/",
    name: "Welcome to Dataland",
    component: WelcomeDataland,
  },
  {
    path: "/samples/eutaxonomy-non-financials",
    name: "Eu Taxonomy For Non-Financials Sample",
    component: ViewEuTaxonomyNonFinancialsSample,
  },
  {
    path: "/companies/upload",
    name: "Upload Company",
    component: UploadCompany,
  },
  {
    path: "/companies/:companyID/frameworks/eutaxonomy-non-financials/upload",
    props: true,
    name: "Upload Eu Taxonomy Data For Non-Financials",
    component: UploadEuTaxonomyDataForNonFinancials,
  },
  {
    path: "/companies/:companyID/frameworks/eutaxonomy-financials/upload",
    props: true,
    name: "Upload Eu Taxonomy Data For Financials",
    component: UploadEuTaxonomyDataForFinancials,
  },
  {
    path: "/companies",
    name: "Search Companies for Framework Data",
    component: SearchCompaniesForFrameworkData,
  },
  {
    path: "/companies/:companyID",
    props: true,
    name: "Company Info",
    component: CompanyInformation,
  },
  {
    path: "/companies/:companyID/frameworks/eutaxonomy-non-financials",
    props: true,
    name: "Company EU Taxonomy for non financials",
    component: ViewEuTaxonomyNonFinancials,
  },
  {
    path: "/companies/:companyID/frameworks/eutaxonomy-financials",
    props: true,
    name: "Company EU Taxonomy for financials",
    component: ViewEuTaxonomyFinancials,
  },
  {
    path: "/companies-only-search",
    name: "Search Companies",
    component: SearchCompanies,
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
    path: "/api-key",
    name: "ApiKeysPage",
    component: ApiKeysPage,
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
