package com.google.apigee.callouts.xmlcipher;

import com.apigee.flow.execution.ExecutionResult;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestRoundtrip extends XmlCipherTestBase {

  private static final String simpleXml1 =
      ""
          + "<?xml version='1.0' encoding='UTF-8'?>\n"
          + "<order>\n"
          + "  <customer customerNumber='0815A4711'>\n"
          + "    <name>Michael Sonntag</name>\n"
          + "    <address>\n"
          + "      <street>Altenbergerstr. 69</street>\n"
          + "      <ZIP>4040</ZIP>\n"
          + "      <city>Linz</city>\n"
          + "      <province>Upper Austria</province>\n"
          + "      <country>Austria</country>\n"
          + "      <phone>+43(732)2468-9330</phone>\n"
          + "    </address>\n"
          + "  </customer>\n"
          + "  <payment type='CC'>\n"
          + "    <creditcard issuer='Mastercard'>\n"
          + "      <nameOnCard>Mag. Dipl.-Ing. Dr. Michael Sonntag</nameOnCard>\n"
          + "      <number>5201 2345 6789 0123</number>\n"
          + "      <expiryDate>2006-04-30</expiryDate>\n"
          + "    </creditcard>\n"
          + "  </payment>\n"
          + "</order>\n";

  @Test
  public void generatedKey() throws Exception {
    msgCtxt.setVariable("message.content", simpleXml1);

    Map<String, String> props = new HashMap<String, String>();
    props.put("debug", "true");
    props.put("source", "message.content");
    props.put("xpath", "/order/payment");

    Encrypt encrypt = new Encrypt(props);

    // execute and retrieve output
    ExecutionResult encryptResult = encrypt.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(encryptResult, ExecutionResult.SUCCESS, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xmlcipher_error");
    Assert.assertNull(errorOutput, "errorOutput");
    Object exception = msgCtxt.getVariable("xmlcipher_exception");
    Assert.assertNull(exception, "generatedKey() exception");
    Object stacktrace = msgCtxt.getVariable("xmlcipher_stacktrace");
    Assert.assertNull(stacktrace, "generatedKey() stacktrace");
    Object xmlOutput = msgCtxt.getVariable("message.content");
    System.out.printf("xml output: %s\n", xmlOutput);
    String keybytes = (String) msgCtxt.getVariable("xmlcipher_keybytes_hex");
    Assert.assertNotNull(keybytes, "generatedKey() keybytes");
    System.out.printf("keybytes: %s\n", keybytes);

    props.put("keybytes", keybytes);

    Decrypt decrypt = new Decrypt(props);
    ExecutionResult decryptResult = decrypt.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(decryptResult, ExecutionResult.SUCCESS, "result not as expected");

    String originalContent = (String) msgCtxt.getVariable("message.content");
    Assert.assertNotNull(originalContent, "content");
    System.out.printf("content: %s\n", originalContent);
    System.out.println("=========================================================");
  }

  @Test
  public void explicitKey_Base16_A() throws Exception {
    msgCtxt.setVariable("message.content", simpleXml1);
    String keyBytes = "cba2e9161919cd254383a454253116ab4637ea0164ce1670";
    Map<String, String> props = new HashMap<String, String>();
    props.put("debug", "true");
    props.put("source", "message.content");
    props.put("xpath", "/order/payment");
    props.put("kek", keyBytes);

    Encrypt encrypt = new Encrypt(props);

    // execute and retrieve output
    ExecutionResult encryptResult = encrypt.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(encryptResult, ExecutionResult.SUCCESS, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xmlcipher_error");
    Assert.assertNull(errorOutput, "errorOutput");
    Object exception = msgCtxt.getVariable("xmlcipher_exception");
    Assert.assertNull(exception, "exception");
    Object stacktrace = msgCtxt.getVariable("xmlcipher_stacktrace");
    Assert.assertNull(stacktrace, "stacktrace");
    Object xmlOutput = msgCtxt.getVariable("message.content");
    System.out.printf("xml output: %s\n", xmlOutput);
    Object outputKeybytes = msgCtxt.getVariable("xmlcipher_keybytes_hex");
    Assert.assertNull(outputKeybytes, "outputKeybytes");

    props.put("keybytes", keyBytes);

    Decrypt decrypt = new Decrypt(props);
    ExecutionResult decryptResult = decrypt.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(decryptResult, ExecutionResult.SUCCESS, "result not as expected");

    String originalContent = (String) msgCtxt.getVariable("message.content");
    Assert.assertNotNull(originalContent, "content");
    System.out.printf("content: %s\n", originalContent);
    System.out.println("=========================================================");
  }

  @Test
  public void explicitKey_Base16_B() throws Exception {
    msgCtxt.setVariable("message.content", simpleXml1);
    String keyBytes = "cba2e9161919cd254383a454253116ab4637ea0164ce1670";
    Map<String, String> props = new HashMap<String, String>();
    props.put("debug", "true");
    props.put("source", "message.content");
    props.put("xpath", "/order/payment");
    props.put("kek", keyBytes);

    Encrypt encrypt = new Encrypt(props);

    // execute and retrieve output
    ExecutionResult encryptResult = encrypt.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(encryptResult, ExecutionResult.SUCCESS, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xmlcipher_error");
    Assert.assertNull(errorOutput, "errorOutput");
    Object exception = msgCtxt.getVariable("xmlcipher_exception");
    Assert.assertNull(exception, "exception");
    Object stacktrace = msgCtxt.getVariable("xmlcipher_stacktrace");
    Assert.assertNull(stacktrace, "stacktrace");
    Object xmlOutput = msgCtxt.getVariable("message.content");
    System.out.printf("xml output: %s\n", xmlOutput);
    Object outputKeybytes = msgCtxt.getVariable("xmlcipher_keybytes_hex");
    Assert.assertNull(outputKeybytes, "outputKeybytes");

    props.put("kek", keyBytes);

    Decrypt decrypt = new Decrypt(props);
    ExecutionResult decryptResult = decrypt.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(decryptResult, ExecutionResult.SUCCESS, "result not as expected");

    String originalContent = (String) msgCtxt.getVariable("message.content");
    Assert.assertNotNull(originalContent, "content");
    System.out.printf("content: %s\n", originalContent);
    System.out.println("=========================================================");
  }
}
