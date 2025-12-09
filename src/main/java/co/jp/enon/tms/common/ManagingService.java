package co.jp.enon.tms.common;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
//import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

// Old Java EE imports
//import javax.servlet.http.HttpServletRequest;
//import javax.annotation.PostConstruct;

// New Jakarta EE imports
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import co.jp.enon.tms.common.exception.LogicalException;
import co.jp.enon.tms.common.exception.OptimisticLockException;
//import co.jp.enon.tms.namecollection.mapper.PvResultMessageMapper;
//import co.jp.enon.tms.namecollection.repository.PvResultMessageRepository;

@Component
@Aspect
public class ManagingService {

	private static final Logger consoleLogger = LoggerFactory.getLogger(ManagingService.class);
	private static final Logger serviceLogger = LoggerFactory.getLogger("ServiceLog");

	//constructor
	public ManagingService() {
	}

	private enum Direction {
		REQ("Request"),
		RES("Response"),
		REQHD("RequestHd"),
		RESHD("ResponseHd"),
		REQDT("$RequestDt"),	// Since RequestDt is a List, it is compared with AcualType, so add $
		RESDT("$ResponseDt");	// Since ResponseDt is a List, it is compared using AcualType, so add $.
		private final String str;
		private final String string;
		Direction(String string){
			this.str = string.substring(0, 3);
			this.string = string;
		}
	}

	@Autowired
	private PlatformTransactionManager tranMgr;

