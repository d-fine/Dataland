<template>
  <div class="grid">
    <div class = "col-offset-1 pt-5" >
      <h2>Learn about our vision</h2>
    </div>
    <div class="col-offset-3 pt-6">
      <div>
        <img src="@/assets/images/elements/orange_short_line.svg" alt="short orange line"/>
      </div>
      <div class = "col-offset-1 text-left">
        Our platform enables organisations to get all the data they need by requesting it directly from data owners while
        giving them a fair compensation.
      </div>
    </div>
   </div>
  <GridHelper/>
  <div class="grid align-items-center col-offset-1 col-10">
    <img src="@/assets/images/elements/video_placeholder.svg" alt="Showcase Video" class="pt-2 pb-6 w-full"/>
  </div>
  <div class="grid m-0">
    <div class="col-offset-1 col-5 p-0">
      <img src="@/assets/images/elements/Flow_Diagramm.svg" alt="Flow Diagramm" class="pl-0 w-full"/>
    </div>
    <div class="col-5 p-0 bg-primary ">
      <p>fsd</p>
    </div>
  </div>
  <div class="grid m-0">
    <div class="col-5 col-offset-1 p-0 surface-700 ">
      <p>placeholder</p>
    </div>
    <div class= "col-5 p-0 ">
      <img src="@/assets/images/elements/Data_Workflow.svg" alt="de" class="pl-0 pt-0 w-full"/>
    </div>
  </div>
  <GridHelper/>
  <div class="text-center">
    Dataland is not for you?
    <p class="text-primary text-center underline cursor-pointer">
      Let us know what we can do better.
    </p>
  </div>

</template>

<script>
import GridHelper from "@/components/helper/GridHelper";

export default {
  name: "MarketingSection",
  components: {GridHelper}
}
</script>

<style scoped>

</style>