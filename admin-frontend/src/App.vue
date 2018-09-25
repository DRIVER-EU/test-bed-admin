<template>
  <v-app>
    <v-toolbar class="primary">
      <img src="./assets/logo.png" class="project-logo">
      <v-toolbar-title class="title">Admin tool</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn :disabled="isTestbedInitialized" v-on:click="initTestbed">Initialize testbed</v-btn>
      <v-btn :disabled="!isTestbedInitialized || isTrialStarted" v-on:click="startTrial">Start trial</v-btn>
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
          this.$store.commit('LOADING', true)
        }
        ,
        startTrial: function () {
          console.log('startTrial')
          this.$store.dispatch('startTrial')
        }
      }
    ,
    computed:
      mapGetters(['isTestbedInitialized', 'isTrialStarted', 'loading'])
  }
</script>
<style lang="stylus">
  @import './stylus/main'
</style>
