import {store} from '../store'
import {_} from 'vue-underscore'
import {webSocketConnection} from '../service/WebSocketConnection'

export function heartbeatController () {
  let prop = store.state.socket;
  prop.messageAccepted = false;
  if (!prop.pingTimer) {
    prop.pingTimer = setInterval(function () {
      webSocketConnection.send('{"mutation":"HBREQUEST",' +
        '"requestId": "' + _.uniqueId() + '",' +
        '"sendTime":"' + new Date().toUTCString() + '"}')
    }, 5000);
    console.log("Started heartbeat timer", prop.pingTimer)
  }
  if (!prop.pingTimeOutTimer) {
    prop.pingTimeOutTimer = setInterval(function () {
      if (!store.state.socket.messageAccepted) {
        webSocketConnection.close()
      } else {
        prop.messageAccepted = false
      }
    }, 10000);
    console.log("Started heartbeat timeout timer", prop.pingTimeOutTimer)
  }
}
