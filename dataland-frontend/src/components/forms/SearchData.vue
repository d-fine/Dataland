<template>
  <div class="grid">
    <div class="col-12">
      <div class="grid">
        <RetrieveCompany/>
        <RetrieveSkyminder/>
      </div>
    </div>
  </div>
</template>

<script>
import RetrieveSkyminder from "@/components/forms/RetrieveSkyminder";
import RetrieveCompany from "@/components/forms/RetrieveCompany";

export default {
  name: "SearchData",
  components: {RetrieveCompany, RetrieveSkyminder}
}
</script>