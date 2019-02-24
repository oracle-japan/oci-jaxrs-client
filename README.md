
JAX-RS Client をベースにしたREST Clientです。  
Oracle Cloud Infrastructure の[リクエストsigning][request-signing]に対応するために、JAX-RS標準の ClientResponseFilterを、Oracleが提供しているJavaサンプルコードを元に作成しました。  

使用方法は、  
com.oracle.jp.se.mw.jaxrs.client.JaxRsClientFilterTest.java  
を参照して下さい。

OCI Signature付のリクエストを送るコードサンプル
```java
String url = "https://oci.api.url";
String proxy = "http://proxy.com:80";

Client client = JaxRsClientHelper.getClient(proxy);
client.register(OciJaxRsClientFilter.class); //"read [DEFAULT] of ~/.oci/config"
WebTarget target = client.target(url);
Builder builder = target.request();
Response response = builder.post(Entity.entity(new Message("こんにちは！"), MediaType.APPLICATION_JSON_TYPE));
```

JaxRsClientHelper には自己署名証明書を突破する機能も備えています。
  
---
*Copyright © 2019, Oracle and/or its affiliates. All rights reserved.*


[request-signing]: https://docs.cloud.oracle.com/iaas/Content/API/Concepts/signingrequests.htm#Java
