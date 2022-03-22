<template>
  <div class="container">
    <div class="row">
      <div class="col m12 s12">
        <div class="card">
          <slot></slot>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "CardWrapper"
}
</script>
