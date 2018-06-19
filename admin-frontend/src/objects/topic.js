export function Topic(topic) {
  this.id = topic.id,
    this.clientId = topic.clientId,
    this.type = topic.type, //getAllTopicTypes
    this.name= topic.name,
    this.state = topic.state || false,
    this.msgType = topic.msgType, //getAllStandards
    this.msgTypeVersion = topic.msgTypeVersion, //getAllStandards
    this.description = topic.description,
    this.publishSolutionIDs = topic.publishSolutionIDs,
    this.showDescription = false
}
