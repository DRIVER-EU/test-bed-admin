<template>
  <v-data-table :items=logEntries :headers="headers">
    <template slot="items" slot-scope="props">
      <tr :class="{logError: props.item.level === 'ERROR' || props.item.level === 'SEVER', logWarning: props.item.level === 'WARNING'}">
        <td>{{props.item.id}}</td>
        <td>{{props.item.sendDate}}</td>
        <td>{{props.item.level}}</td>
        <td>{{props.item.clientId}}</td>
        <td>{{props.item.message}}</td>
      </tr>
    </template>
  </v-data-table>
</template>


<script>
  export default {
    name: "LogsTable",
    data() {
      return {
        headers: [
          {
            text: 'LogID',
            align: 'left',
            sortable: false,
            value: 'id'
          },
          {
            text: 'Date/Time',
            align: 'left',
            sortable: false,
            value: 'date'
          },
          {
            text: 'Level',
            align: 'left',
            sortable: false,
            value: 'level'
          },
          {
            text: 'ClientID',
            align: 'left',
            sortable: false,
            value: 'clientID'
          },
          {
            text: 'Message',
            align: 'left',
            sortable: false,
            value: 'message'
          },
        ]
      }
    },
    computed: {
      logEntries() {
        var logEntries = this.$store.getters.logEntries
        return logEntries.sort(( a, b) => {
          return a.date < b.date;
        })
      }
    }
  }
</script>