	//@Around("execution(* *..*Service.*(..))")
	// The first * in the execution argument is the return value, the next is the class name and method name,
	// and the () are the method arguments. ".." specifies not to specify explicitly.
	@Around("execution(* co.jp.enon.tms.*..*Service.*(..))")
	public Object aroundLog(ProceedingJoinPoint jp) throws Throwable {
		Object ret = null;
		//try {

        //get Class
        //Class<? extends MethodSignature> cls = ((org.aspectj.lang.reflect.MethodSignature)jp.getSignature()).getClass();
	    //logger.info("Class : " + cls.toString());
        //get method
        //java.lang.reflect.Method method  = ((org.aspectj.lang.reflect.MethodSignature)jp.getSignature()).getMethod();
        //logger.info("Method : " + method.toString());

        Integer retryCount = 0;	boolean retryDeadLock;

        do {

        	DefaultTransactionDefinition tranDef = new DefaultTransactionDefinition();
    		TransactionStatus tranSts = tranMgr.getTransaction(tranDef);
    		LocalDateTime reqDatetime = LocalDateTime.now();
    		//MutableBoolean loginUserService = new MutableBoolean(false);
    		MutableBoolean notInheritBaseDto = new MutableBoolean(false);

    		retryDeadLock = false;

			try {

        		//Get arguments and output them to the log
				serviceLogger.info("{} : {} : {}", jp.getSignature(), Direction.REQ.str, getArgStr(jp, Direction.REQ, reqDatetime, notInheritBaseDto));

        		ret = jp.proceed();

        		if (notInheritBaseDto.getValue()) {
        			tranMgr.rollback(tranSts);
        		} else {
	        		String resultCode = getRsultCodeAndSetResultMessage(jp);
	        		if (resultCode.equals("000")) {
	        			tranMgr.commit(tranSts);
	        		} else {
	        			//throw new LogicalException();
	            		tranMgr.rollback(tranSts);
	        		}
        		}

        	} catch (SQLException sqlex) {
        	    if ("40001".equals((sqlex).getSQLState())) {
        	    	// デッドロックは、DeadlockLoserDataAccessExceptionが発生することは確認したが、SQLExceptionが発生しSQLStateが40001になるケースは未確認である。
            		//exceptionLogger.error
        			serviceLogger.info("Dead Lock SQLException ! sqlex.getClass().getSimpleName() = {}, retryCount = {}, sqlex.toString() = {}, sqlex.getStackTrace()[0].toString() = {}",
        					sqlex.getClass().getSimpleName(), retryCount, sqlex.toString(), sqlex.getStackTrace()[0].toString());
        			consoleLogger.info("\njp.getSignature() ==> {} : Dead Lock SQLException ! sqlex.getClass().getSimpleName() = {}, retryCount = {}, sqlex.toString() = {}, sqlex.getStackTrace()[0].toString() = {}",
        					jp.getSignature(), sqlex.getClass().getSimpleName(), retryCount, sqlex.toString(), sqlex.getStackTrace()[0].toString());

        	        // デッドロックが発生すると、DB側でロールバックされているはずだが、DeadlockLoserDataAccessExceptionが発生したときに
        			// rollbackを実行しないとリトライ処理がオートコミットになってしまうのを確認したので、次のrollbackを実行する。
            		tranMgr.rollback(tranSts);
	        		if (retryCount++ < 3) {
	        			retryDeadLock = true;
	        			setRsultToDto(jp, "910", sqlex.getMessage());
	        	    	Thread.sleep(1000);		// 1秒待機
	        		} else {
	        			retryDeadLock = false;
	        			setRsultToDto(jp, "900", sqlex.getMessage());
	        			//setExceptionToDto(jp, sqlex);
	        	    	//throw sqlex;
	        		}
        	    } else {
            		tranMgr.rollback(tranSts);
        	        //exceptionLogger.error("\njp.getSignature() => {}\nex.toString() => {}\nex.getStackTrace()[0].toString() => {}",
	        		//		jp.getSignature(), sqlex.toString(), sqlex.getStackTrace()[0].toString());
            		consoleLogger.error("\njp.getSignature() => {}\nex.toString() => {}\nex.getStackTrace()[0].toString() => {}",
	        				jp.getSignature(), sqlex.toString(), sqlex.getStackTrace()[0].toString());
        	    	// デッドロック以外SQLException
        			//setRsultToDto(jp, "", sqlex.getMessage());
        			setExceptionToDto(jp, sqlex);
        	    	retryDeadLock = false;
        	    	//throw sqlex;
        	    }
        	} catch (PessimisticLockingFailureException deadlockEx) {
        		//exceptionLogger.error
        		serviceLogger.info("DeadlockLoserDataAccessException ! deadlockEx.getClass().getSimpleName() = {}, retryCount = {}, deadlockEx.toString() = {}, deadlockEx.getStackTrace()[0].toString() = {}",
        				deadlockEx.getClass().getSimpleName(), retryCount, deadlockEx.toString(), deadlockEx.getStackTrace()[0].toString());
        		consoleLogger.info("\njp.getSignature() ==> {} : DeadlockLoserDataAccessException ! deadlockEx.getClass().getSimpleName() = {}, retryCount = {}, deadlockEx.toString() = {}, deadlockEx.getStackTrace()[0].toString() = {}",
    					jp.getSignature(), deadlockEx.getClass().getSimpleName(), retryCount, deadlockEx.toString(), deadlockEx.getStackTrace()[0].toString());
    	        // デッドロックが発生すると、DB側でロールバックされているはずだが、次のrollbackを実行しないとリトライ処理がオートコミットになってしまう。
        		tranMgr.rollback(tranSts);
        		if (retryCount++ < 3) {
        			retryDeadLock = true;
        			setRsultToDto(jp, "910", deadlockEx.getMessage());
        	    	Thread.sleep(1000);		// 1秒待機
        		} else {
        			retryDeadLock = false;
        			setRsultToDto(jp, "900", deadlockEx.getMessage());
        			//setExceptionToDto(jp, deadlockEx);
        	    	//throw sqlex;
        		}
        	} catch (OptimisticLockException optLockEx) {
        		tranMgr.rollback(tranSts);
        		setRsultToDto(jp, "901", optLockEx.getMessage());
        	} catch (LogicalException logicalEx) {
        		tranMgr.rollback(tranSts);
        	} catch (Exception ex) {
        		tranMgr.rollback(tranSts);
        		//logger.error("{} : {}", ex.toString(), ex.getMessage());
    	        //exceptionLogger.error("\njp.getSignature() => {}\nex.toString() => {}\nex.getStackTrace()[0].toString() => {}",
        		//		jp.getSignature(), ex.toString(), ex.getStackTrace()[0].toString());
        		consoleLogger.error("\njp.getSignature() => {}\nex.toString() => {}\nex.getStackTrace()[0].toString() => {}",
    	        				jp.getSignature(), ex.toString(), ex.getStackTrace()[0].toString());
        		boolean result = setExceptionToDto(jp, ex);
        		if (result == false) {
        			// BaseDto の ResultCode、ResultMessge に例外情報をセットできなかったときは例外をスローする
        			// 例外をスローすると、co.jp.arche1.kpms.common.security.dto.ErrorController.handleException()で捕捉して、
        			// @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500　がクライアントに返される。
        			throw ex;
        		}
        	} finally {
        		//引数を取得しログに出力
        		serviceLogger.info("{} : {} : {}", jp.getSignature(), Direction.RES.str, getArgStr(jp, Direction.RES, reqDatetime, notInheritBaseDto));
        	}
        } while (retryDeadLock == true);

			//logger.debug("AfterReturning by @Around : {} ret:{}", jp.getSignature(), ret);

		//} catch (Throwable t) {
		//	logger.debug("AfterThrowing by @Around  : {}", jp.getSignature(), t);
		//	throw t;
		//} finally {
		//	//logger.debug("After by @Around          : {}", jp.getSignature());
		//}
		return ret;
	}

