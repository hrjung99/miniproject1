package user;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.mysql.cj.xdevapi.Result;

import board.BoardVO;
import user.UserVO;

public class UserDAO {
	private static Connection conn = null;
	// user list
	private static PreparedStatement userListPsmt = null;
	// user delete
	private static PreparedStatement userDeletePsmt = null;
	// user view
	private static PreparedStatement userViewPsmt = null;
	// user insert
	private static PreparedStatement userInsertPsmt = null;
	// user update
	private static PreparedStatement userUpdatePsmt = null;
	
	//아이디 존재하는지 체크
	private static PreparedStatement userIdValidPsmt = null;
	
	//비밀번호 검증
	private static PreparedStatement userLoginPsmt = null;
	
	//최근 로그인/로그아웃 업데이트
	private static PreparedStatement userRecentLoginPsmt = null;
	private static PreparedStatement userRecentLogoutPsmt = null;
	
	//이름, 전화번호로 아이디 찾기
	private static PreparedStatement userFindIdPsmt = null;
	
	//아이디, 이름, 전화번호로 존재하는 계정인지 조회
	private static PreparedStatement userCheckForResetPassPsmt = null;
	//아이디, 이름, 전화번호로 비밀번호 변경
	private static PreparedStatement userResetPassPsmt = null;
	

	// 연결 및 쿼리
	static {
		try {

			// JDBC Driver 등록
			Class.forName("oracle.jdbc.OracleDriver");

			// 연결하기
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/xe", "MSA07", // user
					"1004" // password
			);
			// 자동 commit 끄기
			conn.setAutoCommit(false);

			
			// 페이징 아직
			userListPsmt = conn.prepareStatement("""
					SELECT * FROM TB_USER WHERE USER_DELETE_YN ='N'
					""");
			// user delete
			userDeletePsmt = conn.prepareStatement("""
					UPDATE TB_USER SET USER_DELETE_YN ='Y' WHERE USER_NO = ?
					""");
			
			
			// user view
			userViewPsmt = conn.prepareStatement("""
					SELECT * FROM TB_USER WHERE USER_NO = ?
					""");
			
			
			// user insert
			userInsertPsmt = conn.prepareStatement("""
					INSERT INTO TB_USER 
					(user_id, user_pass, user_name, user_phone, user_addr, user_sex)
					VALUES
					(?, ?, ?, ?, ?, ?)
					""");
			
			
			// user update
			userUpdatePsmt = conn.prepareStatement("""
					UPDATE TB_USER SET 
					user_pass = ?, 
					user_name = ?, 
					user_phone = ?, 
					user_addr = ?
					WHERE user_no = ?
					""");
			
			userLoginPsmt = conn.prepareStatement("""
					SELECT * FROM TB_USER WHERE USER_ID = ?
					AND USER_DELETE_YN='N'
					""");
			
			
			//아이디 존재하는지 체크
			userIdValidPsmt = conn.prepareStatement("""
					SELECT * FROM TB_USER WHERE TB_USER.USER_ID = ?
					AND USER_DELETE_YN='N'
					""");
			
			//최근 로그인/로그아웃 업데이트
			userRecentLoginPsmt = conn.prepareStatement("""
					UPDATE TB_USER
					SET user_login_recent = SYSDATE
					WHERE user_id = ?
					""");
			userRecentLogoutPsmt = conn.prepareStatement("""
					UPDATE TB_USER
					SET user_logout_recent = SYSDATE 
					WHERE user_no = ?
					""");
			
			
			//이름과 전화번호로 아이디 찾기
			userFindIdPsmt = conn.prepareStatement("""
					SELECT USER_ID FROM TB_USER WHERE USER_NAME = ? AND USER_PHONE= ?
					AND USER_DELETE_YN='N'
					""");
			
			//아이디, 이름, 전화번호로 해당 계정 존재 여부 확인
			userCheckForResetPassPsmt = conn.prepareStatement("""
					SELECT * FROM TB_USER
					WHERE USER_ID = ? AND USER_NAME = ? AND USER_PHONE = ?
					AND USER_DELETE_YN='N'
					""");
			
			//아이디, 이름, 전화번호로 비밀번호 변경
			userResetPassPsmt = conn.prepareStatement("""
					UPDATE TB_USER 
					SET USER_PASS = ?
					WHERE USER_ID = ? 
					AND USER_NAME = ? 
					AND USER_PHONE = ?
					WHERE USER_DELETE_YN='N'
					""");


		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
	}

