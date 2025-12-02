package co.jp.enon.tms.usermaintenance.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import co.jp.enon.tms.common.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class UserSearchOneDto extends BaseDto {
	// static final long serialVersionUIDが必要
	private static final long serialVersionUID = 1L;

	// 引数なしコンストラクタの定義
	public UserSearchOneDto() {
		reqHd = new RequestHd();
		resHd = new ResponseHd();
//		resDtTitle = new ResponseDtTitle();

		super.setTranId(this.getClass().getName());
	}

	// プロパティ(メンバ変数)の宣言
	private RequestHd reqHd;
	private ResponseHd resHd;

	private Object resHdTitle;
	private Object resDtTitle;

	@Data
	public static class RequestHd implements Serializable {
		// static final long serialVersionUIDが必要
		private static final long serialVersionUID = 1L;

		// 引数なしコンストラクタの定義
		public RequestHd() {
		}

		// プロパティ(メンバ変数)の宣言
		private String email;
		private String firstName;
		private String lastName;
	}

	@Data
	public static class ResponseHd implements Serializable {
		// static final long serialVersionUIDが必要
		private static final long serialVersionUID = 1L;

		// 引数なしコンストラクタの定義
		public ResponseHd() {
		}

		// プロパティ(メンバ変数)の宣言
		private Integer userId;
	    private String firstName;
	    private String lastName;
		private String email;
		private String password;
		private String resetPasswordToken;
		private byte role;
		private byte active;
		private LocalDateTime createdAt;
		private LocalDateTime updatedAt;
	}

//	@Data
//	public static class ResponseHdTitle implements Serializable {
//	    // static final long serialVersionUIDが必要
//	    private static final long serialVersionUID = 1L;
//
//	    // 引数なしコンストラクタの定義
//	    public ResponseHdTitle() {}
//
//		// プロパティ(メンバ変数)の宣言
//		private final String startDate = "開始日";
//		private final String endDate = "終了日";
//		private final String loginUser = "ログインユーザ";
//		private final String name = "氏名";
//		private final String password = "パスワード";
//		private final String email = "メールアドレス";
//		private final String updDatetime = "更新日時";
//	}
//	@Data
//	public static class ResponseDtTitle implements Serializable {
//	    // static final long serialVersionUIDが必要
//	    private static final long serialVersionUID = 1L;
//
//	    // 引数なしコンストラクタの定義
//	    public ResponseDtTitle() {}
//
//		// プロパティ(メンバ変数)の宣言
//		private final String roleId = "業務ID";
//		private final String roleName = "業務名";
//		private final String roleLevel = "業務レベル";
//		private final String roleLevelNameShort = "業務レベル名";
//		private final String userRoleStartDate = "ユーザ業務開始日";
//		private final String userRoleEndDate = "ユーザ業務終了日";
//		private final String updDatetime = "更新日時";
//	}
}
