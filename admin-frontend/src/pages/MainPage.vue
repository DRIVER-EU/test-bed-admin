<template>
  <v-app>
    <toolbar class="primary">
      <v-btn :disabled="isTestbedInitialized" v-on:click="initTestbed">Initialize testbed</v-btn>
      <v-btn :disabled="!isTestbedInitialized" v-on:click="startTrial">Start trial</v-btn>
    </toolbar>
    <main>
      <Overview></Overview>
    </main>
  </v-app>
</template>
<script>
  import {mapGetters} from 'vuex'
  import Overview from '../components/Overview'

  export default {
    name: 'App',
    components: {Overview},
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