	// user list
	public static List<UserVO> userList(UserVO userVO) {
		List userList = new ArrayList<UserVO>();

		try {
			ResultSet rs = userListPsmt.executeQuery();
			while(rs.next()) { // 행이 여러개니까 다음행 있으면 리스트에 담고 없으면 끝내는 코드
				UserVO user = new UserVO(
						rs.getInt("user_no")
						,rs.getString("user_id")
						,rs.getString("user_pass")
						,rs.getString("user_name")
						,rs.getString("user_phone")
						,rs.getString("user_addr")
						,rs.getString("user_sex")
						,rs.getString("user_role")
						,rs.getString("user_delete_YN")
						,rs.getString("user_login_recent")
						,rs.getString("user_logout_recent"));
				userList.add(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
		return userList;
	}


	// user delete
	public static String userDelete(int user_no) {
		int updated = 0;
		// 오류&성공 메세지 리턴을 위한 변수
		String message = "";

		try {
			System.out.println(user_no);
			System.out.println();
			userDeletePsmt.setInt(1, user_no);
			updated = userDeletePsmt.executeUpdate();
			System.out.println("updated = " + updated);
			if ( updated == 1) {
				message = "성공";
				conn.commit();
			} else {
				message = "실패";
			}

		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}

		return message;
	}

	// user view
	public static UserVO userView(String user_no) {
		UserVO user = null;
		try {
			userViewPsmt.setString(1, user_no);
			ResultSet rs = userViewPsmt.executeQuery();
			if(rs.next()) {
				user = new UserVO(
						rs.getInt("user_no")
						,rs.getString("user_id")
						,rs.getString("user_pass")
						,rs.getString("user_name")
						,rs.getString("user_phone")
						,rs.getString("user_addr")
						,rs.getString("user_sex")
						,rs.getString("user_role")
						,rs.getString("user_delete_YN")
						,rs.getString("user_login_recent")
						,rs.getString("user_logout_recent")
						);
			}
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
		return user;
	}

	// user insert
	public static String userInsert(UserVO userVO) {
		String message = "";
		int updated = 0;

		try {
			userInsertPsmt.setString(1,userVO.getUser_id());
			userInsertPsmt.setString(2, userVO.getUser_pass());
			userInsertPsmt.setString(3, userVO.getUser_name());
			userInsertPsmt.setString(4, userVO.getUser_phone());
			userInsertPsmt.setString(5, userVO.getUser_addr());
			userInsertPsmt.setString(6, userVO.getUser_sex());
			
			updated = userInsertPsmt.executeUpdate();
			
			if (updated == 1) {
				message = "성공";
				conn.commit();
			} else {
				message = "실패";
			}
			
			
			
		} catch(Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
		
		return message;
	}

	// user update
	public static void userUpdate(UserVO userVO) {
		String message = "";
		int updated = 0;

		try {
	        // PreparedStatement에 값 설정
	        userUpdatePsmt.setString(1, userVO.getUser_pass());
	        userUpdatePsmt.setString(2, userVO.getUser_name());
	        userUpdatePsmt.setString(3, userVO.getUser_phone());
	        userUpdatePsmt.setString(4, userVO.getUser_addr());
	        userUpdatePsmt.setInt(5, userVO.getUser_no());

	        // 쿼리 실행
	        updated = userUpdatePsmt.executeUpdate();

	        // 업데이트 결과 확인
	        if (updated == 1) {
	            message = "성공";
	            conn.commit();
	        } else {
	            message = "실패";
	        }

		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
	}
	
	//user id 존재하는지 확인
	public static boolean isIdAvailable(String user_id) {
		boolean isAvailable = false;
		
		try {
			userIdValidPsmt.setString(1, user_id);
			ResultSet rs = userIdValidPsmt.executeQuery();
			if (rs.next()) { // 결과가 있는지 확인
	            isAvailable = true; // 아이디가 존재함
	        } else {
	        	isAvailable = false;
	        } 
			rs.close();
		} catch(Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
		
		return isAvailable;
	}
	
	//로그인
	public static UserVO userLogin (String user_id) {
		UserVO userInfo = new UserVO();
		try {
			userLoginPsmt.setString(1, user_id);
			ResultSet rs = userLoginPsmt.executeQuery();
			
			if (rs.next()) {
				userInfo = new UserVO(
				rs.getInt("user_no"),
				rs.getString("user_id"),
				rs.getString("user_pass"),
				rs.getString("user_name"),
				rs.getString("user_phone"),
				rs.getString("user_addr"),
				rs.getString("user_sex"),
				rs.getString("user_role"),
				rs.getString("user_delete_YN"),
				rs.getString("user_login_recent"),
				rs.getString("user_logout_recent")
				);
			} else {
				userInfo = null;
			}
			rs.close();

		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
		
		return userInfo;
	}
	
	//로그인시 최근 로그인 update
	public static String userRecentLoginUpdate (String user_id) {
		String message = "";
		int updated = 0;
		try {
			userRecentLoginPsmt.setString(1,user_id);
			updated = userRecentLoginPsmt.executeUpdate();
			
			if (updated == 1) {
				conn.commit();
				message = "성공";
			} else {
				message = "실패";
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}

		return message;
	}
	
	//로그인시 최근 로그아웃 update
	public static String userRecentLogoutUpdate(UserVO userInfo) {
		String message = "";
		int updated = 0;
		try {
			userRecentLogoutPsmt.setInt(1,userInfo.getUser_no());
			updated = userRecentLogoutPsmt.executeUpdate();
			
			if (updated == 1) {
				conn.commit();
				message = "성공";
			} else {
				message = "실패";
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}

		return message;
	}
	
	// 아이디 찾기
	public static String userFindId(String user_name, String user_phone) {
		String user_id = "";

		try {
			userFindIdPsmt.setString(1, user_name);
			userFindIdPsmt.setString(2, user_phone);
			ResultSet rs = userFindIdPsmt.executeQuery();

			if (rs.next()) {
				user_id = rs.getString("user_id");
			} else {
				user_id = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
		return user_id;
	}

	// 비밀번호 초기화
	//1. 검증
	public static boolean checkUserForPasswordReset(String user_id, String user_name, String user_phone) {
		boolean check = false;
		try {
			userCheckForResetPassPsmt.setString(1, user_id);
			userCheckForResetPassPsmt.setString(2, user_name);
			userCheckForResetPassPsmt.setString(3, user_phone);
			
			ResultSet rs = userCheckForResetPassPsmt.executeQuery();
			if (rs.next()) {
				check = true;
			} else {
				check = false;
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
		return check;
	}
	//2. reset
	public static String userResetPass(String user_id, String user_name, String user_phone, String user_pass) {
		int updated = 0;
		String message ="";
		try {
			userResetPassPsmt.setString(1, user_pass);
			userResetPassPsmt.setString(2, user_id);
			userResetPassPsmt.setString(3, user_name);
			userResetPassPsmt.setString(4, user_phone);
			updated = userResetPassPsmt.executeUpdate();
			if (updated == 1) {
				message = "성공";
				conn.commit();
			} else {
				message = "실패";
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
		return message;
	}
}
