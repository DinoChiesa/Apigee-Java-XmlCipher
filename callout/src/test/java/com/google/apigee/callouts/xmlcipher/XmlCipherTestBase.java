package com.google.apigee.callouts.xmlcipher;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.message.Message;
import com.apigee.flow.message.MessageContext;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import mockit.Mock;
import mockit.MockUp;
import org.testng.annotations.BeforeMethod;

public abstract class XmlCipherTestBase {

  protected MessageContext msgCtxt;
  protected InputStream messageContentStream;
  protected Message message;
  protected ExecutionContext exeCtxt;

  @BeforeMethod()
  public void beforeMethod() {

    msgCtxt =
        new MockUp<MessageContext>() {
          private Map variables;

          public void $init() {
            variables = new HashMap();
          }

          @Mock()
          public <T> T getVariable(final String name) {
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

    exeCtxt = new MockUp<ExecutionContext>() {}.getMockInstance();

    message =
        new MockUp<Message>() {
          @Mock()
          public InputStream getContentAsStream() {
            // new ByteArrayInputStream(messageContent.getBytes(StandardCharsets.UTF_8));
            return messageContentStream;
          }
        }.getMockInstance();
  }
}
