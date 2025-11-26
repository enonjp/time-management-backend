package co.jp.enon.tms.usermaintenance.service;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.jp.enon.tms.common.BaseService;
import co.jp.enon.tms.common.exception.OptimisticLockException;
import co.jp.enon.tms.usermaintenance.dao.PtUserDao;
import co.jp.enon.tms.usermaintenance.dto.UserDeleteDto;
import co.jp.enon.tms.usermaintenance.dto.UserSearchManyDto;
import co.jp.enon.tms.usermaintenance.dto.UserSearchOneDto;
import co.jp.enon.tms.usermaintenance.dto.UserUpdateDto;
import co.jp.enon.tms.usermaintenance.entity.PtUser;

//import co.jp.enon.tms.usermaintenance.mapper.PvUserCompanyUserMapper;
//import co.jp.enon.tms.usermaintenance.mapper.PvUserMonthOrderMapper;
//import co.jp.enon.tms.usermaintenance.mapper.PvUserMonthReportMapper;


@Service
public class UserService extends BaseService {

	final static Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
    private PtUserDao ptUserDao;
	
	// Users search
//	public void searchMany(UserSearchManyDto userSearchManyDto) throws Exception {
//		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());

//		UserSearchManyDto.RequestHd reqHd = userSearchManyDto.getReqHd();
//		//if deleted = 1, only then deleted users will be added
//		List<PvUserCompanyUserRepository> listPvUserCompanyUserRepository = pvUserCompanyUserMapper.selectManyUsers(
//				reqHd.getCompanyId(), reqHd.getSei(), reqHd.getMei(), reqHd.getStatus(),(byte)0, reqHd.getDeleted());
//
//		List<UserSearchManyDto.ResponseDt> listResDt = userSearchManyDto.getResDt();
//        int prevUserId = 0;
//		for (Iterator<PvUserCompanyUserRepository> it = listPvUserCompanyUserRepository.iterator(); it.hasNext();) {
//			PvUserCompanyUserRepository pvUserCompanyUserRepository = it.next();
//			// listtrnActorSearchResDt.add(convertTranActorSearchResDt(rdbActor));
//
//			UserSearchManyDto.ResponseDt resDt = new UserSearchManyDto.ResponseDt();
//			if (prevUserId != 0 && prevUserId == pvUserCompanyUserRepository.getUserId()){
//				int indexOfLastElement = listResDt.size() - 1;
//				listResDt.remove(indexOfLastElement);
//				resDt.setAuthorityName("ユーザ、管理者");
//			}else {
//				resDt.setAuthorityName(pvUserCompanyUserRepository.getAuthorityName());
//			}
//			prevUserId = pvUserCompanyUserRepository.getUserId();
//			resDt.setUserId(pvUserCompanyUserRepository.getUserId());
//			resDt.setCompanyId(pvUserCompanyUserRepository.getCompanyId());
//			resDt.setStatus(getStatus(pvUserCompanyUserRepository.getStatus()));
//			if (StringUtils.isNotEmpty(pvUserCompanyUserRepository.getEmail())) {
//				resDt.setEmail(pvUserCompanyUserRepository.getEmail());
//			}
//			resDt.setMei(pvUserCompanyUserRepository.getMei());
//			resDt.setSei(pvUserCompanyUserRepository.getSei());
//			resDt.setMeiKana(pvUserCompanyUserRepository.getMeiKana());
//			resDt.setSeiKana(pvUserCompanyUserRepository.getSeiKana());
//			resDt.setDeleted(pvUserCompanyUserRepository.getDeleted());
//			resDt.setCity(pvUserCompanyUserRepository.getCity());
//			resDt.setPhone(pvUserCompanyUserRepository.getPhone());
//			resDt.setPrefacture(pvUserCompanyUserRepository.getPrefacture());
//			resDt.setStreetNumber(pvUserCompanyUserRepository.getStreetNumber());
//			resDt.setBuildingName(pvUserCompanyUserRepository.getBuildingName());
//			resDt.setCreatedAt(pvUserCompanyUserRepository.getCreatedAt());
//			resDt.setUpdatedAt(pvUserCompanyUserRepository.getUpdatedAt());
//
//			listResDt.add(resDt);
//		}
//
//		makeResponseTitle(userSearchManyDto);
//
//		if (listResDt.size() > 0) {
//			userSearchManyDto.setResultCode("000");
//		} else {
//			userSearchManyDto.setResultCode("001");
//		}
//		return;
//	}

