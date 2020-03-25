package eu.driver.admin.service.controller.role;

public class ActionMatrix {
	
	// view data
	private Boolean viewOrganisations = true;
	private Boolean viewConfigurations = true;
	private Boolean uploadSchemaFile = false;
	
	// edit data
	private Boolean createOrganisation = false;
	private Boolean createConfiguration = false;
	private Boolean createSolution = false;
	private Boolean createTopic = false;
	private Boolean createGateway = false;
	
	private Boolean editOrganisation = false;
	private Boolean editConfiguration = false;
	private Boolean editSolution = false;
	private Boolean editTopic = false;
	private Boolean editGateway = false;
	
	private Boolean removeOrganisation = false;
	private Boolean removeConfiguration = false;
	private Boolean removeSolution = false;
	private Boolean removeTopic = false;
	private Boolean removeGateway = false;
	
	private Boolean createCertificate = false;
	private Boolean downloadCertificate = false;
	
	// create reports
	private Boolean changeConfiguration = false;
	private Boolean changesecurity = false;
	private Boolean initTestbed = false;
	private Boolean startTrial = false;
	
	public ActionMatrix() {
		
	}
	
	public void setAllTrue() {
		this.viewOrganisations = true;
		this.viewConfigurations = true;
		this.uploadSchemaFile = true;
		
		// edit data
		this.createOrganisation = true;
		this.createConfiguration = true;
		this.createSolution = true;
		this.createTopic = true;
		this.createGateway = true;
		
		this.editOrganisation = true;
		this.editConfiguration = true;
		this.editSolution = true;
		this.editTopic = true;
		this.editGateway = true;
		
		this.removeOrganisation = true;
		this.removeConfiguration = true;
		this.removeSolution = true;
		this.removeTopic = true;
		this.removeGateway = true;
		
		this.createCertificate = true;
		this.downloadCertificate = true;
		
		// create reports
		this.changeConfiguration = true;
		this.changesecurity = true;
		this.initTestbed = true;
		this.startTrial = true;
	}
	
	public void resetState() {
		this.viewOrganisations = true;
		this.viewConfigurations = true;
		this.uploadSchemaFile = false;
		
		// edit data
		this.createOrganisation = false;
		this.createConfiguration = false;
		this.createSolution = false;
		this.createTopic = false;
		this.createGateway = false;
		
		this.editOrganisation = false;
		this.editConfiguration = false;
		this.editSolution = false;
		this.editTopic = false;
		this.editGateway = false;
		
		this.removeOrganisation = false;
		this.removeConfiguration = false;
		this.removeSolution = false;
		this.removeTopic = false;
		this.removeGateway = false;
		
		this.createCertificate = false;
		this.downloadCertificate = false;
		
		// create reports
		this.changeConfiguration = false;
		this.changesecurity = false;
		this.initTestbed = false;
		this.startTrial = false;
	}

	public Boolean getViewOrganisations() {
		return viewOrganisations;
	}

	public void setViewOrganisations(Boolean viewOrganisations) {
		this.viewOrganisations = viewOrganisations;
	}

	public Boolean getViewConfigurations() {
		return viewConfigurations;
	}

	public void setViewConfigurations(Boolean viewConfigurations) {
		this.viewConfigurations = viewConfigurations;
	}

	public Boolean getUploadSchemaFile() {
		return uploadSchemaFile;
	}

	public void setUploadSchemaFile(Boolean uploadSchemaFile) {
		this.uploadSchemaFile = uploadSchemaFile;
	}

	public Boolean getCreateOrganisation() {
		return createOrganisation;
	}

	public void setCreateOrganisation(Boolean createOrganisation) {
		this.createOrganisation = createOrganisation;
	}

	public Boolean getCreateConfiguration() {
		return createConfiguration;
	}

	public void setCreateConfiguration(Boolean createConfiguration) {
		this.createConfiguration = createConfiguration;
	}

	public Boolean getCreateSolution() {
		return createSolution;
	}

	public void setCreateSolution(Boolean createSolution) {
		this.createSolution = createSolution;
	}

	public Boolean getCreateTopic() {
		return createTopic;
	}

	public void setCreateTopic(Boolean createTopic) {
		this.createTopic = createTopic;
	}

	public Boolean getCreateGateway() {
		return createGateway;
	}

	public void setCreateGateway(Boolean createGateway) {
		this.createGateway = createGateway;
	}

	public Boolean getEditOrganisation() {
		return editOrganisation;
	}

	public void setEditOrganisation(Boolean editOrganisation) {
		this.editOrganisation = editOrganisation;
	}

	public Boolean getEditConfiguration() {
		return editConfiguration;
	}

	public void setEditConfiguration(Boolean editConfiguration) {
		this.editConfiguration = editConfiguration;
	}

	public Boolean getEditSolution() {
		return editSolution;
	}

	public void setEditSolution(Boolean editSolution) {
		this.editSolution = editSolution;
	}

	public Boolean getEditTopic() {
		return editTopic;
	}

	public void setEditTopic(Boolean editTopic) {
		this.editTopic = editTopic;
	}

	public Boolean getEditGateway() {
		return editGateway;
	}

	public void setEditGateway(Boolean editGateway) {
		this.editGateway = editGateway;
	}

	public Boolean getRemoveOrganisation() {
		return removeOrganisation;
	}

	public void setRemoveOrganisation(Boolean removeOrganisation) {
		this.removeOrganisation = removeOrganisation;
	}

	public Boolean getRemoveConfiguration() {
		return removeConfiguration;
	}

	public void setRemoveConfiguration(Boolean removeConfiguration) {
		this.removeConfiguration = removeConfiguration;
	}

	public Boolean getRemoveSolution() {
		return removeSolution;
	}

	public void setRemoveSolution(Boolean removeSolution) {
		this.removeSolution = removeSolution;
	}

	public Boolean getRemoveTopic() {
		return removeTopic;
	}

	public void setRemoveTopic(Boolean removeTopic) {
		this.removeTopic = removeTopic;
	}

	public Boolean getRemoveGateway() {
		return removeGateway;
	}

	public void setRemoveGateway(Boolean removeGateway) {
		this.removeGateway = removeGateway;
	}

	public Boolean getChangeConfiguration() {
		return changeConfiguration;
	}

	public void setChangeConfiguration(Boolean changeConfiguration) {
		this.changeConfiguration = changeConfiguration;
	}

	public Boolean getChangesecurity() {
		return changesecurity;
	}

	public void setChangesecurity(Boolean changesecurity) {
		this.changesecurity = changesecurity;
	}

	public Boolean getInitTestbed() {
		return initTestbed;
	}

	public void setInitTestbed(Boolean initTestbed) {
		this.initTestbed = initTestbed;
	}

	public Boolean getStartTrial() {
		return startTrial;
	}

	public void setStartTrial(Boolean startTrial) {
		this.startTrial = startTrial;
	}

	public Boolean getCreateCertificate() {
		return createCertificate;
	}

	public void setCreateCertificate(Boolean createCertificate) {
		this.createCertificate = createCertificate;
	}

	public Boolean getDownloadCertificate() {
		return downloadCertificate;
	}

	public void setDownloadCertificate(Boolean downloadCertificate) {
		this.downloadCertificate = downloadCertificate;
	}

}
