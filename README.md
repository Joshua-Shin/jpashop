## SpringBoot와 JPA를 활용한 쇼핑몰 웹 애플리케이션 개발 프로젝트
### 개발 환경  
- Java 11, Spring Boot(2.7.7), JPA, thymeleaf, JUnit4, IntelliJ, H2 database(1.4.200)
- thymeleaf, lombok, bootstrap(4.3.1)
### 구현 기능
- 회원 기능 : 회원 가입, 회원 목록
- 상품 기능 : 상품 등록, 상품 목록, 상품 수정
- 주문 기능 : 상품 주문, 주문 내역, 주문 취소

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
  - [참고](https://www.inflearn.com/questions/31969/cascade-%EC%98%B5%EC%85%98-%EC%A7%88%EB%AC%B8)
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


#### 회원 도메인 개발
- @PersistenceContext 를 통해 entityManager를 주입 받았었는데, Spring data JPA 에서는 이를 @Autowired로 해도 가능하게 해줌
  - 여기에 필드 주입 말고 생성자 주입 스타일로 DI를 하고, 롬복까지 사용하게 되면, 그냥 클래스 위에다가 @RequiredAugsConstructor 만 적어두면 깔끔하게 해결 
  - 필드는 반드시 private final으로 선언해줘야 주입됨. 그냥 private만 하면 주입 안됨.
- @Transactional(readOnly = true)
  - 디폴드값은 false로 되어있는데, 단순히 조회만 하는 메소드의 경우 해당 옵션을 해주면 성능에서 이점을 챙길 수 있음
  - read 메소드가 대부분인 클래스다 싶으면 클래스 전체에다가 @Transactional(readOnly = true) 걸어주고, 아닌 메소드에다가 @Transactional 걸고
  - 반대의 상황이라면 전체에다가 @Transactional 걸고 read 메소드 위에다가만 @Transactional(readOnly = true) 해주면 돼.
  - 상위 메소드에 걸어주면 해당 메소드를 호출하면서 연쇄적으로 호출하게 되는 메소드들도 다 적용되게됨.
  - 그래서 Repository에는 안걸어주고 Service에만 걸어준거야. Service에 있는 메소드를 통해 Repository의 메소드들을 호출하게 되니까. 
- 가입하려 하는데 회원이름이 중복될 경우 예외를 던지게 한다 해도, 멀티쓰레드로 db에 접근을 하게 되면서 해당 예외가 기대한 상황에 안터질 수 있어.
  - 따라서 회원이름을 unique 옵션 주는게 더 안전하겠지.
- @SpringBootApplication
  - 컴포넌트 스캔이 들어가 있어서 컴포넌트로 등록된 클래스들을 DI 해주면서 스프링 빈으로 등록해줌.
  - @Autowired 명시한 애들 DI 해주는데, 생성자 주입 + 생성자가 하나있을 경우에는 명시 하지 않아도 됨. 필드 주입은 명시해줘야 하는듯. 
  - @Service, @Repository, @Controller 에는 @Component 들어가 있고.
- application.yml을 main과 test에서 따로 두는게 좋아. 원하는 설정이 다를테니까.
  - 특히 테스트의 경우 인메모리DB로 진행하는게 안전하며, 
    따로 apllication.yml에 H2에 연동시키는 설정을 적어주지 않으면, spring boot에서 알아서 인메모리DB로 테스트 진행함.


#### 상품 도메인 개발
- Item 클래스에 비즈니스 로직 추가
  - stockQuantity 데이터가 있는 쪽에서 비즈니스 로직 메소드를 만드는게 객체지향적임.
- Repository에서 Member와 Item의 save 메소드가 차이가 있는 이유
  - 처음 persist 하기전까지는 member든 item이든 둘다 id 없음.
  - 다만 member는 한번 생성하면 수정이 없겠지만, item은 한번 생성해놓은것도 수정이 생길 수 있어.
  - 그래서 처음 만든거라면 바로 저장하고, 그렇지 않은거라면 merge로 업데이트 하는거야.
  - merge가 업데이트가 아니긴 한데, 뒤에서 설명할테니 일단 이렇게 알아두고 넘어가래.


#### 주문 도메인 개발
- 엔티티에 생성 메소드, 비즈니스 로직, 조회 로직을 둠
- **생성 메소드 둔 이유** 
  - 서비스 쪽에서 기본 생성자 호출 후 set set set 해서 넘기게 되면,해당 엔티티의 생성 로직이 바뀔때마다 해당 엔티티의 생성을 쓰는 곳마다 가서 다 수정 해줘야돼. 
  - 그러나 생성 메소드를 엔티티 내에 따로 정의해두면, 생성 로직이 바뀔때 해당 메소드만 수정하면 되니까 훨씬 유지 보수가 깔끔하지.
  - 따라서, 인자를 여러개 받고 중요한 엔티티의 경우 이렇게 생성 메소드를 만들어두면 아주 좋아. 
  - 동시에 기본 메소드를 막아두는게 좋으니 protected로 정의해두거나, 롬복 이용해서  @NoArgsConstructor(access= access.level.protected)를 엔티티 위에 두면 됨.
- 비즈니스 로직이나 조회 로직같은것을 엔티티 내부에 넣어 서비스 쪽에서는 필요한 로직만 호출하는것을 '도메인 모델 패턴'이라 함. 조금 더 객체지향적.
- 반대로 서비스쪽에서 비즈니스 로직을 직접 처리하는것을 '트랜잭션 스크립트 패턴'이라 함.
- 상황에 따라 어느것이 더 유지 보수에 효율적인지는 다를 수 있어. 근데 다는 도메인 모델 패턴이 더 마음에 들긴 하네.
- orderRepository.save(order); 만 해줘도 delivery와, orderItem에 cascade를 걸어놨기에, 얘네도 다 persist 됨...이야...
- order.cancel(); 만 해줘도.. persist라든가 뭐 안해줘도 JPA가 변경된 감지. dirty checking을 다 해줘서 알아서 db에 update 쿼리 날려줌...이야...... 
- 주문 검색 기능
  - 회원이름과 주문상태를 입력해서 그에 따른 주문 조회가 이루어지는 기능.
  - 결국 다량 조회는 JPQL 써야 하는데, 이때 회원이름이 있을때, 없을때, 주문상태가 입력 될때 입력 안될때마다 분기가 생김
  - 즉, 동적 쿼리가 필요함. 
    - 동적 쿼리: 상황에 따라 다른 sql 문이 들어가야 하는 쿼리.
  - 이걸 if else문 덕지덕지 붙여서 문자열 붙이고 더하고 파라미터 잡고 그러면 코딩 자체도 너무 힘들고 문자열 그 자체이기에 컴파일 단계에서 잡아주지도 못하고, 유지 보수도 힘들어
  - JPA 표준 스팩인 criteria 라는게 있는데, 이건 너무 sql문과 거리가 멀어서 쓸만하지 못해.
  - 해답은 QueryDsl에 있음. 해당 내용은 이번 강의에서 다루지 않음.
  - 이 프로젝트는 일단 JPQL과 criteria로 작성된거 처음으로 복붙함.
  - 결론은 동적 쿼리를 쓰려면 QueryDsl를 사용해야 됨.


#### 웹 계층 개발
- 현재까지 개발 순서
  - 요구사항 분석 -> 엔티티, 테이블 설계 -> 엔티티 개발(연관관계, 양방향, 연관관계 편의 메소드) 
  - -> 회원 도메인 개발 (회원 리포지토리 개발 -> 회원 서비스 개발 -> 회원 서비스 테스트)
  - -> 상품 도메인 개발 (엔티티에 비즈니스로직 개발 -> 상품 리포지토리 개발 -> 상품 서비스 개발)
  - -> 주문 도메인 개발 (엔티티에 비즈니스로직 개발 -> 주문 리포지토리 개발 -> 주문 서비스 개발 -> 주문 서비스 테스트)
  - -> 이제 웹 계층 개발할 차례
- @Slf4j : Logger log 주입해줌.
- @RequestMapping("/") : 클라이언트가 url 치고 들어올 때 보여줄 html 파일을 매핑해줌.
- html 파일 복붙. Bootstrap 다운받고 css, js 복붙
- 선수 지식 부재..
  - 이번 강의 파트의 내용을 정확히 이해하려면 최소한 html은 알아야 하고, thymeleaf, http, 웹MVC에 대한 이해가 필요할듯.
  - jsp든 thymeleaf든 mustache든 템플릿 엔진을 하나는 익혀두긴 해야할듯.
  - 템플릿 엔진은 html 파일을 동적으로 작동하여 화면을 만들어주는 소프트웨어.
  - jsp는 지는해이고, thymeleaf은 스프링이 공식적으로 권장하는 템플릿엔진이고. mustache 얘는 SSR이 아니라 CSR이네
  - 이 강의가 얼마 안남았으니 일단은 끝까지 따라해보고 추후에 html -> thymeleaf -> http -> 웹MVC 순으로 학습하자 
- Member엔티티가 있는데 MemberForm을 따로 만든 이유
  - 도메인이 원하는 valid 데이터와 MemberForm이 원하는 valid 데이터가 달라.
  - 굳이 굳이 Member엔티티의 필드에다가 이런저런 옵션 넣어서 MemberForm이 원하는 valid 데이터로만 제한을 걸수 있겠지만,
  - 그러면 엔티티에 화면 종속적인 요소들이 붙게 되면서, 엔티티가 지저분해짐.
  - 화면 수정하는데 핵심 비즈니스 로직이 틀어진다거나, 비즈니스 로직을 수정하는데 화면이 틀어진다거나 하는 상황들을 막으려면,
  - 최대한 엔티티를 순수하게 유지하는거야.
  - 따라서 그냥 새로운 적합한 Form을 만들고 Web 혹은 Controller 층에서 데이터를 받아 한번 필터링 하여 Member에 넣는게 좋아.
- 변경 감지와 병합
  - merge
    - 새로운 객체를 만든다음에 그 객체에다가 이미 저장되어있는 엔티티의 식별자를 부여하여 준영속 엔티티로 만듬
    - 준영속 엔티티에 데이터를 새로 입력하고 merge
    - 기존의 데이터가 새로운 데이터로 덮어씌워짐
    - 준영속 엔티티에 필드 값들을 새로 입력하지 않았을시 null로 덮어씌워질 수 있음
  - 변경 감지
    - 영속성 컨텍스트로 저장되어있는 엔티티를 find로 불러옴
    - 영속 엔티티의 데이터를 수정함.
    - 따로 persist하는게 아니라 JPA가 변경된 데이터를 감지해서 commit 순간에 알아서 update 쿼리 날려줌.
    - 딱 변경할 데이터만 수정하면 되고, null로 덮어씌워질일이 없음
  - 결론 : merge 쓰지말고 변경감지로 update 해야돼