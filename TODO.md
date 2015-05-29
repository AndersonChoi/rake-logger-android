### Day 8

- 강제로 flush 하려면? -> subject
- subject 는 hot observable

- Formatter 분리.

- 조절가능한 Timer 를 갖는 Observable 을 만든다.
- onStop, onResume 을 zip 해서 타이머를 돌려야한다.
- 별도의 스레드로 동작해야 한다. subscribeOn, observeOn

- 셔틀과 같이 쓰여야 한다.
- CompressField

- 스케쥴러를 설정할 수 있어야 한다.
- Token, `Dev` 모드일 경우 바로 플러시 해야한다.

### Next

- HTTPClient -> HTTPUrlConnection 으로 변경하려면 TestClient 를 변경해야.
- HTTPS 로 보내야한다. HttpUrlConnection 으로 변경 후 이 작업 진행할 것

- Flush Interval per Config
> Rake Instance 마다, flushInterval 을 다르게 할지는 좀 더 생각해볼 문제.

- DAOSqlite 구현
- DAO multithread 고려

### Day 7

- ~~Token 별로 인스턴스를 생성해야 한다~~
- ~~Live, Dev Token 을 위한 지원~~
- ~~API 로 노출되는 부분이 있어야하지 않을까?~~

- ~~RakeUserConfig hashCode, Equals~~
- ~~RakeUserConfig - time, Mode~~
- ~~Network module 분리~~

- ~~TestLoggerFactory 와 LoggerFactory 연동~~
> 자바에서는 static 에 대한 다형성을 지원하지 않음. Global 로 취급하기 때문. 인터페이스에서도 마찬가
지. JVM8 부터는 인터페이스에 스태틱 메소드가 추가된 것으로 보임.

### Day 6

- ~~printStackTrace 제거~~
- ~~Log Wrapping 하기.~~

- ~~automatic version upgrade using gradle~~

### Day 5

- ~~MockHttpClient~~
-> protected method 인 getResponseString 만들고, 여기서 TestLogger 가 override

- ~~Http Response~~
- ~~Http Header~~
- ~~RakeProtocol~~
- ~~RakeProtocolException~~

- ~~500 404 는 ErrorCode 가 아니라 StatusCode 로 구분해야~~

### Day 4

- ~~Timeout Exception~~
- ~~Json 으로 Assert~~,
- ~~최대 N개 까지만 저장~~
- ~~getMaxCount, getEndPoint 를 다 제거할 수 있지 않을까?~~

### Day 3

- ~~`send` 는 실제 `body` 를 전송하고, 리턴해야한다.~~
- ~~여러개의 바디를 \n 로 Join 해서 전송해야 한다.~~
- ~~`flash` 는 비었을때 아무것도 전송하면 안됀다.~~ -> `null` 을 리턴하도록

- ~~http 로 메세지를 전송해야 한다~~.
- ~~로거마다 엔드포인트가 있어야 한다~~.

### Day 2

- ~~로거는 싱글턴이어야 한다.~~
- ~~클래스 단위로 싱글턴이어야 한다.~~

- ~~안드로이드 프로젝트여야 한다.~~

### Day 1

- ~~track 은 JSON 을 받아서, 내부에 저장해야 한다~~
- ~~한 테스트가 다른 테스트에 의존한다. track 을 테스트할 수 있는 방법을 찾아야한다.~~
- ~~먼저 Track 의 내부 구조를 테스트해야 한다.~~
- ~~LoggerBuffer 를 추가했으나, 이걸 테스트하기 위해 Logger 내부구조를 테스트한다. MOCK 을 사용해볼까?~~
- ~~track 이 addToLog 를 호출하게끔 했다. track 의 책임을 떠올려봤다.~~
- ~~internal state 를 어떻게 테스팅해야할까? powermock 을 쓰는것은 좋은 방법은 아닌것 같다.~~
- ~~flush 를 호출했을 때 그 결과가 여러개 리턴되야 한다.~~
- ~~track 과 flush 가 List 가 아니라 Array 를 리턴하도록 했다. 내부 구조를 수정할 수 없도록 하는관점에서 좋다.~~
- ~~flush 후 삭제되야한다.~~
- ~~track 은 로그를 로컬에 저장해야 한다.~~ 
- ~~logBuffer 즉, persistent 계층을 Logger 와 분리해야 한다.~~
- ~~RakeBasicLogger 가 RakeLogDaoSQLite 를 알고있다.~~


