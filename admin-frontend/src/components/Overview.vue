<template>
  <v-container fluid grid-list-xl class="mt-0">
    <v-layout row wrap justify-space-around style="height: 55vh"
              class="scroll-y">
      <v-flex xs4>
        <h3 class="text-xs-center primary--text">Tools</h3>
        <list-component :data=solutions></list-component>
      </v-flex>
      <v-flex xs4>
        <h3 class="text-xs-center primary--text">Topics</h3>
        <list-component :data=topics></list-component>
      </v-flex>
      <v-flex xs4>
        <h3 class="text-xs-center primary--text">Gateways</h3>
        <list-component :data=gateways></list-component>
      </v-flex>
    </v-layout>
    <v-layout column style="height: 30vh" class="scroll-y">
      <v-flex>
      <v-card>
        <logs-table></logs-table>
      </v-card>
      </v-flex>
    </v-layout>
  </v-container>
</template>

<script>
  import {mapGetters} from 'vuex'
  export default {
    computed:
      mapGetters(['solutions', 'topics', 'gateways'])
    ,
    name: "Overview"
  }
</script>