	// Search 1 user
    public void searchOne(UserSearchOneDto userSearchOneDto) throws Exception {
    	logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
    	UserSearchOneDto.RequestHd reqHd = userSearchOneDto.getReqHd();
        PtUser user = ptUserDao.findByEmail(reqHd.getEmail()); // JDBC call

        UserSearchOneDto.ResponseHd resDt = userSearchOneDto.getResHd();
        if (user != null) {
        	resDt.setUserId(user.getUserId());
            if (StringUtils.isNotEmpty(user.getEmail())) {
            	resDt.setEmail(user.getEmail());
            }
            resDt.setFirstName(user.getFirstName());
            resDt.setLastName(user.getLastName());
            resDt.setPassword(user.getPassword());
            resDt.setResetPasswordToken(user.getResetPasswordToken());
            resDt.setActive(user.getActive());
            resDt.setRole(user.getRole());

            userSearchOneDto.setResultCode("000");
        } else {
            userSearchOneDto.setResultCode("001"); // user not found
        }
        return;
    }

	// Change user info       
	// @Transactional(readOnly = false, rollbackFor = Exception.class)
	public void update(UserUpdateDto userUpdateDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());

		UserUpdateDto.RequestHd reqHd = userUpdateDto.getReqHd();
		PtUser ptUser = new PtUser();
		ptUser.setUserId(reqHd.getUserId());
		if (!reqHd.getPassword().trim().isEmpty()) {
			ptUser.setPassword(reqHd.getPassword());
		}
		if (! reqHd.getEmail().trim().isEmpty()) {
			ptUser.setEmail(reqHd.getEmail());
		}
		if (reqHd.getRole() != null) {
			ptUser.setRole(reqHd.getRole());
		}
		if (reqHd.getActive() != null) {
			ptUser.setActive(reqHd.getActive());
		}
		//Update user Info
//		int cnt = ptUserDao.update(ptUser);
//		if (cnt == 0) {
//			throw new OptimisticLockException("（Method ：update, Table Name：pt_user）");
//		}
		userUpdateDto.setResultCode("000");
		return;
	}

	//delete user (Soft delete)
	public void delete(UserDeleteDto userDeleteDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());

		UserDeleteDto.RequestHd reqHd = userDeleteDto.getReqHd();
		// user delete
		int cnt = ptUserDao.delete(reqHd.getEmail());
		if (cnt == 0) {
			throw new OptimisticLockException("delete pt_user");
		}
		userDeleteDto.setResultCode("000");
		return;
	}

	public void searchAllUsers(UserSearchManyDto userSearchManyDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());

		UserSearchManyDto.RequestHd reqHd = userSearchManyDto.getReqHd();
		List<PtUser> listPtUser = ptUserDao.findAll(reqHd.getActive());
		List<UserSearchManyDto.ResponseDt> listResDt = userSearchManyDto.getResDt();
        if (listPtUser != null) {
			for (Iterator<PtUser> it = listPtUser.iterator(); it.hasNext();) {
				PtUser ptUser = it.next();
				UserSearchManyDto.ResponseDt resDt = new UserSearchManyDto.ResponseDt();
				resDt.setUserId(ptUser.getUserId());
			    resDt.setFirstName(ptUser.getFirstName());
			    resDt.setLastName(ptUser.getLastName());
				resDt.setEmail(ptUser.getEmail());
				resDt.setRole(ptUser.getRole());
				resDt.setActive(ptUser.getActive());
				resDt.setCreatedAt(ptUser.getCreatedAt());
				resDt.setUpdatedAt(ptUser.getUpdatedAt());
				listResDt.add(resDt);
			}
        }
//		makeResponseTitle(userSearchAllDto);
		if (listResDt.size() > 0) {
			userSearchManyDto.setResultCode("000");
		} else {
			userSearchManyDto.setResultCode("001");
		}
		return;
	}

	
}