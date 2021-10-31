<template>
  <h1>{{ title }}</h1>
  <Modal :header="header" :title="title" theme="sale" /> 
</template>

<script>

import Modal from './components/Modal.vue'

export default {
  name: 'App',
  components: { Modal },
  data(){
    return {
      title: 'App 1',
      header: 'This Is The Header',
      title: 'This Is the Title'
    }
  },

methods: {
  handleClick(){
    console.log(this.$refs.name)
    this.$refs.name.classList.add('active')
    this.$refs.name.focus()
  }
}
}

</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
h1 {
  border-bottom: 1px solid #ddd;
  display: inline-block;
  padding-bottom: 10px;
}
</style>
