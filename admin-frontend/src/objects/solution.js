export function Solution(solution) {
  this.id = solution.id,
    this.clientId = solution.clientId,
    this.subjectId = solution.subjectId,
    this.isService = solution.isService,
    this.isAdmin = solution.isAdmin,
    this.state = solution.state || false,
    this.name= solution.name,
    this.description = solution.description,
    this.organisation = solution.organisation
}
