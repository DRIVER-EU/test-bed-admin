export function Topic(topic) {
  this.id = topic.id,
    this.clientId = topic.clientId,
    this.type = topic.type,
    this.name= topic.name,
    this.state = topic.state || false,
    this.msgType = topic.msgType,
    this.msgTypeVersion = topic.msgTypeVersion,
    this.description = topic.description,
    this.publishSolutionIDs = topic.publishSolutionIDs,
    this.showDescription = false
}
