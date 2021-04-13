// Copyright 2018-2021 Google LLC
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

package com.google.apigee.callouts.xmlcipher;

import com.apigee.flow.message.MessageContext;
import com.google.apigee.encoding.Base16;
import com.google.apigee.util.XmlUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import org.w3c.dom.Document;

public abstract class XmlCipherCalloutBase {
  private static final String _varprefix = "xmlcipher_";
  private Map properties; // read-only
  private static final String variableReferencePatternString = "(.*?)\\{([^\\{\\} ]+?)\\}(.*?)";
  private static final Pattern variableReferencePattern =
      Pattern.compile(variableReferencePatternString);
  private static final Pattern commonErrorPattern = Pattern.compile("^(.+?)[:;] (.+)$");
  protected static final String DESede_jceAlgorithmName = "DESede";

  enum EncodingType {
    NOT_SPECIFIED,
    BASE64,
    BASE64URL,
    BASE16
  };

  class KeyEncryptingKeyResult {
    public KeyEncryptingKeyResult(SecretKey key, boolean isGenerated) {
      this.isGenerated = isGenerated;
      this.keyEncryptingKey = key;
    }

    public boolean isGenerated;
    public SecretKey keyEncryptingKey;
  }

  static {
    org.apache.xml.security.Init.init();
  }

  public XmlCipherCalloutBase(Map properties) {
    this.properties = properties;
  }

  static String varName(String s) {
    return _varprefix + s;
  }

  protected Document getDocument(MessageContext msgCtxt) throws Exception {
    String source = getSimpleOptionalProperty("source", msgCtxt);
    if (source == null) {
      return XmlUtils.parseXml(msgCtxt.getMessage().getContentAsStream());
    }
    String text = (String) msgCtxt.getVariable(source);
    if (text == null) {
      throw new IllegalStateException("source variable resolves to null");
    }
    return XmlUtils.parseXml(text);
  }

  protected boolean getPretty(MessageContext msgCtxt) throws Exception {
    String pretty = getSimpleOptionalProperty("pretty", msgCtxt);
    if (pretty == null) return false;
    pretty = pretty.toLowerCase();
    return pretty.equals("true");
  }

  protected boolean getDebug() {
    String value = (String) this.properties.get("debug");
    if (value == null) return false;
    if (value.trim().toLowerCase().equals("true")) return true;
    return false;
  }

  protected String getOutputVar(MessageContext msgCtxt) throws Exception {
    String dest = getSimpleOptionalProperty("output-variable", msgCtxt);
    if (dest == null) {
      return "message.content";
    }
    return dest;
  }

  protected String getSimpleOptionalProperty(String propName, MessageContext msgCtxt)
      throws Exception {
    String value = (String) this.properties.get(propName);
    if (value == null) {
      return null;
    }
    value = value.trim();
    if (value.equals("")) {
      return null;
    }
    value = resolvePropertyValue(value, msgCtxt);
    if (value == null || value.equals("")) {
      return null;
    }
    return value;
  }

  protected String getSimpleRequiredProperty(String propName, MessageContext msgCtxt)
      throws Exception {
    String value = (String) this.properties.get(propName);
    if (value == null) {
      throw new IllegalStateException(propName + " resolves to an empty string");
    }
    value = value.trim();
    if (value.equals("")) {
      throw new IllegalStateException(propName + " resolves to an empty string");
    }
    value = resolvePropertyValue(value, msgCtxt);
    if (value == null || value.equals("")) {
      throw new IllegalStateException(propName + " resolves to an empty string");
    }
    return value;
  }

  protected byte[] decodeString(String s, EncodingType decodingKind) throws Exception {
    if (decodingKind == EncodingType.BASE16) {
      return Base16.decode(s);
    }
    if (decodingKind == EncodingType.BASE64) {
      return Base64.getDecoder().decode(s);
    }
    if (decodingKind == EncodingType.BASE64URL) {
      return Base64.getUrlDecoder().decode(s);
    }
    return s.getBytes(StandardCharsets.UTF_8);
  }

