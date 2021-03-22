/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ranger.db;

import java.util.List;

import javax.persistence.NoResultException;

import org.apache.ranger.common.db.BaseDao;
import org.apache.ranger.entity.XXPortalUser;
import org.springframework.stereotype.Service;

@Service
public class XXPortalUserDao extends BaseDao<XXPortalUser> {

	public XXPortalUserDao(RangerDaoManagerBase daoManager) {
		super(daoManager);
	}

	public XXPortalUser findByLoginId(String loginId) {
		if (daoManager.getStringUtil().isEmpty(loginId)) {
			return null;
		}

		@SuppressWarnings("rawtypes")
		List resultList = getEntityManager()
				.createNamedQuery("XXPortalUser.findByLoginId")
				.setParameter("loginId", loginId).getResultList();
		if (resultList.size() != 0) {
			return (XXPortalUser) resultList.get(0);
		}
		return null;
	}

	public XXPortalUser findByEmailAddress(String emailAddress) {
		if (daoManager.getStringUtil().isEmpty(emailAddress)) {
			return null;
		}

		@SuppressWarnings("rawtypes")
		List resultList = getEntityManager()
				.createNamedQuery("XXPortalUser.findByEmailAddress")
				.setParameter("emailAddress", emailAddress)
				.getResultList();
		if (resultList.size() != 0) {
			return (XXPortalUser) resultList.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<XXPortalUser> findByRole(String userRole) {
		return getEntityManager().createNamedQuery("XXPortalUser.findByRole")
				.setParameter("userRole", userRole.toUpperCase())
				.getResultList();
	}

    @SuppressWarnings("unchecked")
	public List<Object[]> getUserAddedReport(){
    	return getEntityManager()
    			.createNamedQuery("XXPortalUser.getUserAddedReport")
    			.getResultList();
    }

	public XXPortalUser findByXUserId(Long xUserId) {
		if (xUserId == null) {
			return null;
		}
		try {
			return getEntityManager().createNamedQuery("XXPortalUser.findByXUserId", tClass)
					.setParameter("id", xUserId).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<XXPortalUser> findAllXPortalUser() {

		try {
			return getEntityManager().createNamedQuery(
					"XXPortalUser.findAllXPortalUser").getResultList();

		} catch (Exception e) {
			return null;
		}

	}
}
