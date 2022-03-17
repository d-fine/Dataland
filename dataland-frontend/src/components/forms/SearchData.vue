<template>
<RetrieveSkyminder/>
<RetrieveCompany />
</template>

<script>
import RetrieveSkyminder from "@/components/forms/RetrieveSkyminder";
import RetrieveCompany from "@/components/forms/RetrieveCompany";
export default {
  name: "SearchData",
  components: {RetrieveCompany, RetrieveSkyminder}
}
</script>