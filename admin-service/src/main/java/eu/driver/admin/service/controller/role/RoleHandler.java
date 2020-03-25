package eu.driver.admin.service.controller.role;

public class RoleHandler {
	
	private static RoleHandler aMe = null;
	
	private Roles activeRole = Roles.MONITORING;
	
	private RoleHandler() {
		
	}
	
	public static synchronized RoleHandler getInstance() {
		if (RoleHandler.aMe == null) {
			RoleHandler.aMe = new RoleHandler();
		}
		return RoleHandler.aMe;
	}
	
	public void setActiveRole(Roles activeRole) {
		this.activeRole = activeRole;
	}
	
	public Roles getActiveRole() {
		return this.activeRole;
	}

}
