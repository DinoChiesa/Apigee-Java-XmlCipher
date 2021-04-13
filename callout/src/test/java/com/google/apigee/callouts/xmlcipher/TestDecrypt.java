package com.google.apigee.callouts.xmlcipher;

import com.apigee.flow.execution.ExecutionResult;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDecrypt extends XmlCipherTestBase {

  private static final String simpleXml1_encrypted =
      "<?xml version='1.0' encoding='UTF-8'?>\n"
          + "<order>\n"
          + "        <customer customerNumber='0815A4711'>\n"
          + "                <name>Michael Sonntag</name>\n"
          + "                <address>\n"
          + "                        <street>Altenbergerstr. 69</street>\n"
          + "                        <ZIP>4040</ZIP>\n"
          + "                        <city>Linz</city>\n"
          + "                        <province>Upper Austria</province>\n"
          + "                        <country>Austria</country>\n"
          + "                        <email>sonntag@fim.uni-linz.ac.at</email>\n"
          + "                        <phone>+43(732)2468-9330</phone>\n"
          + "                        <fax>+43(732)2468-8599</fax>\n"
          + "                </address>\n"
          + "        </customer>\n"
          + "        <articles>\n"
          + "                <line>\n"
          + "                        <quantity unit='piece'>30</quantity>\n"
          + "                        <product productNumber='9907'>XML editing widget</product>\n"
          + "                        <price currency='EUR'>0.10</price>\n"
          + "                </line>\n"
          + "                <line>\n"
          + "                        <quantity unit='piece'>1</quantity>\n"
          + "                        <product productNumber='666'>Course supervisor handbook</product>\n"
          + "                        <price currency='EUR'>999.89</price>\n"
          + "                </line>\n"
          + "                <line>\n"
          + "                        <quantity unit='litre'>5</quantity>\n"
          + "                        <product productNumber='007'>Super juice</product>\n"
          + "                        <price currency='HUF'>500</price>\n"
          + "                </line>\n"
          + "        </articles>\n"
          + "        <delivery>\n"
          + "                <deliveryaddress>\n"
          + "                        <name>Michael Sonntag</name>\n"
          + "                        <address>\n"
          + "                                <street>Auf der Wies 18</street>\n"
          + "                                <ZIP>4040</ZIP>\n"
          + "                                <city>Linz</city>\n"
          + "                                <province>Upper Austria</province>\n"
          + "                                <country>Austria</country>\n"
          + "                                <phone>+43(676)3965166</phone>\n"
          + "                        </address>\n"
          + "                </deliveryaddress>\n"
          + "                <options>\n"
          + "                        <insurance>none</insurance>\n"
          + "                        <collection>1</collection>\n"
          + "                        <service>post</service>\n"
          + "                </options>\n"
          + "        </delivery>\n"
          + "        <xenc:EncryptedData xmlns:xenc='http://www.w3.org/2001/04/xmlenc#' Type='http://www.w3.org/2001/04/xmlenc#Element'><xenc:EncryptionMethod Algorithm='http://www.w3.org/2001/04/xmlenc#aes128-cbc'/><ds:KeyInfo xmlns:ds='http://www.w3.org/2000/09/xmldsig#'>\n"
          + "<xenc:EncryptedKey><xenc:EncryptionMethod Algorithm='http://www.w3.org/2001/04/xmlenc#kw-tripledes'/><xenc:CipherData><xenc:CipherValue>ZrDPveDO0SQC9hcPPv62eEAB4tX1NrVdAoxqkPfEweA=</xenc:CipherValue></xenc:CipherData></xenc:EncryptedKey></ds:KeyInfo><xenc:CipherData><xenc:CipherValue>9AsdSsGsvpvCosefVUfwZ8/2pSr4KvwJU6omAqgxmRJc5fxu7P6jhimgdjK31+JbngX304eIfol7&#13;\n"
          + "XdrBuokXO5z6FA9aJbcTVoYrbZC4PupMpyWnikWuqp49HQmBRyWZfHTOSOKbQIUrVTNv56Ze1Pdz&#13;\n"
          + "lszA0OVDYQ+64XmOEqsZW+GnrHTTLaFT9UJmPPgBrH1kT2U7BopiKnQzr1+yCauDT8hb2qHkC49w&#13;\n"
          + "v+2jEfVKIeQoCoOFkhjLoHX/4hE+vmKBilAwE9UCG2JDtoumYE0E65wp7k/DrPYKQgPXZ6lHV9tI&#13;\n"
          + "HUYY0+3cOpURha8+qFOWV48vZ0nbpOFuo5/Yo1D73hl8rPRcAY88toru7vGNF/K5V4NWpDeZzzds&#13;\n"
          + "GO/pE4zlTsWdlgtpkbFP3xy1zxrkRz4b/MQP8uoEPcHbAg60RlGnj4NGoGo7Dlc/RMm7Ego4ahuW&#13;\n"
          + "aTHwrJ/eyx4FGA==</xenc:CipherValue></xenc:CipherData></xenc:EncryptedData>\n"
          + "</order>\n";

  @Test
  public void test_EmptySource() throws Exception {
    String expectedError = "source variable resolves to null";
    msgCtxt.setVariable("message-content", simpleXml1_encrypted);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "not-message.content");

    Decrypt callout = new Decrypt(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.ABORT, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xmlcipher_error");
    Assert.assertNotNull(errorOutput, "errorOutput");
    // System.out.printf("expected error: %s\n", errorOutput);
    Assert.assertEquals(errorOutput, expectedError, "error not as expected");
    Object stacktrace = msgCtxt.getVariable("xmlcipher_stacktrace");
    Assert.assertNull(stacktrace, "EmptySource() stacktrace");
    System.out.println("=========================================================");
  }

  @Test
  public void test_MissingKeybytes() throws Exception {
    String expectedError = "you must specify kek or keybytes";

    msgCtxt.setVariable("message.content", simpleXml1_encrypted);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");

    Decrypt callout = new Decrypt(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.ABORT, "result not as expected");
    Object exception = msgCtxt.getVariable("xmlcipher_exception");
    Assert.assertNotNull(exception, "ValidResult() exception");
    Object errorOutput = msgCtxt.getVariable("xmlcipher_error");
    Assert.assertNotNull(errorOutput, "errorOutput");
    // System.out.printf("expected error: %s\n", errorOutput);
    Assert.assertEquals(errorOutput, expectedError, "error not as expected");
    Object stacktrace = msgCtxt.getVariable("xmlcipher_stacktrace");
    Assert.assertNull(stacktrace, "MissingKeybytes() stacktrace");
    System.out.println("=========================================================");
  }

  @Test
  public void test_RubbishKeybytes() throws Exception {
    String expectedError = "Invalid Hexadecimal Character: t";
    msgCtxt.setVariable("message.content", simpleXml1_encrypted);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");
    props.put("keybytes", "this-is-not-a-hex-encoded-string");

    Decrypt callout = new Decrypt(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.ABORT, "result not as expected");
    Object exception = msgCtxt.getVariable("xmlcipher_exception");
    Assert.assertNotNull(exception, "ValidResult() exception");
    Object errorOutput = msgCtxt.getVariable("xmlcipher_error");
    Assert.assertNotNull(errorOutput, "errorOutput");
    System.out.printf("expected error: %s\n", errorOutput);
    Assert.assertEquals(errorOutput, expectedError, "error not as expected");
    Object stacktrace = msgCtxt.getVariable("xmlcipher_stacktrace");
    Assert.assertNotNull(stacktrace, "RubbishKeybytes() stacktrace");
    System.out.println("=========================================================");
  }

  @Test
  public void test_BadKey1() throws Exception {
    String expectedError = "Invalid Hexadecimal Character: T";
    msgCtxt.setVariable("message.content", simpleXml1_encrypted);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");
    props.put("keybytes", "Tba2e9161919cd254383a454253116ab4637ea0164ce1670");

    Decrypt callout = new Decrypt(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.ABORT, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xmlcipher_error");
    Assert.assertEquals(errorOutput, expectedError, "errorOutput");
    Object exception = msgCtxt.getVariable("xmlcipher_exception");
    Assert.assertNotNull(exception, "BadKey1() exception");
    Object stacktrace = msgCtxt.getVariable("xmlcipher_stacktrace");
    Assert.assertNotNull(stacktrace, "BadKey1() stacktrace");
    System.out.println("=========================================================");
  }

  @Test
  public void test_BadKey2() throws Exception {
    String expectedError = "Wrong key size";
    msgCtxt.setVariable("message.content", simpleXml1_encrypted);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");
    props.put("keybytes", "cba2e9161919cd254383a454253116ab4637ea0164ce");

    Decrypt callout = new Decrypt(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.ABORT, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xmlcipher_error");
    Assert.assertEquals(errorOutput, expectedError, "errorOutput");
    Object exception = msgCtxt.getVariable("xmlcipher_exception");
    Assert.assertNotNull(exception, "BadKey2() exception");
    Object stacktrace = msgCtxt.getVariable("xmlcipher_stacktrace");
    Assert.assertNull(stacktrace, "BadKey2() stacktrace");
    System.out.println("=========================================================");
  }

  @Test
  public void test_ValidResult() throws Exception {
    msgCtxt.setVariable("message.content", simpleXml1_encrypted);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");
    props.put("keybytes", "cba2e9161919cd254383a454253116ab4637ea0164ce1670");

    Decrypt callout = new Decrypt(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.SUCCESS, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xmlcipher_error");
    Assert.assertNull(errorOutput, "errorOutput");
    Object exception = msgCtxt.getVariable("xmlcipher_exception");
    Assert.assertNull(exception, "ValidResult() exception");
    Object stacktrace = msgCtxt.getVariable("xmlcipher_stacktrace");
    Assert.assertNull(stacktrace, "ValidResult() stacktrace");
    Object xmlOutput = msgCtxt.getVariable("message.content");
    System.out.printf("xml output: %s\n", xmlOutput);
    System.out.println("=========================================================");
  }
}
