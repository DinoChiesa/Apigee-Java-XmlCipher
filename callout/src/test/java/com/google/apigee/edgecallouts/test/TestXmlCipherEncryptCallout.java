package com.google.apigee.edgecallouts.test;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;

import mockit.Mock;
import mockit.MockUp;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.message.Message;

import com.google.apigee.edgecallouts.xmlcipher.Encrypt;

public class TestXmlCipherEncryptCallout {
    //private final static String testDataDir = "src/test/resources/test-data";

    MessageContext msgCtxt;
    InputStream messageContentStream;
    Message message;
    ExecutionContext exeCtxt;

    @BeforeMethod()
    public void beforeMethod() {

        msgCtxt = new MockUp<MessageContext>() {
            private Map variables;
            public void $init() {
                variables = new HashMap();
            }

            @Mock()
            public <T> T getVariable(final String name){
                if (variables == null) {
                    variables = new HashMap();
                }
                return (T) variables.get(name);
            }

            @Mock()
            public boolean setVariable(final String name, final Object value) {
                if (variables == null) {
                    variables = new HashMap();
                }
                variables.put(name, value);
                return true;
            }

            @Mock()
            public boolean removeVariable(final String name) {
                if (variables == null) {
                    variables = new HashMap();
                }
                if (variables.containsKey(name)) {
                    variables.remove(name);
                }
                return true;
            }

            @Mock()
            public Message getMessage() {
                return message;
            }
        }.getMockInstance();

        exeCtxt = new MockUp<ExecutionContext>(){ }.getMockInstance();

        message = new MockUp<Message>(){
            @Mock()
            public InputStream getContentAsStream() {
                // new ByteArrayInputStream(messageContent.getBytes(StandardCharsets.UTF_8));
                return messageContentStream;
            }
        }.getMockInstance();
    }

    
    private static final String simpleXml1 =
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<order>\n" +
"        <customer customerNumber='0815A4711'>\n" +
"                <name>Michael Sonntag</name>\n" +
"                <address>\n" +
"                        <street>Altenbergerstr. 69</street>\n" +
"                        <ZIP>4040</ZIP>\n" +
"                        <city>Linz</city>\n" +
"                        <province>Upper Austria</province>\n" +
"                        <country>Austria</country>\n" +
"                        <email>sonntag@fim.uni-linz.ac.at</email>\n" +
"                        <phone>+43(732)2468-9330</phone>\n" +
"                        <fax>+43(732)2468-8599</fax>\n" +
"                </address>\n" +
"        </customer>\n" +
"        <articles>\n" +
"                <line>\n" +
"                        <quantity unit='piece'>30</quantity>\n" +
"                        <product productNumber='9907'>XML editing widget</product>\n" +
"                        <price currency='EUR'>0.10</price>\n" +
"                </line>\n" +
"                <line>\n" +
"                        <quantity unit='piece'>1</quantity>\n" +
"                        <product productNumber='666'>Course supervisor handbook</product>\n" +
"                        <price currency='EUR'>999.89</price>\n" +
"                </line>\n" +
"                <line>\n" +
"                        <quantity unit='litre'>5</quantity>\n" +
"                        <product productNumber='007'>Super juice</product>\n" +
"                        <price currency='HUF'>500</price>\n" +
"                </line>\n" +
"        </articles>\n" +
"        <delivery>\n" +
"                <deliveryaddress>\n" +
"                        <name>Michael Sonntag</name>\n" +
"                        <address>\n" +
"                                <street>Auf der Wies 18</street>\n" +
"                                <ZIP>4040</ZIP>\n" +
"                                <city>Linz</city>\n" +
"                                <province>Upper Austria</province>\n" +
"                                <country>Austria</country>\n" +
"                                <phone>+43(676)3965166</phone>\n" +
"                        </address>\n" +
"                </deliveryaddress>\n" +
"                <options>\n" +
"                        <insurance>none</insurance>\n" +
"                        <collection>1</collection>\n" +
"                        <service>post</service>\n" +
"                </options>\n" +
"        </delivery>\n" +
"        <payment type='CC'>\n" +
"                <creditcard issuer='Mastercard'>\n" +
"                        <nameOnCard>Mag. Dipl.-Ing. Dr. Michael Sonntag</nameOnCard>\n" +
"                        <number>5201 2345 6789 0123</number>\n" +
"                        <expiryDate>2006-04-30</expiryDate>\n" +
"                </creditcard>\n" +
"        </payment>\n" +
"</order>\n";
        

