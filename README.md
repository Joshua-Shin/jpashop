## SpringBoot와 JPA를 활용한 웹 애플리케이션 개발 프로젝트
- 본 프로젝트는 <실전! 스프링 부트와 JPA 활용1 : 웹 애플리케이션 개발 - 김영한> 강의를 수강하며 진행한 실습 프로젝트 입니다.
- 개발 환경 : Java 11, Spring Boot(2.7.7), JPA, thymeleaf, JUnit4, IntelliJ, H2 database(1.4.200)
-------
#### 엔티티 분석
![](../../../../Desktop/스크린샷 2023-01-25 오후 7.58.04.png)


#### 테이블 분석
![](../../../../Desktop/스크린샷 2023-01-25 오후 7.58.37.png)
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
