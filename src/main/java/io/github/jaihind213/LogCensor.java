package io.github.jaihind213;

import java.util.*;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.message.Message;

/**
 * Log4j event pattern convertor that censors sensitive data in log messages where ever it finds a
 * 'sensitive word' being logged, it will mask it.
 */
@Plugin(name = "LogCensor", category = "Converter")
@ConverterKeys({"censor"})
public class LogCensor extends LogEventPatternConverter {

  private static volatile Pattern SENSITIVE_PATTERN = null;

  private final boolean isEnabled;

  protected LogCensor(String[] options) {
    super("censor", "censor");
    isEnabled =
        options != null && options.length > 0 && Arrays.asList(options).contains("enabled=true");
    // one LogCensor is created per appender.
    String optionsStr = options != null ? Arrays.toString(options) : "null";
    System.err.println(
        "LogCensor for logger created. enabled: " + isEnabled + " .options are: " + optionsStr);
  }

  public static LogCensor newInstance(final String[] options) {
    return new LogCensor(options);
  }

  @Override
  public void format(LogEvent event, StringBuilder toAppendTo) {
    if (event == null) {
      return;
    }
    Message messageObj = event.getMessage();
    if (messageObj == null) {
      return;
    }

    final String formattedMsgStr = messageObj.getFormattedMessage();
    if (!isEnabled || SENSITIVE_PATTERN == null) {
      toAppendTo.append(formattedMsgStr);
      return;
    }
    final String maskedMessage = SENSITIVE_PATTERN.matcher(formattedMsgStr).replaceAll("******");
    toAppendTo.append(maskedMessage);
  }

  /**
   * Detects sensitive configs in the properties and sets the pattern to mask them. Suggest to call
   * this as soon as configs are loaded/read.
   *
   * @param configs properties object.
   */
  public static void detectSensitiveConfigs(Properties configs) {
    LoggerContext context = (LoggerContext) LogManager.getContext(false);
    Configuration config = context.getConfiguration();
    // search for 'property.sensitive_configs = client_id,access_key,password,passwd,username' in
    // log4j2.properties
    String sensitiveConfigKeyWords =
        config
            .getStrSubstitutor()
            .replace(
                "${sensitive_configs:-passwd,key,access,secret,apikey,api_key,credential,token,auth,signature,passphrase,client_id,client.id,client_secret,client.secret,authorization,bearer,user_name,username,user,pass,password}");

    Set<String> dontPrintThis = new HashSet<>();

    for (String sensitiveConfigParamName : sensitiveConfigKeyWords.split(",")) {
      for (String key : configs.stringPropertyNames()) {
        if (key.toLowerCase().contains(sensitiveConfigParamName.toLowerCase())) {
          Object sensitiveValue = configs.get(key);
          if (sensitiveValue != null && sensitiveValue.toString().trim().length() > 0) {
            // don't add empty string
            dontPrintThis.add(sensitiveValue.toString());
          }
        }
      }
    }
    if (dontPrintThis.isEmpty()) {
      System.out.println(
          "No sensitive data found in the properties. will use default pattern for censoring.");
      return;
    }

    String patternContainingSecretsNeverPrint =
        "(?i)("
            + dontPrintThis.stream()
                .map(Pattern::quote) // escape all special characters
                .reduce((a, b) -> a + "|" + b)
                .orElse("")
            + ")";
    dontPrintThis.clear();
    setPattern(patternContainingSecretsNeverPrint);
  }

  private static void setPattern(String pattern) {
    if (pattern == null || pattern.trim().length() == 0) {
      return;
    }
    SENSITIVE_PATTERN = Pattern.compile(pattern);
  }
}
