### Day 12

- Base64 encoding
- UrnEncoding Entity
- HTTPS 로 보내야한다.

- ~~JSONArray 로 보내야 한다.~~
- ~~CompressField~~

- SuperProperty -> Rake 책임.

- RakeLogger.getInstance() 를 Double-Check Locking 으로 변경
- flush, track synchronized

- ~~ShuttleProtocol 정리~~

### Next

- HttpUrlConnection 또는 라이브러리
- RakeUserConfig 애 `providePrintLog`

- onStop, onResume 을 zip 해서 타이머를 돌려야한다.
- onErrorReturn 더 정교히

- DAOSqlite 구현

- lombok

- defaultProperties 에서 네트워크 타입이 준 실시간 즉, Rake 인슽
처
- track 자체를 스레드에서 돌리면?
- Shuttle Protocol.static 함수들을 다 rake private 로 변경하면?
- RakeImpl 쓸모없음. 차라리 ShuttleDelivery 이렇게 해도 될 듯.
- CrashLogger 는 Delivery 를 상속받아, Flush없이 바로 Tracked 해도 될것 같고.

- maven AAR

#### TBD

- Flush Interval per Config
> Rake Instance 마다, flushInterval 을 다르게 할지는 좀 더 생각해볼 문제.

- HTTPClient -> HTTPUrlConnection 으로 변경하려면 TestClient 를 변경해야.
- HTTPUrlConnection 대신 Retrofit 사용
> 오버헤드가 있지만, 안드로이드 버전따라 어떤 라이브러리를 사용해야 할지 자동으로 결정.
> http://helloworld.naver.com/helloworld/377316 참조

- worker 내의 Http, Dao 접근 Future 로

### Day 11

- ~~셔틀과 같이 쓰여야 한다.~~
- ~~Sentinel Meta 를 뽑아서 depth 를 한단계 올려야 한다.~~
- ~~Properties~~ 에 기존 값들을 넣어야 한다.
- ~~SystemInformation~~


### Day 10

- ~~RakeCore 테스팅하기가 불편하다. RakeHttpClient 주입하기가 어려움~~ -> 싱글턴 패턴 제거
> 테스팅 하려면 어쨌든 주입해야 한다. 주입하는 방법은 생성자, Setter 두가지 밖에 없다.
싱글턴의 문제는 생성자로 주입한 것을 나중에 갈아치우기 어렵다는 것.

- ~~RakeCore.returned 만들때 dev면 timer 제거하고, 즉시 flush~~
- ~~Token, `Dev` 모드일 경우 바로 플러시 해야한다. -> `RakeCore.returned` 를 build 옵션 주듯~~

- ~~Formatter 분리~~ -> 비즈니스 로직은 RakeProtocol 에

### Day 9

- ~~RakeCore 에서 filter flush if null~~
- ~~subscribe 정책 세울 것.~~
- ~~RakeCore.getInstance 에서 inject 할 것인지 정하기~~

- ~~별도의 스레드로 동작해야 한다. subscribeOn, observeOn~~
- ~~DAO multithread 고려 -> Observable subscribeOn 내에서 일어나면, synchronization 필요 X~~
- ~~스케쥴러를 설정할 수 있어야 한다. -> Timer Observable 로~~
- ~~RakeUserConfig 내에서 provideFlushInterval, maxCount~~

### Day 8

- ~~trackable~~
- ~~flushable~~
- ~~timer~~
- ~~코어 로직을 Observable 과 Subject 로 만들기~~
- ~~첫번째 타이머 돌기 전에도 Flush 가능하게 만들기~~
- ~~조절가능한 Timer 를 갖는 Observable 을 만든다.~~


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


