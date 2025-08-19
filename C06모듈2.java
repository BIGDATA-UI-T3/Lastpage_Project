package Ch08;

import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class C06모듈2 {
	// DB CONN DATA
	private static String id = "root";
	private static String pw = "1234";
	private static String url = "jdbc:mysql://localhost:3306/tmpdb";

	// JDBC참조변수
	private static Connection conn = null; // DBMS 의 특정 DB와 연결되는 객체
	private static PreparedStatement pstmt = null; // SQL Query 전송용 객체
	private static ResultSet rs = null; // Select 결과물 담을 객체

	public static void conn() throws Exception {
		// mysql download 홈페이지에서 mysql-connector를 다운받아서 홈페이지에서 DB와 Eclipse를 연결했었다 .
		// JDBC 드라이버를 JVM에 로드한다고 함.
		Class.forName("com.mysql.cj.jdbc.Driver");
		System.out.println("Driver Loading Success...");
		// DriverManager.getConnection()함수를 통해 DB와 접속을 할 수 있다. 접속할 url, 접속할 db사용자의 계정을
		// 입력하고 실행하면 접속성공!
		conn = DriverManager.getConnection(url, id, pw);
		System.out.println("DB CONNECTED...");
	}

//selectAll() 함수에서 SQL예외가 발생할 수 있다는것을 선언해두었다. 
	// 예외가 발생했을시 함수안에서 처리하지 않고 호출한 쪽으로 throw한다라는 의미를 갖는다.
	// 만약에 rs = pstmt.executeQuery()에서 예외가 발생했다고 하면 저 밑에 catch(SQLException e){} 에서
	// 예외처리를 하게 되는 것이다.
	// 이렇게하면 코드가 한결 보기 편해지고 selectAll()이 여러곳에서 호출될 시 상당히 귀찮아지기 때문에
	// throws로 예외를 던져버리면 코드를 재사용 할 때 유용하다.
	public static List<BookDto> selectAll() throws SQLException {
//conn.prepareStatement()함수를 사용해서 사용할 table명을 넣어주고
		// 테이블 읽기,수정,삭제,삽입을 준비하도록 하는 단계이다.
		pstmt = conn.prepareStatement("SELECT * FROM tbl_book");
		rs = pstmt.executeQuery();// 함수명 그대로 쿼리문을 실행하고 그 결과값(출력값)을 rs에 담는것.
		// List<BookDto> list = new ArrayList(); 딱 BookDto객체만 저장할 수 있는 List타입 변수 'list'를 만드는 것.
		
		// new라는 것은 동적할당을 의미.
		// 한마디로 Bookdto객체들을 담는 동적배열을 만든것.
		List<BookDto> list = new ArrayList();
		BookDto dto = null;// 이름 dto라는 객체를 null로 초기화 시켜주고
		if (rs != null) {// 객체의 실행결과값이 null이 아니라면
			while (rs.next()) {
				// 커서를 한 칸 밑으로 움직이고 행이 있으면 true를 없으면 false를 반환해라
				// 언제까지? 행이 존재하는동안까지

				dto = new BookDto();// 이름이 dto인 행을 저장해 둘 객체생성
				dto.setBookCode(rs.getLong("bookCode"));// dto객체에 bookcode의 값을 가져와서 저장
				dto.setBookName(rs.getString("bookName"));// dto객체에 bookName값을 가져와서 저장
				dto.setPublisher(rs.getString("publisher"));// dto객체에 publisher값을 가져와서 저장
				dto.setIsbn(rs.getString("isbn"));// dto객체에 isbn 값을 가져와서 저장
				list.add(dto);// 한 행이 만들어지고 그걸 list에 저장 이것을 조건안에서 계속 반복하다보면 1행,2행,3행--- 쌓여서 테이블이 만들어짐
//			
			}
		}
		return list;// 그 다음 이렇게 만들어진 list를 반환해 줌.
	}

//반환값이 BookDto객체인 select함수 long타입 인자값을 받아서 처리한다.
	public static BookDto select(Long bookCode) throws SQLException {
		// 위에서 selectall과 다르게 where조건절이 붙고
		// ? 에 bookCode값을 넣고서
		// 완성된 쿼리문을 실행한다.
		// 이 결과가 rs에 담김!!
		pstmt = conn.prepareStatement("SELECT * FROM tbl_book where bookCode=?");
		pstmt.setLong(1, bookCode);
		rs = pstmt.executeQuery();

		BookDto dto = null;
		if (rs != null) {// 포인터가 null값이 아니면
			rs.next();// 한 칸 내리고
			dto = new BookDto();
			dto.setBookCode(rs.getLong("bookCode"));
			dto.setBookName(rs.getString("bookName"));
			dto.setPublisher(rs.getString("publisher"));
			dto.setIsbn(rs.getString("isbn"));// 위와같이 빈 dto에 저장이 됨. [bookCode, bookName,publisher,isbn] 요렇게

		}
		return dto;// 만들어진 이름dto 객체가 반환됨. 한 행이 반환된다.
	}

//반환값이 int인 함수 insertBook BooKDto 객체를 받아서 처리한다.
	public static int insertBook(BookDto bookDto) throws SQLException {
//tbl_book 테이블에 값을 넣을건데 아직모르고 뒤에 명시하겠음!
		pstmt = conn.prepareStatement("insert into tbl_book values(?,?,?,?)");
		pstmt.setLong(1, bookDto.getBookCode());// 첫번째 자리에 bookDto.getBookCode() 넣기
		pstmt.setString(2, bookDto.getBookName());// 두번째 자리에 bookDto.getBookName() 넣기
		pstmt.setString(3, bookDto.getPublisher());// 세번째 자리에 bookDto.getPublisher()넣기
		pstmt.setString(4, bookDto.getIsbn());// 네번째 자리에 bookDto.getIsbn() 넣기

		int result = pstmt.executeUpdate();// 한 행이 실행성공 했다? 그럼 result값은 1, n개의 행이 실행 성공했다? 그러면 result값은 n

		return result;// 반환값 int이므로 int result 값은 잘 반환될 것.
	}

	public static int updateBook(BookDto bookDto) throws SQLException {
		// tbl_book 테이블을 수정할건데 어떤 값으로 수정할지는 아직 모르고 뒤에 명시하겠음!
		pstmt = conn.prepareStatement("update tbl_book set bookName=?, publisher=?, isbn=? where bookCode=?");

		pstmt.setString(1, bookDto.getBookName());// 첫번째 자리에 bookDto.getBookName() 넣기
		pstmt.setString(2, bookDto.getPublisher());// 두번째 자리에 bookDto.getPublisher() 넣기
		pstmt.setString(3, bookDto.getIsbn());// 세번째 자리에 bookDto.getIsbn() 넣기
		pstmt.setLong(4, bookDto.getBookCode());// 네번째 자리에 bookDto.getBookCode() 넣기
		//
		int result = pstmt.executeUpdate();// 한 행이 실행성공 했다? 그럼 result값은 1, n개의 행이 실행 성공했다? 그러면 result값은 n

		return result;// 반환값 int이므로 int result 값은 잘 반환될 것.
	}

//
	public static int deleteBook(BookDto bookDto) throws SQLException {
//tbl_book테이블에서 뭔가를 지울건데 아직 모르겠고 뒤에 명시하겠음!
		pstmt = conn.prepareStatement("delete from tbl_book where bookCode=?");
		pstmt.setLong(1, bookDto.getBookCode());// ?자리에 bookDto.getBookCode() 넣기

		//
		int result = pstmt.executeUpdate();// 한 행이 실행성공 했다? 그럼 result값은 1, n개의 행이 실행 성공했다? 그러면 result값은 n

		return result;// 반환값 int이므로 int result 값은 잘 반환될것.
	}

//메인함수
	public static void main(String[] args) {

//try 블록이다.

		try {
			// DBCONN
			conn();

			// TX START
			conn.setAutoCommit(false);
			// 기본적으로 자동커밋모드가 설정되어있기때문에 꺼주고 commit 호출 전까지 commit 안하고 임시 저장인 상태인 것
			// 이렇게 하면 rollback이 가능하다.

//			 INSERT
			// 값을 insert함.
			// insertBook이 'BookDto'타입을 인자값으로 받기때문에 객체를 하나 새로 만들어서 입력값을 주어야 한다!
			// 이렇게 하면 dto.setBookCode() ... 막 불필요하게 길어지는 걸 막을 수 있음.
			insertBook(new BookDto(1L, "도서명1", "출판사명1", "isbn-1"));
			insertBook(new BookDto(2L, "도서명2", "출판사명2", "isbn-2"));
			insertBook(new BookDto(3L, "도서명3", "출판사명3", "isbn-3"));

//			// SELECTALL
			// 이름이 allBook인 BookDto객체안에 selectAll()을 넣어준다.
			// selectAll()의 리턴값이 list였기 때문에 문제없이 돌아간다.
			List<BookDto> allBook = selectAll();
			System.out.println("SelectAll : ");
			allBook.forEach(System.out::println);// allBook객체를 하나하나 방문하면서 보고 출력하면 콘솔창에 나온대로 출력값이 나옴.

//			// SELECT
			BookDto dto = select(1L);// dto라는 객체에 첫번째 행을 select한 값 즉, 리스트를 담을 것이다.
			System.out.println("select : " + dto);
			// 그 값을 출력해서 보여주면
			// select : BookDto [bookCode=1, bookName=도서명1, publisher=출판사명1, isbn=isbn-1]
			// 출력값이나옴.
//			

			// UPDATE
//dto의 bookName과 Publisher명을 바꿀건데
			dto.setBookName("수정도서명-2");//bookName에 "수정도서명-2", publisher에 "수정출판사명-2"를 넣고
			dto.setPublisher("수정출판사명-2");
			int r1 = updateBook(dto);// dto의 주소가 넘어가서 위에 select한 행의 bookName과 Publisher명이 바뀌게 될것!
			if (r1 > 0) // r1의 값은 행을 하나 실행하였기에 1이고 0보다 크니까
				System.out.println("수정완료 : " + r1);// 수정완료 : 1 출력된다!

			// DELETE
			dto.setBookCode(2L);// 2L은 long타입 값 2를 의미.bookCode에 2를 넣고
			int r2 = deleteBook(dto);// r2의 값은 행을 하나 실행하였기에 1이고 0보다 크니까
			if (r2 > 0)
				System.out.println("삭제완료 : " + r2);// 삭제완료 : 1 출력된다!
//
//			//TX END
			conn.commit();// 이제 transaction을 commit해도 된다. 그럼 그제서야 DB에 반영이 됨.
		} catch (Exception e) {//위 try블럭 안에서 예외가 발생하면 실행된다!
			// TX ROLLBACKALL
			try {
				conn.rollback();// 예외가 발생하면 rollback을 할거고 롤백은 여태까지 한 작업을 모두 취소시킴.
			} catch (Exception e2) {//
			}
		} finally {//무조건! 실행되어야하는 블록
			//그래서 동적으로 할당된 메모리를 지울 때 사용하고, DB와의 연결을 끊을때도 쓴다.
			//순서는 마지막으로 할당한 자원 순서대로 닫는다!
			// 자원제거
			try {
				rs.close();//DB의 자원을 못쓰게되고, 참조도 못하게 됨.
			} catch (Exception e3) {
			}
			try {
				pstmt.close();//SQL 쿼리문 실행 못함.
			} catch (Exception e3) {
			}
			try {
				conn.close();//DB와의 연결 해제!
			} catch (Exception e3) {
			}

		}

	}

}