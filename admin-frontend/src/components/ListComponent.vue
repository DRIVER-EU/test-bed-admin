<template>
  <v-flex>
    <v-layout column>
      <v-card v-for="d in data" :key="d.id" class="mb-3 tertiary" >
        <v-card-title>
          <p>{{ d.name}}</p>
          <v-spacer></v-spacer>
          <v-icon v-if="d.hasOwnProperty('state')" :class="{'unavailable': !d.state, 'available': d.state}">lens</v-icon>
        </v-card-title>
        <v-card-text>
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
