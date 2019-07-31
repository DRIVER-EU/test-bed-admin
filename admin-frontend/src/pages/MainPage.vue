<template>
  <v-app>
    <toolbar class="primary">
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn slot="activator">
          <v-icon left>settings</v-icon> <!-- settings_ethernet -->
          Configurations
        </v-btn>
        <v-card>
          <v-list>
            <v-list-tile v-bind:key="configuration.name" v-for="configuration in configurations" @click="switchToConfiguration(configuration.name)">
              <v-icon left v-if="configuration.name === currentConfiguration.configName">radio_button_checked</v-icon>
              <v-icon left v-else>radio_button_unchecked</v-icon>
              {{ configuration.name }}
            </v-list-tile>
          </v-list>
        </v-card>
      </v-menu>
      <v-btn :disabled="isTestbedInitialized" v-on:click="initTestbed">
        <v-icon left>rotate_right</v-icon>
        Initialize testbed
      </v-btn>
      <v-btn :disabled="!isTestbedInitialized" v-on:click="startTrial">
        <v-icon left>play_arrow</v-icon>
        Start trial
      </v-btn>
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
        },
        startTrial: function () {
          console.log('startTrial')
          this.$store.dispatch('startTrial')
        },
        switchToConfiguration: function (name) {
          console.log('Setting configuration to ', name)
        }
      },
    computed: mapGetters(['isTestbedInitialized', 'isTrialStarted', 'loading', 'configurations', 'currentConfiguration']),
    created: function () {
      this.$store.dispatch('getCurrentConfiguration');
      this.$store.dispatch('getConfigurations');
    }
  }
</script>
