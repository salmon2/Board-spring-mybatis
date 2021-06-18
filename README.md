---
layout: post
title: springMVC + mybatis 게시판 만들기
category: spring
tags: [spring, board]
---
## 목차

---
- 기술스택
- 프로젝트 생성하기
- DB 연결하기
- mybatis 연결하기
- 게시글 리스트 보기(R)
- 게시글 자세히 보기(R)
- 게시글 생성하기(C)
- 게시글 수정하기(U)
- 게시글 삭제하기(D)
---
## 기술 스택

---
개발환경 : IntelliJ
빌드 : Gradle
애플리케이션 프레임워크 : Spring
탬플릿 : Thymeleaf
ORM : Mybtis
DB : H2-Database

---
## 프로젝트 생성하기

---
![그림](https://user-images.githubusercontent.com/23234577/122560839-10203380-d07c-11eb-8e9e-1426b906e61a.png)

Spring Initializr를 이용하여 쉽게 관련된 라이브러리들을 설치할 수 있다.

---
## DB 연결하기

---
### application.properties
```java
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```
h2 db의 url과, username, password, driver를 설정해준다.

### schema.sql
db연결을 확인해야함으로 초기 더미데이터를 넣어 확인한다.
```resources/schema.sql``` 파일을 만들고 아래와 같이 query문을 작성한다.

```
DROP TABLE IF EXISTS tbl_board;
CREATE TABLE tbl_board(
    boardId Long auto_increment,
    title varchar (30) not null,
    content varchar (30) not null,
    name varchar (30) not null,
    read integer default 0,
    primary key(boardId)
);

INSERT INTO tbl_board(title, content, name) VALUES('title1', 'content1', 'name1');
INSERT INTO tbl_board(title, content, name) VALUES('title2', 'content2', 'name2');
INSERT INTO tbl_board(title, content, name) VALUES('title3', 'content3', 'name3');
INSERT INTO tbl_board(title, content, name) VALUES('title4', 'content4', 'name4');
INSERT INTO tbl_board(title, content, name) VALUES('title5', 'content5', 'name5');
INSERT INTO tbl_board(title, content, name) VALUES('title6', 'content6', 'name6');
INSERT INTO tbl_board(title, content, name) VALUES('title7', 'content7', 'name7');
INSERT INTO tbl_board(title, content, name) VALUES('title8', 'content8', 'name8');
```
![그림](https://user-images.githubusercontent.com/23234577/122560819-0bf41600-d07c-11eb-9c49-e0b1f089c811.png)

---
## mybatis 연결하기

---
### Board.class 생성
보통은 entity는 entity대로 생성하고 db접근은 dao 또는 dto를 사용하지만 이 프로젝트는 entity그대로를 사용한다.
```java
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    private Long boardId;
    private String title;
    private String content;
    private LocalDateTime createDate;
    private Integer read;
    private String name;

    private Long memberId;


    public Board(String title, String content, String name) {
        this.title = title;
        this.content = content;
        this.name = name;
    }

}
```
게시글의 필요한 기능들과 lombok을 활용하여 getter, setter, 생성자들을 구현하였다.

### BoardMapper
com/example/blog_board/mapper/BoardMapper.java 파일 생성
Board클래스와 DB의 tbl_board을 연결해준다. Mapper 안에는 tbl_board와 관련된 select, insert, update, delete문과 같은 query문이 맵핑된다.

```java
@Repository
public interface BoardMapper{

    int boardCount();

    List<Board> findAll();
}
```
@Repository를 통해 스프링 bean에 등록
mybatis같은 경우 함수에 대응하는 query문을 작성하여 맵핑해야한다.
맵핑한 query 문은 resources/com/example/blog_board/mapper/BoardMapper.xml 파일을 만들고 맵핑한다. 
이름을 봐서 알겠지만 위의 BoardMapper와 resource파일 하위에 위치와 이름이 같아야한다.

```
repository : com/example/blog_board/mapper/BoardMapper.java
xml : resources/com/example/blog_board/mapper/BoardMapper.xml
```

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.blog_board.mapper.BoardMapper">

    <select id="boardCount" resultType="int">
        SELECT count(BoardId) AS cbt FROM tbl_board;
    </select>

    <select id="findAll" resultType="com.example.blog_board.domain.Board">
        SELECT
        *
        FROM tbl_board;
    </select>
</mapper>
```
id 는 BoardMapper.interface의 함수명을 동일하게 맞춰주고 리턴 타입이 만약 class라면 경로 또한 적어주어 맵핑한다.

id : BoardMapper.interface 의 대응되는 함수와 매칭
resultType : class라면 전체 이름을 적어주기, 원시타입이라면 그냥 적어도된다. ex) resultType="int"
parameterTyp : 맵핑한 함수의 파라미터의 타입명을 매칭
ex) parameterTyp="Long"

---
## controller와 service 그리고 View

---
### BoardController
모든 사용자는 일단 Controller로 들어오고, 들어온 url에 따라서 기능이 사용된다.
```java
@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/hello")
    public String Hello(){
        return "/board/hello";
    }

    @GetMapping("/test")
    public String test(Model model){
        model.addAttribute("cnt", boardService.boardCount());
        model.addAttribute("test", boardService.boardList());

        return "/board/hello";
    }

    @GetMapping
    public String main(Model model){
        model.addAttribute("boards", boardService.boardList());

        return "/board/boards";
    }
}
```
```@Controller``` : spring의 controller 계층을 암시, bean을 생성한다.
```@RequestMapping("/boards")``` : 하위 url은 /boards + 로 시작된다.

```@RequiredArgsConstructor``` : ```private final BoardService boardService;``` spring DI를 이용하기 위해서 사용, 자동으로 BoardService interface에 적절한 Bean이 주입된다.

```java
    @GetMapping("/test")
    public String test(Model model){
        model.addAttribute("cnt", boardService.boardCount());
        model.addAttribute("test", boardService.boardList());

        return "/board/hello";
    }
```
```return "/board/hello"``` : resources/templates/board/hello.html 파일로 이동된다.
```model.addAttribute("key", value)``` : 이동 시 "key", "value"를 통해 파라미터로 데이터를 들고 갈 수 있다.

```java
<!DOCTYPE html>
<html lang ="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <link   th:href="@{/css/bootstrap.min.css}"
            href="../css/bootstrap.min.css" rel="stylesheet">
    <title>테스트 페이지</title>
</head>
<body>
    <h1>
        테스트 페이지!
    </h1>

    <h1>
        [[${cnt}]]
    </h1>
    <h1>
        [[${test}]]
    </h1>
</body>
</html>
```
```model.addAttribute("cnt", value)``` 를 통해서 가져온 cnt 변수를 `[[$cnt]]`로 화면에 출력할 수 있다. `[[$]]`는 타임리프 문법이다.

![그림](https://user-images.githubusercontent.com/23234577/122560822-0d254300-d07c-11eb-9f2b-742c4ddba14e.png)

### service

서비스 계층은 보통 리포지토리 계층에서 불러온 데이터에 추가적인 논리작업을 시행한다. 그러나 이 프로젝트에서는 간단한 CRUD만을 작업한다.
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardMapper boardMapper;

    public int boardCount(){
        return boardMapper.boardCount();
    }
    public List<Board> boardList(){
        return boardMapper.findAll();
    }

    public Board findById(Long boardId){
        return boardMapper.findById(boardId);
    }

    @Transactional
    public Long add(Board board) {
        boardMapper.save(board);
        return board.getBoardId();
    }

    @Transactional
    public Long update(Board board){
        return boardMapper.update(board);
    }

    public void deleteById(Long boardId) {
        boardMapper.delete(boardId);
    }
}
```
서비스 게층을 뜻하는 @Service 어노테이션을 달며, 기본적으로 모든 함수는 Transcatioanl을 read로 사용하고 db에 저장하는 함수는
각각 따로 false로 달아준다.


![그림](https://user-images.githubusercontent.com/23234577/122560824-0dbdd980-d07c-11eb-8613-95b80615eebf.jpg)

controller는 servcie계층을 가지며 service계층은 repository(dao, mapper)계층을 가지고 있다.
모든 연결은 controller에서부터 시작하며 controller에서 service로 데이터를 요청하고 요청한 데이터를 필두로
동적으로 페이지를 작성한다.

---
## 게시글 리스트 보기

---
### controller
하위 문서의 uri는 기본적으로 ("/boards")를 포함한다.

```java
@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    public String main(Model model){
        model.addAttribute("boards", boardService.boardList());

        return "/board/boards";
    }
}


```
Get boards url이 들어온다면 boardService의 boardList 함수를 실행하고 리턴값을 boards라는 key값을 담은 뒤 resources/templete/board/boards.html 파일을 실행한다.
### view
#### boards.html
```html
 <div>
        <table class="table">
            <thead>
            <tr>
                <th>ID</th>
                <th>글쓴이</th>
                <th>제목</th>
                <th>조회수</th>
            </tr>
            </thead>
            <tbody>
            <tr th:each="board : ${boards}">
                <td><a href="board.html" th:href="@{/boards/{boardId}(boardId=${board.boardId})}" th:text="${board.boardId}">게시글 아이디</a></td>
                <td><a href="board.html" th:href="@{|/boards/${board.boardId}|}" th:text="${board.name}">글쓴이</a></td>
                <td th:text="${board.title}">제목</td>
                <td th:text="${board.read}">1</td>
            </tr>
            </tbody>
        </table>
    </div>
```
키값으로 받은 boards를 for each 문을 통하여 반복적으로 목록을 불러온다.



### Servcie
#### BoardService
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardMapper boardMapper;

    public List<Board> boardList(){
        return boardMapper.findAll();
    }
}

```

### Repostory
#### BoardMapper.interface
```java
    List<Board> findAll();
```

#### boardMapper.xml
```xml
<mapper namespace="com.example.blog_board.mapper.BoardMapper">
    <select id="findAll" resultType="com.example.blog_board.domain.Board">
        SELECT
        *
        FROM tbl_board;
    </select>
</mapper>
```

![그림](https://user-images.githubusercontent.com/23234577/122560827-0e567000-d07c-11eb-93c8-1287046a57ab.png)


---
## 게시글 자세히 보기(R)

---
### controller
#### BoardController.java
```java
@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/{boardId}")
    public String board(@PathVariable long boardId, Model model){
        model.addAttribute("board", boardService.findById(boardId));

        return "/board/board";
    }
}

