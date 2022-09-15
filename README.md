# Java XMLCipher Callout

This directory contains the Java source code and pom.xml file required
to compile a simple Java callout for Apigee, that performs an
XML encryption or decryption, according to [https://www.w3.org/TR/xmlenc-core1/](https://www.w3.org/TR/xmlenc-core1/), via [Apache XML Security for Java](http://santuario.apache.org/).
This callout encrypts or decrypts one element
within an XML document, and returns the resulting document.

## Disclaimer

This example is not an official Google product, nor is it part of an official Google product.

## License

This material is copyright 2018-2021, Google LLC.
and is licensed under the Apache 2.0 license. See the [LICENSE](LICENSE) file.

This code is open source but you don't need to compile it in order to use it.

## Details

There are two callout classes,

* com.google.apigee.callouts.xmlcipher.Encrypt - encrypts the element specified by an xpath
* com.google.apigee.callouts.xmlcipher.Decrypt - decrypts the encrypted element


## Usage

See [the example API proxy included here](./bundle) for the implementation.

### Encryption

During Encryption, the callout:

* generates a random 128-bit AES key
* Uses that key with Cipher-Block Chaining to encrypt the chosen element. The [EncryptedData element](https://www.w3.org/TR/2002/REC-xmlenc-core-20021210/Overview.html#sec-Usage) uses Algorithm= http://www.w3.org/2001/04/xmlenc#aes128-cbc ].
* generates a random TripleDES (3DES) key, or de-serializes the 3DES key you provide. This is called the "key encrypting key".
* uses that key-encrypting-key to encrypt the AES key. The EncryptedKey element uses Algorithm= http://www.w3.org/2001/04/xmlenc#kw-tripledes .
* embeds that enciphered key into the ciphertext. (This is all just standard [Apache XML Security for Java](http://santuario.apache.org/).)
* if it was generated, sets the 3DES key into a context variable.

You need this 3DES key in order to decrypt.

This callout does not support RSA-based key transport, at this time. 

Use these properties to configure the callouts:

| property name  | meaning                                          |
| -------------- | ------------------------------------------------ |
| `kek`          | key-encrypting-key. An encoded version of the key.  See also `kek-encoding`.  This is required for `Decrypt`, optional for `Encrypt`. In the case of encryption if you do not specify a kek, the callout will randomly generate one for you. |
| `kek-encoding` | The encoding to use to decode the `kek`.  One of: Base16, Bae64, Base64url.  Default: Base16 |
| `source`       | The content to encrypt or decrypt.  Usually `message.content`. |
| `xpath`        | The xpath of the element to encrypt.  This is used only with the `Encrypt` callout. |


### Example

This repo includes an example API Proxy, which demonstrates hwo to encrypt and decrypt.

The encrypt flow in the proxy sends an HTTP header in the response
that contains the HEX-encoded string that represents the key-encrypting-key
bytes.

To invoke the proxy, use something like this:

```
curl -i $endpoint/xmlcipher/encrypt1?xpath=/order/payment  -H content-type:application/xml --data-binary @./sample-data/order.xml
```

... where `$endpoint` holds the endpoint for the org + environment where you have imported and deployed the proxy. One of these:
```
# Apigee Edge
endpoint=https://${ORG}-${ENV}.apigee.net

# Apigee X
endpoint=https://my-custom-hostname-for-apigee.net

```

Supposing the input XML looks like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<order>
  <customer customerNumber="0815A4711">
    <name>Michael Sonntag</name>
    <address>
      <street>Altenbergerstr. 69</street>
      <ZIP>4040</ZIP>
      <city>Linz</city>
    </address>
  </customer>
  <articles>
    <line>
      <quantity unit="piece">30</quantity>
      <product productNumber="9907">XML editing widget</product>
      <price currency="EUR">0.10</price>
    </line>
    <line>
      <quantity unit="litre">5</quantity>
      <product productNumber="007">Super juice</product>
      <price currency="HUF">500</price>
    </line>
  </articles>
  <delivery>
    <deliveryaddress>
      <name>Michael Sonntag</name>
      <address>
        <street>Auf der Wies 18</street>
      </address>
    </deliveryaddress>
  </delivery>
  <payment type="CC">
    <creditcard issuer="Mastercard">
      <nameOnCard>Mag. Dipl.-Ing. Dr. Michael Sonntag</nameOnCard>
      <number>5201 2345 6789 0123</number>
      <expiryDate>2006-04-30</expiryDate>
    </creditcard>
  </payment>
</order>
```

...when encrypting `/order/payment`, the encrypted output looks like this:

```
HTTP/1.1 200 OK
Date: Tue, 03 Apr 2018 23:07:03 GMT
Content-Length: 2399
Connection: keep-alive
keybytes: 8f73a15e2f8a4ab354109d83ba9b1957c2bc32ecd6feb920

<order>
  <customer customerNumber="0815A4711">
    <name>Michael Sonntag</name>
    <address>
      <street>Altenbergerstr. 69</street>
      <ZIP>4040</ZIP>
      <city>Linz</city>
    </address>
  </customer>
  <articles>
    <line>
      <quantity unit="piece">30</quantity>
      <product productNumber="9907">XML editing widget</product>
      <price currency="EUR">0.10</price>
    </line>
    <line>
      <quantity unit="litre">5</quantity>
      <product productNumber="007">Super juice</product>
      <price currency="HUF">500</price>
    </line>
  </articles>
  <delivery>
    <deliveryaddress>
      <name>Michael Sonntag</name>
      <address>
        <street>Auf der Wies 18</street>
      </address>
    </deliveryaddress>
  </delivery>
  <xenc:EncryptedData xmlns:xenc="http://www.w3.org/2001/04/xmlenc#" Type="http://www.w3.org/2001/04/xmlenc#Element"><xenc:EncryptionMethod Algorithm="http://www.w3.org/2001/04/xmlenc#aes128-cbc"/><ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
<xenc:EncryptedKey><xenc:EncryptionMethod Algorithm="http://www.w3.org/2001/04/xmlenc#kw-tripledes"/><xenc:CipherData><xenc:CipherValue>ABuAz4R5NL1Lj0gge4wDxpm9OM/RHUGIt7afT6K/3v0=</xenc:CipherValue></xenc:CipherData></xenc:EncryptedKey></ds:KeyInfo><xenc:CipherData><xenc:CipherValue>D0rt2+gZuUhs/TUJ0vWbDK1+H1YESQztMm+KRA4cCivGv/iRhgLmbznYcBdUuVbaPHLfAXhVL892&#xD;
7QphINvrL7TcpzEuuFxrXY3K7xbNLquFBEpxOFs0Ize6NUaQ7yYmOUxQIdMTPNfcmieERXfv38d0&#xD;
2+iZm/26HRFrySZwgUeQvSfWPU9tZpHOua0UtlgfdWbfFh106oO7QKol+iBdc73COaEkj8V9vQwK&#xD;
cV7BoRyhBRzbqtYhehQfvO/bTgQtyV+jh8US7WYTjJe+jQuWhbSuqv2STTObBr312HeHEzixPS2O&#xD;
F0Ds6idWbCj7KL4r1p1gMnjnp8ZxBfkKbMRcHg==</xenc:CipherValue></xenc:CipherData></xenc:EncryptedData>
</order>
```


Notice the keybytes  HTTP header in the response.



To Decrypt, you must pass the payload returned by the encryption step, along with the key, in a query param. Like so:

```
curl -i $endpoint/xmlcipher/decrypt?key=8f73a15e2f8a4ab354109d83ba9b1957c2bc32ecd6feb920  -H content-type:application/xml --data-binary @./sample-data/order-encrypted.xml
```

Note the `key` query param here is filled with the value returned in the `keybytes` header in the response to the call to /encrypt. The output from this command will be the original XML.


## Bugs

* The callout does not support [RSA Key Transports](https://www.w3.org/TR/2002/REC-xmlenc-core-20021210/Overview.html#sec-Alg-KeyTransport).
