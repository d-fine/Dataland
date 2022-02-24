<template>
  <div class="container">
    <span>
      <input :placeholder="placeholder_message" />
      <button class="waves-effect waves-light btn" type="submit" name="action">Search</button>
    </span>
  </div>

</template>

<script>
export default {
  name: "ESG",
  data() {
    return {
      placeholder_message: 'Search for ESG data'
    }
  }
}
</script>