export function Gateway(gateway) {
  this.id = gateway.id,
    this.clientId = gateway.clientId,
    this.name = gateway.name,
    this.state = gateway.state || false,
    this.description = gateway.description,
    this.managingType = gateway.managingType,
    this.showDescription = false
}