    /**
     * 指定したメソッドの引数の文字列を取得する
     *
     * @param jp 横断的な処理を挿入する場所
     * @param direction Request、Responseを表す
     * @return 指定したメソッドの引数
     */
    private String getArgStr(JoinPoint jp, Direction direction, LocalDateTime reqDatetime, MutableBoolean notInheritBaseDto) {
        StringBuilder sb = new StringBuilder();
        Object[] args = jp.getArgs();

        if(args.length > 0){
	        if (args[0] instanceof BaseDto) {
	        	BaseDto baseDto = (BaseDto)args[0];
	        	if (direction.equals(Direction.REQ)) {
	        		// サービス実行前の要求時刻、ユーザ、端末の設定
	        		baseDto.setReqDatetime(reqDatetime);
	        		// スレッドローカルオブジェクト(SecurityContextHolder)から認証情報を取り出す
	        		SecurityContext ctx = SecurityContextHolder.getContext();
	                if (null != ctx) {
	                	String user = ctx.getAuthentication().getName();
	                    //System.out.println(user);
	                    baseDto.setUser(user);
	                }
	        		// スレッドローカルオブジェクト(RequestContextHolder)からリクエスト情報を取り出す
	                HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	                baseDto.setTerminal(req.getRemoteAddr());
	        	}
	        	// サービス実行前後の端末、ユーザ、要求時刻の出力準備
	        	sb.append("Terminal=").append(baseDto.getTerminal());
	        	sb.append(", User=").append(baseDto.getUser());
	        	sb.append(", ReqDatetime=").append(baseDto.getReqDatetime()).append(", ");
	        	if (direction.equals(Direction.RES)) {
	        		// サービス実行後の応答時刻、要求からの経過時間、結果コード、結果メッセージの出力準備
	        		baseDto.setResDatetime(LocalDateTime.now());
		        	sb.append("ResDatetime=").append(baseDto.getResDatetime());
		        	sb.append(", ElapsedTime=").append(baseDto.getElapsedTime());
		        	sb.append(", ResultCode=").append(baseDto.getResultCode());
		        	sb.append(", ResultMessage=").append(baseDto.getResultMessage()).append(", ");
	        	}

	            for(Object arg : args) {
	                for (Field field : arg.getClass().getDeclaredFields()) {
	                //for (Field field : arg.getClass().getFields()) {
	                    try {
	                        field.setAccessible(true);
	                        String instanceName = field.getName();
	                        String typeName = field.getType().getSimpleName();
	                        //String sname = field.get(arg).getClass().getSimpleName();
	                        //System.out.println("sname=" + typeName + ", instanceName=" + instanceName);
	                    	if (direction.equals(Direction.REQ)) {
	                    		// サービス実行前
	                            if (typeName.startsWith(Direction.REQ.string)) {
	                            	// オブジェクトの型がRequest
	                        		sb.append(instanceName).append("=").append(field.get(arg)).append(", ");
	                            } else if (typeName.equals(List.class.getSimpleName())) {
	                            	// オブジェクトの型がList
	                            	List<?> objList = (List<?>)field.get(arg);

	                            	if (objList.isEmpty() == false) {
		                            	ParameterizedType paramType = (ParameterizedType)field.getGenericType();
		                            	Type actualType = paramType.getActualTypeArguments()[0];
		                            	if (actualType.toString().lastIndexOf(Direction.REQDT.string) >= 0) {
		                            		sb.append(instanceName).append("=").append(field.get(arg)).append(", ");
		                            	}
	                            	}

	                            	//Type type = field.getGenericType();
	                            	//System.out.println(type + " : " + type.getClass());

	                        		//type = ((GenericArrayType)type).getGenericComponentType();
	                        		//System.out.println(type + " : " + type.getClass());

	                            }
	                    	} else if(direction.equals(Direction.RES)) {
	                    		// サービス実行後
	                            if (typeName.startsWith(Direction.RES.string)) {
		                            if (typeName.startsWith(Direction.RESHD.string)) {
	        	                        sb.append(instanceName).append("=").append(field.get(arg)).append(", ");
		                            } else {
		                            	// オブジェクトの型がResponseDtの場合（通常、ResponseDtのtypeNameはListになるのでこの処理は行わない）
	                                	if (serviceLogger.isDebugEnabled()) {
	                                		// デバッグモードならオブジェクトの内容の出力準備
		        	                        sb.append(instanceName).append("=").append(field.get(arg)).append(", ");
	                                	} else {
		                            		// デバッグモードでないならオブジェクトの内容を省略して出力準備
	                                		String strField = field.get(arg).toString();
	                                		if (strField.length() > 128) {
	                                			strField = strField.substring(0,128);
	                                		}
		    		                        sb.append(instanceName).append(strField).append("...)]").append(", ");
	                                	}
		                            }
                            	} else if (typeName.equals(List.class.getSimpleName())) {
	                            	// オブジェクトの型がListの場合（通常、ResponseDtはtypeNameはListになるのでResponseDtでこの処理を行う）
	                            	List<?> objList = (List<?>)field.get(arg);

	                            	if (objList.isEmpty() == false) {
		                            	ParameterizedType paramType = (ParameterizedType)field.getGenericType();
		                            	Type actualType = paramType.getActualTypeArguments()[0];
		                            	if (actualType.toString().lastIndexOf(Direction.RESDT.string) >= 0) {
		                                	if (serviceLogger.isDebugEnabled()) {
		                                		sb.append(instanceName).append("=").append(field.get(arg)).append(", ");
		                                	} else {
			                            		// デバッグモードでないならオブジェクトの内容を省略して出力準備
		                                		String strField = field.get(arg).toString();
		                                		if (strField.length() > 128) {
		                                			strField = strField.substring(0,128);
		                                		}
			    		                        sb.append(instanceName).append(strField).append("...)]").append(", ");
		                                	}
		                            	}
	                            	}
		                        }
	                    	}
	                    } catch (IllegalAccessException e) {
	                        sb.append(field.getName()).append("=Access Denied").append(", ");
	                    }
	                }
	            }

	        } else {
	        	notInheritBaseDto.setValue(true);
	        	// If args[0] is not of type BaseDto, it is considered to be LoginUserService.
        		// Authentication information cannot be obtained from the thread local object (SecurityContextHolder) because it is not yet logged in.
        		//SecurityContext ctx = SecurityContextHolder.getContext();
        		//String user = "";
                //if (null != ctx) {
                //	if (ctx.getAuthentication() != null) {
                //		user = ctx.getAuthentication().getName();
                //    //System.out.println(user);
                //}
	        	// The login name is in args[0] of LoginUserService, so use this
                String user = args[0].toString();
        		// Retrieve request information from the thread local object (RequestContextHolder)
                HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

	        	//String classSimpleName = jp.getTarget().getClass().getSimpleName();
	        	//if ( classSimpleName.equals("LoginUserService")) {
	        	//	System.out.println("classSimpleName=" + classSimpleName + ", direction=" + direction.toString());
	        	//	if (direction.equals(Direction.REQ)) {
	        	//		String commonName = getCert(req);
	        	//		System.out.println("classSimpleName=" + classSimpleName + ", direction=" + direction.toString() + ", commonName=" + commonName);
	        	//	}
	        	//}

	        	// Preparing to output terminal, user, and requested time before and after service execution
	        	sb.append("Terminal=").append(req.getRemoteAddr());
	        	sb.append(", User=").append(user);
	        	sb.append(", ReqDatetime=").append(reqDatetime).append(", ");

	        	if (direction.equals(Direction.RES)) {
	        		//Prepare to output response time after service execution, elapsed time from request, result code, and result message
	        		LocalDateTime resDatetime= LocalDateTime.now();
		        	sb.append("ResDatetime=").append(resDatetime);

		        	java.time.Duration duration = java.time.Duration.between(reqDatetime, resDatetime);
		    		//Add Duration to 0:00
		    		java.time.LocalTime t = java.time.LocalTime.MIDNIGHT.plus(duration);
		    		String s = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS").format(t);
		        	sb.append(", ElapsedTime=").append(s).append(", ");
		        	//sb.append(", ResultCode=").append(baseDto.getResultCode());
		        	//sb.append(", ResultMessage=").append(baseDto.getResultMessage()).append(", ");
	        	}
	        }

            if (sb.length() >= 2) {
            	if (sb.substring(sb.length() - 2, sb.length()).equals(", ")) {
            		sb.delete(sb.length() - 2, sb.length());
            	}
            }

        }else{
            sb.append("(引数なし)");
        }
        return sb.toString();
    }