```
### view
```xml
<div class="container">
    <div class="py-5 text-center">
        <h2>상품 상세</h2>
    </div>

    <h2 th:if="${param.status}" th:text="'저장 완료!'"></h2>


    <div>
        <label for="boardId">게시판 ID</label>
        <input type="text" id="boardId" name="boardId" class="form-control"
               value="1" th:value="${board.boardId}" readonly>
    </div>

    <div>
        <label for="name">작성자</label>
        <input type="text" id="name" name="name" class="form-control"
               value="1" th:value="${board.name}" readonly>
    </div>

    <div>
        <label for="title">제목</label>
        <input type="text" id="title" name="title" class="form-control"
               value="제목1" th:value="${board.title}" readonly>
    </div>
    <div>
        <label for="content">본문</label>
        <input type="text" id="content" name="content" class="form-control"
               value="컨텐츠" th:value="${board.content}" readonly>
    </div>
    <div>
        <label for="read">조회 수</label>
        <input type="text" id="read" name="read" class="form-control"
               value="1" th:value="${board.read}" readonly>
    </div>

    <hr class="my-4">
    <div class="row">
        <div class="col">
            <button class="w-100 btn btn-primary btn-lg"
                    onclick="location.href='editForm.html'"
                    th:onclick="|location.href='@{/boards/{boardId}/edit(boardId=${board.boardId})}'|"
                    type="button">게시글 수정</button>
        </div>
        <div class="col">
            <button class="w-100 btn btn-secondary btn-lg"
                    onclick="location.href='items.html'"
                    th:onclick="|location.href='@{/boards/{boardId}/delete(boardId=${board.boardId})}'|"
                    type="button">게시글 삭제</button>
        </div>
        <div class="col">
            <button class="w-100 btn btn-secondary btn-lg"
                    onclick="location.href='boards.html'"
                    th:onclick="|location.href='@{/boards}'|"
                    type="button">목록으로</button>
        </div>

    </div>
