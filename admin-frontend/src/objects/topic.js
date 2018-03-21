export function Topic(topic) {
  this.id = topic.id,
    this.type = topic.type,
    this.name= topic.name,
    this.state = topic.state || false,
    this.description = topic.description,
    this.publishSolutionIDs = topic.publishSolutionIDs,
    this.showDescription = false
}
