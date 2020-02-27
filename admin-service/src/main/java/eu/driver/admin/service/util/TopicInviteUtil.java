package eu.driver.admin.service.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.model.core.TopicInvite;

public class TopicInviteUtil {
	
	public TopicInviteUtil() {
		
	}
	
	public List<TopicInvite> createTopicInviteMessages(String topicName, List<Solution> solutionList, List<String> publishClientIDs, List<String> subscribeClientIDs) {
		boolean allSolutionsPublish = false;
		boolean allSolutionsSubscribe = false;
		
		if (publishClientIDs.size() > 0) {
			if (publishClientIDs.get(0).equalsIgnoreCase("all")) {
				allSolutionsPublish = true;
			}
		}
		
		if (subscribeClientIDs.size() > 0) {
			if (subscribeClientIDs.get(0).equalsIgnoreCase("all")) {
				allSolutionsSubscribe = true;
			}
		}
		
		List<TopicInvite> inviteMsgs = new ArrayList<TopicInvite>();

		if (allSolutionsPublish && allSolutionsSubscribe) {
			for (Solution solution: solutionList) {
				if (!solution.getIsAdmin()) {
					TopicInvite inviteMsg = new TopicInvite();
					inviteMsg.setId(solution.getClientId());
					inviteMsg.setTopicName(topicName);
					inviteMsg.setPublishAllowed(true);
					inviteMsg.setSubscribeAllowed(true);
					
					inviteMsgs.add(inviteMsg);
				}
			}
		} else if (allSolutionsPublish && !allSolutionsSubscribe) {
			for (Solution solution: solutionList) {
				if (!solution.getIsAdmin()) {
					TopicInvite inviteMsg = new TopicInvite();
					inviteMsg.setId(solution.getClientId());
					inviteMsg.setTopicName(topicName);
					inviteMsg.setPublishAllowed(true);
					inviteMsg.setSubscribeAllowed(false);
					
					// find the client ID in the list of subscribers
					for (String clientID : subscribeClientIDs) {
						if (clientID.equalsIgnoreCase(solution.getClientId())) {
							inviteMsg.setSubscribeAllowed(true);
							break;	
						}
					}
					
					inviteMsgs.add(inviteMsg);
				}
			}
		} else if (!allSolutionsPublish && allSolutionsSubscribe) {
			for (Solution solution: solutionList) {
				if (!solution.getIsAdmin()) {
					TopicInvite inviteMsg = new TopicInvite();
					inviteMsg.setId(solution.getClientId());
					inviteMsg.setTopicName(topicName);
					inviteMsg.setSubscribeAllowed(true);
					inviteMsg.setPublishAllowed(false);
					// find the client ID in the list of subscribers
					for (String clientID : publishClientIDs) {
						if (clientID.equalsIgnoreCase(solution.getClientId())) {
							inviteMsg.setPublishAllowed(true);
							break;	
						}
					}
					inviteMsgs.add(inviteMsg);
				}
			}
		} else {
			Map<String, Map<String, Boolean>> solutionMap = new HashMap<String, Map<String, Boolean>>();
			
			for (String clientID : publishClientIDs) {
				Map<String, Boolean> flags = new HashMap<String, Boolean>();
				flags.put("publishAllowed", true);
				flags.put("subscribeAllowed", false);
				solutionMap.put(clientID, flags);
			}
			for (String clientID : subscribeClientIDs) {
				Map<String, Boolean> flags = solutionMap.get(clientID);
				if (flags == null) {
					flags = new HashMap<String, Boolean>();
					flags.put("publishAllowed", false);
					flags.put("subscribeAllowed", true);
					solutionMap.put(clientID, flags);
				} else {
					flags.put("subscribeAllowed", true);
					solutionMap.put(clientID, flags);
				}
			}
			
			for (Map.Entry<String, Map<String, Boolean>> entry : solutionMap.entrySet())
			{
				Map<String, Boolean> flags = entry.getValue();
				TopicInvite inviteMsg = new TopicInvite();
				inviteMsg.setId(entry.getKey());
				inviteMsg.setTopicName(topicName);
				inviteMsg.setPublishAllowed(flags.get("publishAllowed"));
				inviteMsg.setSubscribeAllowed(flags.get("subscribeAllowed"));
				inviteMsgs.add(inviteMsg);
			}
		}
		
		return inviteMsgs;
	}

}