    // Set the result message obtained from the result code of Dto in Dto
    private String getRsultCodeAndSetResultMessage(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String resultCode = "";

        if(args.length > 0){
	        if (args[0] instanceof BaseDto) {
	        	BaseDto baseDto = (BaseDto)args[0];
	        	resultCode = baseDto.getResultCode();
	        	 // Only proceed if resultCode is present
	            if (StringUtils.isNotEmpty(resultCode)) {
	                // If DTO already has a more descriptive message, keep it (do not overwrite).
	                if (StringUtils.isNotEmpty(baseDto.getResultMessage())) {
	                    // keep existing resultMessage
	                } else {
	                    // Try to get a message from repository
	                    String messageFromRepo = getResultMessageRepository(args[0].getClass().getSimpleName(), resultCode);
	                    if (StringUtils.isNotEmpty(messageFromRepo)) {
	                        baseDto.setResultMessage(messageFromRepo);
	                    } else {
	                        // do not set resultMessage to resultCode here; leave it blank
	                        // (fallback to setRsultToDto logic when needed)
	                    }
	                }
	            }
	        }
        }
        return resultCode;
    }
    
    private String getResultMessageRepository(String dtoName, String resultCode) {
        // TODO: wire your mapper or repository here (uncomment & implement)
        // Example:
        // return pvResultMessageMapper.selectOne(dtoName, resultCode)
        //         .map(PvResultMessageRepository::getMsgNameLong)
        //         .orElse("");
        //
        // Temporary safe stub: return empty string if not found
        return "";
    }
    