  protected String encodeString(byte[] a, EncodingType encodingKind) throws Exception {
    if (encodingKind == EncodingType.BASE16) {
      return Base16.encode(a);
    }
    if (encodingKind == EncodingType.BASE64) {
      return Base64.getEncoder().encodeToString(a);
    }
    if (encodingKind == EncodingType.BASE64URL) {
      return Base64.getUrlEncoder().encodeToString(a);
    }
    return new String(a, StandardCharsets.UTF_8);
  }

  protected static SecretKey toDESedeKey(final byte[] keyBytes) throws Exception {
    SecretKeyFactory skf = SecretKeyFactory.getInstance(DESede_jceAlgorithmName);
    DESedeKeySpec keySpec = new DESedeKeySpec(keyBytes);
    SecretKey key = skf.generateSecret(keySpec);
    return key;
  }

  private static SecretKey generateKeyEncryptionKey() throws Exception {
    KeyGenerator keyGenerator = KeyGenerator.getInstance(DESede_jceAlgorithmName);
    SecretKey kek = keyGenerator.generateKey();
    return kek;
  }

  private EncodingType getKekEncoding(MessageContext msgCtxt) throws Exception {
    String kekEncoding = getSimpleOptionalProperty("kek-encoding", msgCtxt);
    if (kekEncoding == null) {
      return EncodingType.BASE16;
    }
    kekEncoding = kekEncoding.trim().toLowerCase();

    if (kekEncoding.equals("base16") || kekEncoding.equals("hex")) {
      return EncodingType.BASE16;
    }
    if (kekEncoding.equals("base64")) {
      return EncodingType.BASE64;
    }
    if (kekEncoding.equals("base64url")) {
      return EncodingType.BASE64URL;
    }
    return EncodingType.NOT_SPECIFIED;
  }

  protected KeyEncryptingKeyResult getKeyEncryptingKey(MessageContext msgCtxt, boolean forEncrypt)
      throws Exception {
    // for backward compatibility
    if (!forEncrypt) {
      String keyBytesHexString = getSimpleOptionalProperty("keybytes", msgCtxt);
      if (keyBytesHexString != null) {
        final byte[] keyBytes = decodeString(keyBytesHexString, EncodingType.BASE16);
        return new KeyEncryptingKeyResult(toDESedeKey(keyBytes), false);
      }
    }

    // maybe specified, and maybe generated
    String kekString = getSimpleOptionalProperty("kek", msgCtxt);
    if (kekString == null) {
      if (forEncrypt) {
        return new KeyEncryptingKeyResult(generateKeyEncryptionKey(), true);
      } else {
        throw new IllegalStateException("you must specify kek or keybytes");
      }
    }

    EncodingType kekEncoding = getKekEncoding(msgCtxt);

    // get key bytes, 16 bytes for ABA,  24 bytes for ABC
    // see https://crypto.stackexchange.com/questions/24664/key-sizes-supported-by-3des
    final byte[] originalKeyBytes = decodeString(kekString, kekEncoding);
    final byte[] finalKeyBytes = Arrays.copyOf(originalKeyBytes, 24);
    if (originalKeyBytes.length < 24) {
      // ABA
      for (int j = 0, k = 16; j < 8; ) {
        finalKeyBytes[k++] = finalKeyBytes[j++];
      }
    }
    final SecretKey kek = toDESedeKey(finalKeyBytes);
    return new KeyEncryptingKeyResult(kek, false);
  }

  // If the value of a property contains any pairs of curlies,
  // eg, {apiproxy.name}, then "resolve" the value by de-referencing
  // the context variables whose names appear between curlies.
  private String resolvePropertyValue(String spec, MessageContext msgCtxt) {
    Matcher matcher = variableReferencePattern.matcher(spec);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(sb, "");
      sb.append(matcher.group(1));
      Object v = msgCtxt.getVariable(matcher.group(2));
      if (v != null) {
        sb.append((String) v);
      }
      sb.append(matcher.group(3));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  protected static String getStackTraceAsString(Throwable t) {
    StringWriter sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }

  protected void setExceptionVariables(Exception exc1, MessageContext msgCtxt) {
    String error = exc1.toString().replaceAll("\n", " ");
    msgCtxt.setVariable(varName("exception"), error);
    Matcher matcher = commonErrorPattern.matcher(error);
    if (matcher.matches()) {
      msgCtxt.setVariable(varName("error"), matcher.group(2));
    } else {
      msgCtxt.setVariable(varName("error"), error);
    }
  }
}
