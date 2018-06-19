<template>
  <v-app>
    <v-toolbar class="primary">
      <img src="https://avatars2.githubusercontent.com/u/16935616?s=200&v=4" class="project-logo">
      <v-toolbar-title class="title">Admin tool</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn :disabled="isTestbedInitialized" v-on:click="initTestbed">Initialize testbed</v-btn>
      <v-btn :disabled="!isTestbedInitialized || isTrialStarted" v-on:click="startTrial">Start trial</v-btn>
    </v-toolbar>
    <main>
      <v-progress-circular v-if="loading" indeterminate color="primary"></v-progress-circular>
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