</div> <!-- /container -->
</body>
</html>
```
### service
#### BoardService.java
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardMapper boardMapper;

    public Board findById(Long boardId){
        return boardMapper.findById(boardId);
    }

}

```

### repository
#### boardMapper.java
```java
@Repository
public interface BoardMapper{
    Board findById(Long boardId);
}
```
### boardMapper.xml
```xml
<select id ="findById" parameterType="Long" resultType="com.example.blog_board.domain.Board">
    SELECT * FROM tbl_board WHERE boardId=#{boardId};
</select>
```
---
## 게시글 생성하기(R)

---
### controller
#### boardContoller.java
```java
@GetMapping("/add")
    public String add(){
        return "/board/addForm";
    }

@PostMapping("/add")
public String add(@RequestParam String title, @RequestParam String content,
                    @RequestParam String name, RedirectAttributes redirectAttributes){
    Board newBoard = new Board(title, content, name);
    Long boardId = boardService.add(newBoard);
    System.out.println("boardId = " + boardId);

    redirectAttributes.addAttribute("boardId", boardId);
    redirectAttributes.addAttribute("status", true);

    return "redirect:/boards/{boardId}";
}
```
저장하기 같은 경우 두 가지 api를 사용한다. 일단 사용자는 get boards/add 라는 url에 접속하고 board/addForm html로 접속한다.
페이지에서 저장할 게시판 정보를 입력하고 ```게시글 등록``` 버튼 을 누르면 post boards/add url에 접속되고 정보를 토대로 db에 게시글이 저장된다.
이때 새로고침시 post가 다시 나가며, (새로고침은 전에 사용한 url을 다시 사용한다. post가 나갔으므로 바로 f5시 post가 연속으로 나오고 같은 내용의 게시글이 다시 저장된다.) 
이를 방지하기 위해 redirect 한다.

