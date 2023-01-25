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
  - 따라서 Setter를 없애고, 생성자를 만들되, JPA에서는 스펙상 해당 클래스의 빈 생성자가 필요하기에 protected의 껍데기 생성자도 하나 넣어둬야돼.