    @Test
    public void test_EmptySource() throws Exception {
        String expectedError = "source variable resolves to null";
        msgCtxt.setVariable("message-content", simpleXml1);

        Map<String,String> props = new HashMap<String,String>();
        props.put("source","not-message.content");

        Encrypt callout = new Encrypt(props);

        // execute and retrieve output
        ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
        Assert.assertEquals(actualResult, ExecutionResult.ABORT, "result not as expected");
        Object errorOutput = msgCtxt.getVariable("xmlcipher_error");
        Assert.assertNotNull(errorOutput, "errorOutput");
        //System.out.printf("expected error: %s\n", errorOutput);
        Assert.assertEquals(errorOutput, expectedError, "error not as expected");
        Object stacktrace =  msgCtxt.getVariable("xmlcipher_stacktrace");
        Assert.assertNull(stacktrace, "EmptySource() stacktrace");
        System.out.println("=========================================================");
    }
    
    @Test
    public void test_MissingXpath() throws Exception {
        String expectedError = "xpath resolves to an empty string";
        
        msgCtxt.setVariable("message.content", simpleXml1);

        Map<String,String> props = new HashMap<String,String>();
        props.put("source","message.content");

        Encrypt callout = new Encrypt(props);

        // execute and retrieve output
        ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
        Assert.assertEquals(actualResult, ExecutionResult.ABORT, "result not as expected");
        Object exception = msgCtxt.getVariable("xmlcipher_exception");
        Assert.assertNotNull(exception, "ValidResult() exception");
        Object errorOutput = msgCtxt.getVariable("xmlcipher_error");
        Assert.assertNotNull(errorOutput, "errorOutput");
        //System.out.printf("expected error: %s\n", errorOutput);
        Assert.assertEquals(errorOutput, expectedError, "error not as expected");
        Object stacktrace =  msgCtxt.getVariable("xmlcipher_stacktrace");
        Assert.assertNull(stacktrace, "MissingXpath() stacktrace");
        System.out.println("=========================================================");
    }
    
    @Test
    public void test_RubbishXpath() throws Exception {
        String expectedError = "Unknown error in XPath.";
        msgCtxt.setVariable("message.content", simpleXml1);

        Map<String,String> props = new HashMap<String,String>();
        props.put("source","message.content");
        props.put("xpath","$%rubbish-here");

        Encrypt callout = new Encrypt(props);

        // execute and retrieve output
        ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
        Assert.assertEquals(actualResult, ExecutionResult.ABORT, "result not as expected");
        Object exception = msgCtxt.getVariable("xmlcipher_exception");
        Assert.assertNotNull(exception, "ValidResult() exception");
        Object errorOutput = msgCtxt.getVariable("xmlcipher_error");
        Assert.assertNotNull(errorOutput, "errorOutput");
        System.out.printf("expected error: %s\n", errorOutput);
        Assert.assertEquals(errorOutput, expectedError, "error not as expected");
        Object stacktrace =  msgCtxt.getVariable("xmlcipher_stacktrace");
        Assert.assertNull(stacktrace, "RubbishXpath() stacktrace");
        System.out.println("=========================================================");
    }
    
    @Test
    public void test_ValidResult() throws Exception {
        msgCtxt.setVariable("message.content", simpleXml1);

        Map<String,String> props = new HashMap<String,String>();
        props.put("source","message.content");
        props.put("xpath","/order/payment");

        Encrypt callout = new Encrypt(props);

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
        Object keybytes = msgCtxt.getVariable("xmlcipher_keybytes_hex");
        Assert.assertNotNull(keybytes, "ValidResult() keybytes");
        System.out.printf("keybytes: %s\n", keybytes);
        System.out.println("=========================================================");
    }

}
