<template>
    <v-layout class="mt-3 mr-3 mb-3 ml-3">
      <v-flex row wrap xs2>
        <v-card style="height:100%">
          <v-list>
            <v-list-tile>
              <v-list-tile-content>
                <v-list-tile-title> Gateway 1</v-list-tile-title>
              </v-list-tile-content>
              <v-list-tile-action>
                <v-icon color="grey">chevron_right</v-icon>
              </v-list-tile-action>
            </v-list-tile>
            <v-divider></v-divider>
            <v-list-tile>
              <v-list-tile-content>
                <v-list-tile-title> Gateway 2</v-list-tile-title>
              </v-list-tile-content>
              <v-list-tile-action>
                <v-icon color="grey">chevron_right</v-icon>
              </v-list-tile-action>
            </v-list-tile>

          </v-list>
        </v-card>
      </v-flex>
      <v-flex row wrap xs10>
        <v-card style="height:100%">

        </v-card>
      </v-flex>
    </v-layout>
</template>

<script>
  export default {
    name: "ConfigureGateways"
  }
</script>

<style scoped>

</style>
