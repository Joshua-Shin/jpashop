## SpringBoot와 JPA를 활용한 쇼핑몰 웹 애플리케이션 개발 프로젝트
### 개발 환경  
- Java 11, Spring Boot(2.7.7), JPA, thymeleaf, JUnit4, IntelliJ, H2 database(1.4.200)
- thymeleaf, lombok
### 구현 기능
- 회원 기능 : 회원 가입, 회원 목록
- 상품 기능 : 상품 등록, 상품 목록
- 주문 기능 : 상품 주문, 주문 내역

### 엔티티 구조
<img width="799" alt="스크린샷 2023-01-25 오후 7 58 04" src="https://user-images.githubusercontent.com/93418349/214547603-5ca8ed34-834d-45e3-bb7c-37c1a4f1cce3.png">


### 테이블 구조
<img width="798" alt="스크린샷 2023-01-25 오후 7 58 37" src="https://user-images.githubusercontent.com/93418349/214547614-546209e6-8aeb-49cd-92e7-b25c51e00f5c.png">

-------
### 강의 내용 요약
#### 프로젝트 환경설정
- @PersistenceContext
  - 기존에는 EntityManagerFactory에서 persistence.xml에 입력한 DB 정보를 바탕으로 EntityManager 생성했었는데,
  - 스프링 부트에서 이 에노테이션 사용하면 알아서 주입 해줌.
- @Transactional
  - 기존에는 em.get트랜젝션 해서 tx.start() 하고 tx.commit 하고,
    try catch finally문 써서 실패할경우 rollback 하고, finally에서 em.close 써주고 다 해줬잖아
  - 스프링 부트에서는 이 에노테이션 쓰면 알아서 다 해줌.
  - test 코드에서 사용할 경우, 테스트 돌리고 나서 자동으로 rollback 해줌. 단, @Rollback(false) 라 해주면 롤백 안해줌
- JUnit4
  - 현재 테스트 코드를 JUnit4로 돌리고 있음. 때문에 build.gradle에서 test를 junit4로 돌리라고 설정해줌. 이걸 따로 안잡아주면 디폴트로 junit5가 돌아가는듯
  - 테스트 코드 작성할때 위에 어노테이션을 @Runwith 이라든가, org.junit.Test의 @Test 라든가 하는것도 다 junit4 전용 라이브러리인듯.
  - @RunWith(SpringRunner.class) : 스프링 관련된걸로 테스트 할거야~ 라고 알려주는거
  - JUnit5에서 @SpringBootTest 작성시 별도로 작성하지 않아도 됨.
  - 일단 이 강의는 junit4로 진행하고, 후에 수정을 하든 하자.
  - tdd + tab : given when then 틀 만들어놓음.
- p6spy. 쿼리 날라가는거 잘 보이게 해주는 오픈소스. build.gradle에 추가함.
- 현재 기존 테이블이 드랍되지 않는 버그가 있는데, 이는 H2버전과 하이버네이트버전의 문제인듯. 추후에 수정.

#### 도메인 분석 설계
- Category 엔티티를 보면 셀프 조인을 함.
  - 내가 parent라 생각하면 child가 여럿 있을테니 OneToMany로 지정해서, 콜렉션으로 참조하는거고.
  - 내가 child라 생각하면 parent가 하나 있을테니 ManyToOne으로 지정해서 연관관계 주인으로 잡는거고.
- 값타입 클래스는 immutable하게 관리되어야 함.
  - Member가 참조하는 Address와 Delivery가 참조하는 Address가 공유하게 되어 값이 변경되는 대참사가 발생할 수도 있어
  - 따라서 Setter를 없애고, 모든 필드값을 인자로 받는 생성자를 만들되, 
    JPA에서는 스펙상 해당 클래스의 기본 생성자가 필요하기에 protected으로 선언한 기본 생성자도 하나 넣어둬야돼.
- cascade = CascadeType.ALL
  - 영속성 전이. 
  - 게시물을 삭제 할때, 게시물의 첨부파일도 당연히 삭제되기를 기대하니까 이런경우에는 생애주기를 묶어주는거야.
  - 즉, 게시물이 첨부파일을 개인소유한 상태이니 영속성을 전이시키면 좋다.
  - 마찬가지로 order가 사라지면, orderitem도 사라지길 기대하니 Order 클래스에다가 OrderItem을 참조하는 필드위에 해당 옵션을 명시
  - [답변](https://www.inflearn.com/questions/31969/cascade-%EC%98%B5%EC%85%98-%EC%A7%88%EB%AC%B8)
- 연관관계 편의 메소드
  - 양방향 연관관계일경우 한쪽에만 set을 하고 다른쪽 set 하는것을 까먹을 수 있기에 두 함수를 묶어주는 사용자 정의 함수를 새로 선언해주는거.
  - 꼭 연관관계 주인 되는쪽에다가 해당 메소드를 정의하는것은 아니고 그냥 자주 사용하는 엔티티쪽에다가 선언해주면 돼.
- 테이블명 명시해야 되는 경우
  - db에서는 order by 키워드 때문에 order를 잘 안씀 "orders"로 명시.
  - OrderItem 처럼 두 단어일 경우 db에서는 camel 스타일보다 언더바를 쓰기에 "order_item"으로 명시
- 칼럼명 명시해야 되는 경우
  - 엔티티에서는 필드명을 id를 그냥 그대로 써도 member.getId()로 하기에 상관없지만, db에서는 되도록 다른 테이블이더라도 다른 칼럼명을 사용하는것이 좋기에, "member_id"로 명시
- @DiscriminatorColumn(name = "dtype")
  - 상속관계일때, 상위엔티티에 이거 두는거 까먹지마!
  - @DiscriminatorValue("B") 는 해도 되고 안해도 되고. 안하면 그냥 클래스명이 디폴트로 들어감.
- 다대다 연관관계를 일단은 학습을 위해 구현해놨지만, 실무에서는 절대 하지마. 