    private void setRsultToDto(JoinPoint jp, String resultCode, String exMessage) {
        Object[] args = jp.getArgs();

        if (args.length == 0) return;

        if (!(args[0] instanceof BaseDto)) return;

        BaseDto baseDto = (BaseDto) args[0];

        // only set resultCode if provided
        if (StringUtils.isNotEmpty(resultCode)) {
            baseDto.setResultCode(resultCode);
        }

        String dtoName = args[0].getClass().getSimpleName();

        // 1) Try repo lookup first (useful for human friendly messages)
        String repoMessage = StringUtils.isNotEmpty(resultCode)
                ? getResultMessageRepository(dtoName, resultCode)
                : "";

        String finalMessage = null;

        // Priority 1: explicit exception/custom message
        if (StringUtils.isNotEmpty(exMessage)) {
            // If repo message exists and you want to include exMessage into it, you can format:
            if (StringUtils.isNotEmpty(repoMessage)) {
                // If you have placeholders in repoMessage, you can format like below.
                // Uncomment MessageFormat import and next line if you want formatting:
                // finalMessage = MessageFormat.format(repoMessage, exMessage);
                // For safety (no formatting), prefer to append or choose exMessage directly:
                finalMessage = repoMessage + " : " + exMessage;
            } else {
                finalMessage = exMessage;
            }
        }
        // Priority 2: repo message (no exMessage)
        else if (StringUtils.isNotEmpty(repoMessage)) {
            finalMessage = repoMessage;
        }
        // Priority 3: keep any existing message on DTO
        else if (StringUtils.isNotEmpty(baseDto.getResultMessage())) {
            finalMessage = baseDto.getResultMessage();
        }
        // Priority 4: last resort — set resultCode as message (avoid if possible)
        else if (StringUtils.isNotEmpty(resultCode)) {
            finalMessage = resultCode;
        }

        baseDto.setResultMessage(finalMessage);
    }

