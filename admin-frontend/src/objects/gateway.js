export function Gateway(gateway) {
  this.id = gateway.id,
    this.name = gateway.name,
    this.state = gateway.state || false,
    this.description = gateway.description,
    this.managingType = gateway.managingType
}
