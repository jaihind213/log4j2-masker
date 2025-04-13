# log4j2-masker

**log4j2-masker** is a lightweight Java library that automatically censors sensitive information (like passwords, API keys, and tokens) from being logged in your Log4j2 logs.

> Prevent secrets from leaking into log files — safely and automatically.

---

## ✨ Features

- 🚫 Masks passwords, secrets, tokens, and other sensitive fields
- ⚙️ Plug-and-play integration with Log4j2
- 🔒 Keeps your logs clean and secure
- 📦 works with your shaded JAR with all dependencies with a minor tweak to pom.xml

---

## How does it work ?

Say you have the following java code

```java

        Properties configs = new Properties();
        //We wish to censor these properties
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
ex: in our example 'admin' is the username, hence the word 'admin' will censored where ever it appears in the log message.


## log4j2 support

| log4j2 version | log4j2-masker version |
|----------------|-----------------------|
| 2.20.0         | 2.20.0                |
| < 2.20.0       | not available         |

## 📦 Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>io.github.jaihind213</groupId>
  <artifactId>log4j2-masker</artifactId>
  <version>2.20.0</version>
</dependency>
```
- If you are not using fat jar, add the log4j2-masker jar to your classpath else

- If you are using mvn shade plugin in maven, please add the following configuration to shade plugin:

For  2.20.0 log4j2 version  add the following

```xml
          <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.2</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <shadedArtifactAttached>true</shadedArtifactAttached>
                            <shadedClassifierName>jar-with-dependencies</shadedClassifierName>
                            <filters>
                            </filters>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                                <!--we need to add this, else our masking log4j plugin removes all the default plugins in Log4j2Plugins.dat. -->
                                <!--by this doing, all the default ones are kept and our masking plugin is added to list in Log4j2Plugins.dat -->
                                <transformer
                                        implementation="io.github.edwgiz.log4j.maven.plugins.shade.transformer.Log4j2PluginCacheFileTransformer">
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
                <dependencies>
                    <dependency>
                        <groupId>io.github.edwgiz</groupId>
                        <artifactId>log4j-maven-shade-plugin-extensions</artifactId>
                        <version>2.20.0</version>
                    </dependency>
                </dependencies>
            </plugin>
```

if you are using log4j2 version > 2.20.0 add the following to your pom.xml 
```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <version>3.4.1</version>
  <dependencies>
    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-transform-maven-shade-plugin-extensions</artifactId>
      <version>0.2.0</version>
    </dependency>
  </dependencies>
  <executions>
    <execution>
      <id>shade-jar-with-dependencies</id>
      <phase>package</phase>
      <goals>
        <goal>shade</goal>
      </goals>
      <configuration>
        <transformers>
          <transformer implementation="org.apache.logging.log4j.maven.plugins.shade.transformer.Log4j2PluginCacheFileTransformer"/>
          <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
          <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
            <manifestEntries>
              <Multi-Release>true</Multi-Release>
            </manifestEntries>
          </transformer>
        </transformers>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## live Examples 

1. refer to the unit test in the project.
2. refer to the github project <todo> 