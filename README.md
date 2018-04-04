# Java XMLCipher Callout

This directory contains the Java source code and pom.xml file required to
compile a simple Java callout for Apigee Edge, that performs an XMLCipher encryption.

## Disclaimer

This example is not an official Google product, nor is it part of an official Google product.

## Notes

There are two callout classes,

* com.google.apigee.edgecallouts.xmlcipher.Encrypt - encrypts the xpath
* com.google.apigee.edgecallouts.xmlcipher.Decrypt - decrypts the encrypted element


## Dependencies

Make sure these JARs are available as resources in the  proxy or in the environment or organization.

* xmlsec-2.1.1.jar
* commons-lang3-3.7.jar
* commons-codec-1.6.jar


## Usage

See [the example API proxy included here](./bundle) for the implementation.

### Encryption

```
curl -i https://${ORG}-${ENV}.apigee.net/xmlcipher/encrypt?xpath=/order/payment  -H content-type:application/xml --data-binary @./sample-data/order.xml
```

During Encryption, the callout generates a random AES key, and a random TripleDES key. It sets the 3DES key into a context variable.  You need this 3DES key in order to decrypt. (If you wanted to, you could modify the callout to accept a specific 3DES key, rather than randomly generating one.)

As implemented in THIS example API Proxy, the response will include an HTTP header  containing the HEX-encoded string that represents the key bytes, filled with the value of that context variable. Supposing the input XML looks like this:

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

To Decrypt, you must pass the payload returned by the encryption step, along with the key, in a query param. Like so:

```
curl -i https://${ORG}-${ENV}.apigee.net/xmlcipher/decrypt?key=8f73a15e2f8a4ab354109d83ba9b1957c2bc32ecd6feb920  -H content-type:application/xml --data-binary @./sample-data/order-encrypted.xml
```

Note the `key` query param here is filled with the value returned in the `keybytes` header in the response to the call to /encrypt. The output here is the original XML.



## License

This material is copyright 2018, Google Inc.
and is licensed under the Apache 2.0 license. See the [LICENSE](LICENSE) file.


## Bugs

none?
