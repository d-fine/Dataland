<template>
  <table>
    <caption><h4>{{ entity }}</h4></caption>
    <thead>
    <tr>
      <th v-for="(header, i) in headers" :key="i">{{ header }}</th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="(dataset, index) in data" :key="index">
      <td v-for="(item, i) in dataset" :key="i">
        <router-link v-if="i === linkKey" :to="route + dataset[linkID]">{{ item }}</router-link>
        <template v-else>
          {{item }}
        </template>
      </td>
    </tr>
    </tbody>
  </table>
</template>

<script>
export default {
  name: "ResultTable",
  props: ["headers", "data", "entity", "route", "linkID", "linkKey"]
}
</script>