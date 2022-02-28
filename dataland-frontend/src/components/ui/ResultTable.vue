<template>
  <table>
    <caption>Table of Results</caption>
    <thead>
      <tr>
        <th v-for="(header, i) in headers" :key="i">{{ header }}</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(dataset, i) in data" :key="i">
        <td>{{dataset.Name}}</td>
        <td>{{dataset.Address[0]}}</td>
        <td>{{dataset.Website[0]}}</td>
        <td>{{dataset.Email[0]}}</td>
        <td>{{dataset.Phone[0]}}</td>
      </tr>
    </tbody>
  </table>
</template>

<script>
export default {
  name: "ResultTable",
  props: ["headers", "data"]
}
</script>