<template>
  <v-app>
    <toolbar class="primary">
      <v-btn @click="openOverviewDiagramPage()">
        <v-icon left>insert_chart_outlined</v-icon>
        Overview
      </v-btn>
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn slot="activator">
          <v-icon left>settings</v-icon>
          Configurations
        </v-btn>
        <v-card>
          <v-list>
            <v-list-tile v-bind:key="configuration.id" v-for="configuration in configurations" @click="switchToConfiguration(configuration.name)">
              <v-icon left v-if="configuration.name === currentConfiguration.configName">radio_button_checked</v-icon>
              <v-icon left v-else>radio_button_unchecked</v-icon>
              {{ configuration.name }}
            </v-list-tile>
          </v-list>
        </v-card>
      </v-menu>
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn slot="activator">
          <v-icon left>settings_ethernet</v-icon>
          Modes
        </v-btn>
        <v-card>
          <v-list>
            <v-list-tile v-bind:key="mode" v-for="mode in modes" @click="switchToMode(mode)">
              <v-icon left v-if="mode === currentConfiguration.testbedMode">radio_button_checked</v-icon>
              <v-icon left v-else>radio_button_unchecked</v-icon>
              {{ mode }}
            </v-list-tile>
          </v-list>
        </v-card>
      </v-menu>
      <v-btn :disabled="isTestbedInitialized" v-on:click="initTestbed">
        <v-icon left>rotate_right</v-icon>
        Initialize test-bed
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
  import {configurationService} from '../service/ConfigurationService'

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
          configurationService.switchToConfiguration(name, this.currentConfiguration, this.modes);
        },
        switchToMode: function (name) {
          configurationService.switchToMode(name, this.currentConfiguration, this.configurations);
        },
        openOverviewDiagramPage() {
          console.log("Opening overview image");
          let routeData = this.$router.resolve({name: 'overviewDiagram'});
          window.open(routeData.href, '_blank');
        }
      },
    computed: mapGetters(['isTestbedInitialized', 'isTrialStarted', 'loading', 'configurations', 'modes', 'currentConfiguration']),
    created: function () {
      this.$store.dispatch('getCurrentConfiguration');
      this.$store.dispatch('getConfigurations');
      this.$store.dispatch('getModes');
    }
  }
</script>
