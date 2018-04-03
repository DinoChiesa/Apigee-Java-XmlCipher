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

During Encryption, the callout generates a random AES key, and a random TripleDES key. It sets the 3DES key into a context variable.  You need this 3DES key in order to decrypt.

If you wanted to, you could modify the callout to accept a specific 3DES key, rather than randomly generating one.

The response will include an HTTP header containing the HEX-encoded string that represents the key bytes.

To Decrypt, you must pass the payload returned by the encryption step, along with the key, in a query param. Like so:

```
curl -i https://${ORG}-${ENV}.apigee.net/xmlcipher/decrypt?key=8f73a15e2f8a4ab354109d83ba9b1957c2bc32ecd6feb920  -H content-type:application/xml --data-binary @./sample-data/order-encrypted.xml
```



## License

This material is copyright 2018, Google Inc.
and is licensed under the Apache 2.0 license. See the [LICENSE](LICENSE) file.


## Bugs

none?
