function getHost() {
  const host = window.location.host;
  if (host === "localhost:8080") {
    return "localhost:8090";
  } else {
    return host;
  }
}

export default {
  HTTP_BASE: 'http://' + getHost() + '/AdminService',
  //HTTP_BASE: '/AdminService',
  WEBSOCKET: 'ws://'+ getHost() + '/AdminServiceWSEndpoint',
  SOCKJS: 'http://'+ getHost() + '/AdminServiceWSEndpoint'
  //SOCKJS: '/AdminServiceWSEndpoint'
}