### view
#### addForm.xml
```xml
<form action="board.html" th:action  method="post">
    <div>
        <label for="title">제목</label>
        <input type="text" id="title" name="title" class="formcontrol" placeholder="제목을 입력해주세요">
    </div>
    <div>
        <label for="content">본문</label>
        <input type="text" id="content" name="content" class="form-control"
                placeholder="본문을 입력해주세요">
    </div>
    <div>
        <label for="name">글쓴이</label>
        <input type="text" id="name" name="name" class="form-control"
                placeholder="닉네임을 입력해주세요">
    </div>

    <hr class="my-4">
    <div class="row">
        <div class="col">
            <button class="w-100 btn btn-primary btn-lg" type="submit">게시글 등록</button>
        </div>
        <div class="col">
            <button class="w-100 btn btn-secondary btn-lg"
                    onclick="location.href='boards.html'"
                    th:onclick="|location.href='@{/boards}'|"
                    type="button">취소</button>
        </div>
    </div>
</form>
```
### service
#### boardService.java
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardMapper boardMapper;
    @Transactional
    public Long add(Board board) {
        boardMapper.save(board);
        return board.getBoardId();
    }
}
```
DB에 접속하여 insert 쿼리를 날리기 위해서 Transactional 어노테이션을 추가로 달아 readOnly = false로 바꾼다.

### repository
#### boardMapper.java
```java
@Repository
public interface BoardMapper{
    Long save(Board board);
}
```
#### baordMapper.xml
```xml
<insert id ="save" parameterType="com.example.blog_board.domain.Board" useGeneratedKeys="true" keyProperty="boardId">
    INSERT INTO tbl_board (title, content, name) VALUES (#{title}, #{content}, #{name});
</insert>
```
![그림](https://user-images.githubusercontent.com/23234577/122560830-0e567000-d07c-11eb-9c85-98878d42191b.png)

---
## 게시글 수정하기(R)

---
### controller
#### BoardController.java
```java
@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/{boardId}/edit")
    public String editForm(@PathVariable Long boardId, Model model){
        Board findBoard = boardService.findById(boardId);
        model.addAttribute("board", findBoard);

        return "board/editForm";
    }

    @PostMapping("/{boardId}/edit")
    public String editForm(@PathVariable Long boardId, @RequestParam String title,
                           @RequestParam String content, @RequestParam String name)
    {

        Board findBoard = boardService.findById(boardId);
        findBoard.setTitle(title);
        findBoard.setContent(content);
        findBoard.setName(name);

        boardService.update(findBoard);

        return "redirect:/boards/{boardId}";
    }
}
```
저장하기와 마찬가지로 입력 폼으로 이동 후 post로 db내용을 수정한다. 단 수정하기 임으로 입력 폼에 추가로 기존의 게시글 정보를
가져간다.

### view
#### editForm.xml
```xml
<form action="board.html" th:action method="post">
    <div>
        <label for="id">상품 ID</label>
        <input type="text" id="id" name="id" class="form-control" value="1" th:value="${board.boardId}"readonly>
    </div>
    <div>
        <label for="title">제목</label>
        <input type="text" id="title" name="title" class="formcontrol" placeholder="제목을 입력해주세요">
    </div>
    <div>
        <label for="content">본문</label>
        <input type="text" id="content" name="content" class="form-control"
                placeholder="본문을 입력해주세요">
    </div>
    <div>
        <label for="name">글쓴이</label>
        <input type="text" id="name" name="name" class="form-control"
                placeholder="닉네임을 입력해주세요">
    </div>
    <hr class="my-4">
    <div class="row">
        <div class="col">
            <button class="w-100 btn btn-primary btn-lg" type="submit">저장
            </button>
        </div>
        <div class="col">
            <button class="w-100 btn btn-secondary btn-lg"
                    onclick="location.href='board.html'"
                    th:onclick="|location.href='@{/boards/{boardId}(boardId=${board.boardId})}'|"
                    type="button">취소</button>
        </div>
    </div>
