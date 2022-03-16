<template>

  <table>
    <caption><h4>Results for {{entity}} {{status}}</h4></caption>

    <thead>
      <tr>
        <th  v-for="(header, i) in headers" :key="i">{{ header }}</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(dataset, index) in data" :key="index">
        <td v-for="(item, i) in dataset" :key="i">
          {{item}}

        </td>
      </tr>

    </tbody>
  </table>

</template>

<script>
export default {
  name: "ResultTable",
  props: ["headers", "data", "entity", "status"]
}
</script>