    // Add the exception message of the argument to the result message obtained from the result code of the argument and set it in the Dto
//    private void setRsultToDto(JoinPoint jp, String resultCode, String exMessage) {
//        Object[] args = jp.getArgs();
//
//        if(args.length > 0){
//	        if (args[0] instanceof BaseDto) {
//	        	BaseDto baseDto = (BaseDto)args[0];
//	        	if (StringUtils.isNotEmpty(resultCode)) {
//		        	baseDto.setResultCode(resultCode);
//	        	} 	
//	        	 String finalMessage = null;
//
//	             // Priority 1: exMessage (actual exception message or custom message)
//	             if (StringUtils.isNotEmpty(exMessage)) {
//	                 finalMessage = exMessage;
//	             }
//	             else if (StringUtils.isNotEmpty(resultCode)) {
//	                 // Priority 2: lookup message from repository
//	                 finalMessage = resultCode;
//	             }
//
//	             baseDto.setResultMessage(finalMessage);
//	        }
//        }
//    }
//    
   

//	@Autowired
//	PvResultMessageMapper pvResultMessageMapper;
//
//	/ /Return the name collection result message from the result code
//    private String getResultMessageRepository(String dtoName, String resultCode) {
//    	String nameLong = "";
//
//    	try {
//    		PvResultMessageRepository pvResultMessageRepository = pvResultMessageMapper.selectOne(dtoName, resultCode);
//
//	    	if (pvResultMessageRepository != null) {
//	    		nameLong = pvResultMessageRepository.getMsgNameLong();
//		    	if (nameLong == null) {
//		    		nameLong = "";
//		    	}
//	    	}
//    	} catch (Exception ex) {
//    		nameLong = ex.toString();
//    	}
//    	return nameLong;
//    }
	// Set the result code and result message from the exception
    private boolean setExceptionToDto(JoinPoint jp, Exception ex) {

        boolean result = false;
    	Object[] args = jp.getArgs();

		String exMessage = String.format("%s, %s", ex.toString(), ex.getStackTrace()[0].toString());
        if(args.length > 0){
	        if (args[0] instanceof BaseDto) {
	        	BaseDto baseDto = (BaseDto)args[0];

	        	baseDto.setResultCode("Exception");
	        	baseDto.setResultMessage(exMessage);
	        	result = true;
	        }
        }
    	return result;
    }

}
