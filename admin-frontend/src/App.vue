<template>
  <v-app>
    <v-toolbar class="primary">
      <img src="https://avatars2.githubusercontent.com/u/16935616?s=200&v=4" class="project-logo">
      <v-toolbar-title class="title">Admin tool</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-alert v-for="alert in alerts" :key="alert.id" :type="alert.type" dismissible v-model="alert.isVisible">
        <v-badge left>1</v-badge>
        {{alert.text}}
      </v-alert>
      <v-spacer></v-spacer>
      <v-btn v-on:click="initTestbed">Initialize testbed</v-btn>
      <v-btn :disabled="!testbedAvailable" v-on:click="startTrial">Start trial</v-btn>
    </v-toolbar>
    <main>
      <router-view></router-view>
    </main>
  </v-app>
</template>

<script>
  import {mapGetters} from 'vuex'

  export default {
    name: 'App',
    methods:
      {
        initTestbed: function () {
          console.log('initTestbed')
          this.$store.dispatch('initTestbed')
        }
        ,
        startTrial: function () {
          console.log('startTrial')
          this.$store.dispatch('startTrial')
        }
      }
    ,
    computed:
      mapGetters(['alerts','testbedAvailable'])
  }
</script>
<style lang="stylus">
  @import './stylus/main'
</style>
