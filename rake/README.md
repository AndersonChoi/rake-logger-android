# Rake Android 2.0

- Explicit Configuration
- Simplify Concurrency Logic Using Rx
- Reusable Component

## Development

### 1. Roboletric

add this line into your `@Before` to enable logging in testing environment

```
ShadowLog.stream = System.out;
```

### 2. Retrolambda

set `JAVA_HOME`, `JAVA6_HOME`, `JAVA7_HOME`, `JAVA8_HOME` in `build.gradle` `retrolambda` block.

for example,

```
retrolambda {
    String JAVA_HOME  = '/Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home'
    String JAVA6_HOME = '/Library/Java/JavaVirtualMachines/1.6.0_65-b14-462.jdk/Contents/Home'
    String JAVA7_HOME = '/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home'
    String JAVA8_HOME = '/Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home'

    jdk JAVA8_HOME
    oldJdk JAVA6_HOME
    javaVersion JavaVersion.VERSION_1_6
    defaultMethods false
    incremental true
}
```

If you don't know where JRE is installed, This will be helpful.

```gradle
retrolambda {
    println("***************** ---------- *******************")
    println("JAVA_HOME: " + System.getenv("JAVA_HOME"))
    println("JAVA6_HOME: " + System.getenv("JAVA6_HOME"))
    println("JAVA7_HOME: " + System.getenv("JAVA7_HOME"))
    println("JAVA8_HOME: " + System.getenv("JAVA8_HOME"))
    println("***************** ---------- *******************")

    ...
    ...
}
```