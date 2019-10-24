export function Solution(solution) {
  this.id = solution.id,
    this.clientId = solution.clientId,
    this.subjectId = solution.subjectId,
    this.isAdmin = solution.isAdmin,
    this.isService = solution.isService,
    this.name= solution.name,
    this.state = solution.state || false,
    this.description = solution.description,
    this.showDescription = false,
    this.orgName = solution.orgName
}
