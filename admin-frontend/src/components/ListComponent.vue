<template>
  <v-flex>
    <v-layout column>
      <v-card v-for="d in data" :key="d.id" class="mb-3 tertiary">
        <v-card-title style="padding-top: 0;
    padding-bottom: 0;">
          {{ d.name}}
          <v-spacer></v-spacer>
          <v-icon v-if="d.hasOwnProperty('state')" :class="{'unavailable': !d.state, 'available': d.state}">lens
          </v-icon>
          <v-card-actions>
            <v-spacer></v-spacer>
              <v-btn icon @click.native="d.showDescription = !d.showDescription">
                <v-icon>{{ d.showDescription ? 'keyboard_arrow_up' : 'keyboard_arrow_down' }}</v-icon>
              </v-btn>
          </v-card-actions>
        </v-card-title>
        <v-card-text v-show="d.showDescription">
          {{d.description}}
        </v-card-text>
      </v-card>
    </v-layout>
  </v-flex>
</template>

<script>
  export default {
    name: "ListComponent",
    props: ['data']
  }
</script>
