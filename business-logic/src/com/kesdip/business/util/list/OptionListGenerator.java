/*
 * Disclaimer:
 * Copyright 2008 - KESDIP E.P.E & Stelios Gerogiannakis - All rights reserved.
 * eof Disclaimer
 * 
 * Date: Dec 8, 2008
 * @author <a href="mailto:sgerogia@gmail.com">Stelios Gerogiannakis</a>
 */

package com.kesdip.business.util.list;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.kesdip.business.domain.generated.AccessRight;
import com.kesdip.business.domain.generated.User;
import com.kesdip.business.logic.BaseLogicAction;

/**
 * Utility bean offering option list values.
 * 
 * @author gerogias
 */
public class OptionListGenerator extends BaseLogicAction {

	/**
	 * The list of access rights.
	 */
	private List<AccessRight> accessRightList = null;
	
	/**
	 * The logger.
	 */
	private final static Logger logger = Logger
			.getLogger(OptionListGenerator.class);

	/**
	 * Default constructor.
	 */
	public OptionListGenerator() {
		// do nothing
	}


	/**
	 * Get a list of users.
	 * 
	 * @return List a list of users
	 */
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<User> getUsers() {
		List<User> users = getHibernateTemplate().find(
				"from " + User.class.getName() + " u");
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieved " + users.size() + " users");
		}
		return users;
	}

	/**
	 * Get a list of access rights.
	 * 
	 * @return List a list of access rights
	 */
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<AccessRight> getAccessRights() {
		if (accessRightList == null) {
			accessRightList = getHibernateTemplate().find(
					"from " + AccessRight.class.getName());
			if (logger.isDebugEnabled()) {
				logger.debug("Retreived " + accessRightList.size() + " roles");
			}
		}
		return accessRightList;
	}

}