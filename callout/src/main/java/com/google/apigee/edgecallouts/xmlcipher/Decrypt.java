// Copyright 2018 Google LLC
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
import com.apigee.flow.message.Message;
import com.apigee.flow.message.MessageContext;
import com.google.apigee.util.XmlUtils;
import java.security.Key;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.utils.EncryptionConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Decrypt extends XmlCipherCalloutBase implements Execution {

    public Decrypt(Map properties) {
        super(properties);
    }

    private static SecretKey loadKeyEncryptionKey(final byte[] keyBytes) throws Exception {
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DESede");
        DESedeKeySpec keySpec = new DESedeKeySpec(keyBytes);
        SecretKey key = skf.generateSecret(keySpec);
        return key;
    }

    private void execute0(Document document, byte[] keyBytes) throws Exception {

            /* Find the encrypted data element: retrieve the first encrypted
               element by its name and namespace */
            Element encryptedDataElement = (Element) document.getElementsByTagNameNS(EncryptionConstants.EncryptionSpecNS,EncryptionConstants._TAG_ENCRYPTEDDATA).item(0);

            /* Load the master key to be used for decrypting the xml data encryption key */
            Key kek = loadKeyEncryptionKey(keyBytes);

            /* The key to be used for decrypting xml data would be obtained from the
               keyinfo of the EncrypteData using the kek. */
            XMLCipher xmlCipher = XMLCipher.getInstance();
            xmlCipher.init(XMLCipher.DECRYPT_MODE, null);
            xmlCipher.setKEK(kek);

            /* The following doFinal call replaces the encrypted data with decrypted
               contents in the document */
            xmlCipher.doFinal(document, encryptedDataElement);
    }

    public ExecutionResult execute (final MessageContext msgCtxt,
                                    final ExecutionContext execContext) {
        try {
            Document document = getDocument(msgCtxt);
            String keyBytesHexString = getSimpleRequiredProperty("keybytes", msgCtxt);
            byte[] keyBytes = Hex.decodeHex(keyBytesHexString.toCharArray());
            execute0(document, keyBytes);
            String result = XmlUtils.toString(document, getPretty(msgCtxt));
            String outputVar = getOutputVar(msgCtxt);
            msgCtxt.setVariable(outputVar, result);
            return ExecutionResult.SUCCESS;
        }
        catch (java.security.InvalidKeyException exc0) {
            setExceptionVariables(exc0, msgCtxt);
            return ExecutionResult.ABORT;
        }
        catch (org.apache.commons.codec.DecoderException exc1) {
            setExceptionVariables(exc1, msgCtxt);
            return ExecutionResult.ABORT;
        }
        catch (IllegalStateException exc2) {
            setExceptionVariables(exc2, msgCtxt);
            return ExecutionResult.ABORT;
        }
        catch (Exception e) {
            if (getDebug()) {
                System.out.println(ExceptionUtils.getStackTrace(e));
            }
            setExceptionVariables(e,msgCtxt);
            msgCtxt.setVariable(varName("stacktrace"), ExceptionUtils.getStackTrace(e));
            return ExecutionResult.ABORT;
        }
    }

}
