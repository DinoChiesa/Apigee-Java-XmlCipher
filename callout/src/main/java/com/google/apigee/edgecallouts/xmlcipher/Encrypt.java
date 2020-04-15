// Copyright 2018-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

// portions of this code are derived from other code which is
// under a different copyright.

/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.google.apigee.edgecallouts.xmlcipher;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;
import com.google.apigee.util.XmlUtils;
import java.security.Key;
import java.util.Map;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Encrypt extends XmlCipherCalloutBase implements Execution {

  public Encrypt(Map properties) {
    super(properties);
  }

  private static SecretKey generateKeyEncryptionKey() throws Exception {
    String jceAlgorithmName = "DESede";
    KeyGenerator keyGenerator = KeyGenerator.getInstance(jceAlgorithmName);
    SecretKey kek = keyGenerator.generateKey();
    return kek;
  }

  private static SecretKey generateDataEncryptionKey() throws Exception {
    KeyGenerator nonThreadSafe_AESKeyGenerator = KeyGenerator.getInstance("AES");
    nonThreadSafe_AESKeyGenerator.init(128);
    return nonThreadSafe_AESKeyGenerator.generateKey();
  }

  private byte[] execute0(Document document, String xpathPath) throws Exception {
    // 1. generate keys
    Key symmetricKey = generateDataEncryptionKey();
    Key keyEncryptingKey = generateKeyEncryptionKey();
    byte[] keyBytes = keyEncryptingKey.getEncoded();

    // 2. encrypt the key
    XMLCipher keyCipher = XMLCipher.getInstance(XMLCipher.TRIPLEDES_KeyWrap);
    keyCipher.init(XMLCipher.WRAP_MODE, keyEncryptingKey);
    EncryptedKey encryptedKey = keyCipher.encryptKey(document, symmetricKey);

    // 3. Initialize the cipher for encrypting the xml data
    XMLCipher xmlCipher = XMLCipher.getInstance(XMLCipher.AES_128);
    xmlCipher.init(XMLCipher.ENCRYPT_MODE, symmetricKey);

    // 4. set keyinfo inside the encrypted data being prepared
    EncryptedData encryptedData = xmlCipher.getEncryptedData();
    KeyInfo keyInfo = new KeyInfo(document);
    keyInfo.add(encryptedKey);
    encryptedData.setKeyInfo(keyInfo);

    // Encrypt the element
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
    Element toEncrypt =
        (Element)
            xpath.evaluate(
                xpathPath, // "/order/payment"
                document.getDocumentElement(),
                XPathConstants.NODE);

    /* Do the actual encryption. "false" below indicates that we want to
    encrypt the complete element and not only it's content. This replaces
    the existing element with the encrypted form. */
    xmlCipher.doFinal(document, toEncrypt, false);

    return keyBytes;
  }

  public ExecutionResult execute(final MessageContext msgCtxt, final ExecutionContext execContext) {
    try {
      Document document = getDocument(msgCtxt);
      String xpathPath = getSimpleRequiredProperty("xpath", msgCtxt);
      byte[] keyBytes = execute0(document, xpathPath);
      String result = XmlUtils.toString(document, getPretty(msgCtxt));
      String outputVar = getOutputVar(msgCtxt);
      msgCtxt.setVariable(outputVar, result);
      String encodedHex = Hex.encodeHexString(keyBytes);
      msgCtxt.setVariable(varName("keybytes_hex"), encodedHex);
      String encodedB64 = Base64.encodeBase64URLSafeString(keyBytes);
      msgCtxt.setVariable(varName("keybytes_b64"), encodedB64);
      return ExecutionResult.SUCCESS;
    } catch (javax.xml.xpath.XPathExpressionException texc1) {
      setExceptionVariables(texc1, msgCtxt);
      return ExecutionResult.ABORT;
    } catch (IllegalStateException exc1) {
      setExceptionVariables(exc1, msgCtxt);
      return ExecutionResult.ABORT;
    } catch (Exception e) {
      String stacktrace = getStackTraceAsString(e);
      if (getDebug()) {
        System.out.println(stacktrace);
      }
      setExceptionVariables(e, msgCtxt);
      msgCtxt.setVariable(varName("stacktrace"), stacktrace);
      return ExecutionResult.ABORT;
    }
  }
}
