<template>
  <div class="list-wrapper">
        <v-container grid-list-lg class="pl-5 pt-5">
          <v-layout column >
            <v-flex md4>
              <v-card>
                <v-card-title>
                  <div>Tool 1</div>
                  <div>dsfdfg</div>
                  <div>dsfdfg</div><div>dsfdfg</div><div>dsfdfg</div>

                </v-card-title>
                <v-card-actions>

                </v-card-actions>
              </v-card>
            </v-flex>
            <v-flex md4>
              <v-card>
                <v-card-title>
                  Tool 2
                </v-card-title>
                <v-card-actions>

                </v-card-actions>
              </v-card>
            </v-flex>
          </v-layout>
        </v-container>
  </div>
</template>

<script>
  export default {
    name: "Tools"
  }
</script>

<style scoped>
  .list-wrapper {
    min-width: 1200px;
  }
</style>
