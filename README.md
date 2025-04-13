# log4j2-masker

**log4j2-masker** is a lightweight Java library that automatically censors sensitive information (like passwords, API keys, and tokens) from being logged in your Log4j2 logs.

> Prevent secrets from leaking into log files — safely and automatically.

---

## ✨ Features

- 🚫 Masks passwords, secrets, tokens, and other sensitive fields
- ⚙️ Plug-and-play integration with Log4j2
- 🔒 Keeps your logs clean and secure
- 📦 works with your shaded JAR with all dependencies with a minor tweak to pom.xml
- unlike other approaches No complex regex to configure log4j with. 

---

## How does it work ?

Say you have the following java code

```java

        Properties configs = new Properties();
        //We wish to censor the values of these configuration paramters
        configs.put("db.user", "admin");
        configs.put("db.passwd", "startrek123");
        configs.put("aws.access_key", "@12h7$");
        configs.put("aws.client.id", "hellothere@");

        //pass configs to LogCensor
        LogCensor.detectSensitiveConfigs(configs);

        //You notice The keywords from the configuration parameters are:
        //user (from db.user), passwd(from db.passwd), key (from aws.access_key)
        //client.id (from aws.client.id)
        //These keywords are to be speficied in log4j2.properties        

        //try to log it :)
        logger.info("Connecting to database with user: "
        + configs.getProperty("db.user")
        + " and password: " + configs.getProperty("db.passwd"));

        logger.info("Connecting to AWS with access key: " + configs.getProperty("aws.access_key")
        + " and client id: " + configs.getProperty("aws.client.id"));

```

Step 1: Configure the censor with the keywords you want to hide. 

Add these lines to log4j2.properties

```properties
property.sensitive_configs = user,passwd,key,client.id
# feel free to add more key words
#property.sensitive_configs = passwd,key,access,secret,apikey,api_key,credential,token,auth,signature,passphrase,client_id,client.id,client_secret,client.secret,authorization,bearer,user_name,username,user,pass,password

# now configure EVERY appender in your log4j2.properties

# You will notice '%censor{enabled=true}%n%ex' instead of the default '%m%n%ex'
appender.console.type = Console
appender.console.name = console
appender.console.target = SYSTEM_ERR
appender.console.layout.type = PatternLayout
#appender.console.layout.pattern = %d{yy/MM/dd HH:mm:ss} %p %c{1}: %m%n%ex
appender.console.layout.pattern = %d{yy/MM/dd HH:mm:ss} %p %c{1}: %censor{enabled=true}%n%ex
#appender.console.layout.pattern = %d{yy/MM/dd HH:mm:ss} %p %c{1}: %censor{enabled=false}%n%ex
```

Step 2:

Run your java application with the above configuration.

```bash

java -cp .... -Dlog4j.configurationFile=<path_to>/log4j2.properties ....
```

Step 3:

Your should see the following output

```text
LogCensor for logger created. enabled: true .options are: [enabled=true]
25/04/13 13:04:21 INFO CensorTest: Connecting to database with user: ****** and password: ******
25/04/13 13:04:21 INFO CensorTest: Connecting to AWS with access key: ****** and client id: ******
```

The Log censor will mask the senstive word where ever it finds in the log message.

Ex: in our example 'admin' is the username, hence the word 'admin' will censored where ever it appears in the log message.


## 📦 Installation

Add the following dependency to your `pom.xml`:

```xml
<!-- Make sure to ALSO add the log4j api/core deps to your pom.xml -->
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>${set.your.log4j2.version}</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
    <version>${set.your.log4j2.version}</version>
    <scope>compile</scope>
</dependency>

<!-- Add the masker dependency -->
<dependency>
  <groupId>io.github.jaihind213</groupId>
  <artifactId>log4j2-masker</artifactId>
  <version>2.20.0</version>
</dependency>

```
### Adding to your Fat Jar

- If you are not using fat jar, add the log4j2-masker jar to your classpath

You will need to add a few transformers to your shade plugin configuration::

- For **Log4j2 versions > 2.20.0**, please refer to the [`pom_gte_log4j2_2_20_0.xml`](./pom_gte_log4j2_2_20_0.xml) for instructions on how to shade the JAR correctly.
- For the following Log4j2 versions,
    - `2.20.0`
    - `2.19.0`
    - `2.18.0`
    - `2.17.2`
    - `2.17.1`
    - `2.8.1`
    - `2.8`
    - `2.7`
    - `2.6.2`
    - `2.6.1`
    - `2.1`
        - refer to the [`pom_legacy.xml`](./pom_legacy.xml)  on how to shade the JAR correctly.

#### Examples 

1. refer to the unit test in the project.
2. refer to this project https://github.com/jaihind213/using_log4j2-masker