</form>
```
### service
#### BoardService.java
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardMapper boardMapper;
    @Transactional
    public Long update(Board board){
        return boardMapper.update(board);
    }
}
```
### repository
#### boardMapper.java
```java
@Repository
public interface BoardMapper{
    Long update(Board board);
}
```
#### baordMapper.xml
```xml
<update id ="update" parameterType="com.example.blog_board.domain.Board">
    UPDATE tbl_board
    SET title = #{title}, content = #{content}, name = #{name}
    WHERE boardId = #{boardId};
</update>

```
![그림](https://user-images.githubusercontent.com/23234577/122560832-0eef0680-d07c-11eb-9ec0-e6cf1aebd620.png)
---
## 게시글 삭제하기(R)

---
### controller
#### BoardController.java
```java
@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {
    @GetMapping("/{boardId}/delete")
    public String deleteBoard(@PathVariable Long boardId){
        boardService.deleteById(boardId);
        return "redirect:/boards";
    }
}
```
삭제 후 바로 boards로 라디이렉트 한다.

### service
#### BoardService.java
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardMapper boardMapper;

    public void deleteById(Long boardId) {
        boardMapper.delete(boardId);
    }
}
```
### repository
#### boardMapper.java
```java
@Repository
public interface BoardMapper{
    void delete(Long boardId);
}
```
#### baordMapper.xml
```xml
<delete id ="delete" parameterType="Long">
    DELETE
    FROM tbl_board
    WHERE boardId = #{boardId};
</delete>
```
![그림](https://user-images.githubusercontent.com/23234577/122560834-0f879d00-d07c-11eb-92fd-a20ac2ae7290.png)

![그림](https://user-images.githubusercontent.com/23234577/122560835-0f879d00-d07c-11eb-9a67-b70c3ec23565.png)

---
## 전체 코드

---
https://github.com/salmon2/Board-spring-mybatis

---
