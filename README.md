
# JAX-RS client filter for Oracle Cloud Infrastructure API request sigining

Oracle Cloud Infrastructure の[リクエストsigning][request-signing]に対応するための JAX-RS標準の ClientResponseFilterです。通常のJAX-RS Clientと同様のアプリケーションを書けます。このFilterが送信時に自動的に必要な署名ヘッダを付加します。  
Oracleが提供しているJavaサンプルコードを元に作成しました。  

使用方法はソース `com.oracle.jp.se.mw.jaxrs.client.JaxRsClientFilterTest.java` を参考にして下さい。

OCI API Signature付のリクエストを送るコードサンプル

```java
String url = "https://oci.api.url";
String proxy = "http://proxy.com:80";

// Use JaxRsClientHelper when you'd like to set proxy  
// Client client = JaxRsClientHelper.getClient(proxy);
Client ClientBuilder.newClient();
// Register OciJaxRsClientFilter to add Authorization header required for OCI 
client.register(OciJaxRsClientFilter.class); //"read [DEFAULT] of ~/.oci/config"
WebTarget target = client.target(url);
Builder builder = target.request();
Response response = builder.post(
    Entity.entity(new Message("こんにちは！"), MediaType.APPLICATION_JSON_TYPE)
  );
```

JaxRsClientHelper には自己署名証明書を許す緩いclientを生成する機能も備えています。
  
---
*Copyright © 2019, Oracle and/or its affiliates. All rights reserved.*

[request-signing]: https://docs.cloud.oracle.com/iaas/Content/API/Concepts/signingrequests.htm#Java
