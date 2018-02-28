package eu.driver.admin.service.dto;

import java.util.ArrayList;
import java.util.List;

import eu.driver.admin.service.dto.solution.Solution;

public class SolutionList {
	private List<Solution> solutions = new ArrayList<Solution>();
	
	public SolutionList() {
		
	}

	public List<Solution> getSolutions() {
		return solutions;
	}

	public void setSolutions(List<Solution> solutions) {
		this.solutions = solutions;
	}
}
