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
