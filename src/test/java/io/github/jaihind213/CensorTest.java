package io.github.jaihind213;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.io.*;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.Assert;
import org.junit.Test;

public class CensorTest {

  private static final Logger logger = getLogger(CensorTest.class.getName());

  @Test
  public void testCensoringPasswdWhileLoggingConfigs() throws IOException {
    //
    LoggerContext context = (LoggerContext) LogManager.getContext(false);
    Configuration config = context.getConfiguration();

    //
    String sensitiveConfigNames =
        config.getStrSubstitutor().replace("${sensitive_configs:-please_define_it}");
    Assert.assertNotEquals("please_define_it", sensitiveConfigNames);

    final String secretPassword = "@123$\\अ _";
    final String nonSensitiveWord = "its_ok_to_print_this";

    // prepare test configs
    Properties configs = new Properties();
    for (String configName : sensitiveConfigNames.split(",")) {
      configs.put("db.sensitive.config.parameter." + configName, secretPassword);
    }
    configs.put("db.non_sensitive.config.parameter", nonSensitiveWord);

    try (FileOutputStream output = new FileOutputStream("/tmp/secret.properties", false)) {
      configs.store(output, "Config with secrets for testing");
      System.out.println("secret Properties saved to /tmp/secret.properties");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // load from file by calling loadConfigs
    Properties loadedConfigs = new Properties();
    loadedConfigs.load(new FileInputStream("/tmp/secret.properties"));

    // ask Logcensor to detect sensitive configs
    LogCensor.detectSensitiveConfigs(loadedConfigs);

    logger.info(
        "trying to print secretPassword (u will see it in console but not in file)"
            + secretPassword
            + " in log file");
    logger.info(
        "trying to print configs (u will see secrets in console but not in file)" + loadedConfigs);

    // check if in log file, the word secret_password is replaced by ****
    boolean normalWordFound = false;
    try (BufferedReader reader = new BufferedReader(new FileReader("/tmp/censor.log"))) {
      String line;
      int lineNumber = 0;

      while ((line = reader.readLine()) != null) {
        lineNumber++;
        if (line.contains(secretPassword)) {
          Assert.fail(
              "Censoring not working, Found secret password in log file /tmp/censor.log: " + line);
          System.out.println("Found '" + secretPassword + "' on line " + lineNumber + ": " + line);
        }
        if (line.contains(nonSensitiveWord)) {
          normalWordFound = true;
        }
      }
    } catch (IOException e) {
      throw e;
    }
    Assert.assertTrue("NON Sensitive word NOT found in log file", normalWordFound);
  }
}
