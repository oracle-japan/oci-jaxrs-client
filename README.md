
# JAX-RS client filter for Oracle Cloud Infrastructure API request sigining

Oracle Cloud Infrastructure の [リクエストsigning][request-signing] に対応するための JAX-RS標準に準拠した ClientResponseFilterです。このFilterが送信時に自動的に必要な署名ヘッダを付加しますので、通常のJAX-RS Clientと同様のアプリケーションの書き方でAPIのリクエストができます。  
Oracleが提供しているJavaサンプルコードを元に作成しました。  

使用方法はソース `src/test/java` 配下にあるテストクラスを参考にして下さい。

※ OCI API Signature付のリクエストを送る例

```java
Client ClientBuilder.newClient();
client.register(OciJaxRsClientFilter.class); //"read [DEFAULT] of ~/.oci/config"
WebTarget target = client.target("https://foo.bar.com/api/xxx");
Builder builder = target.request();
Response response = builder.post(
    Entity.entity(new HelloMessage("こんにちは！"), MediaType.APPLICATION_JSON_TYPE)
  );
```

通常は以下の簡単な方法（付加機能があります）でClientを取得するのが便利です。

```java
// OciJaxRsClientFilter 内蔵
Client client1 = OciJaxRsClient.getClient();
// OciJaxRsClientFilter 内蔵 + SSL自己署名証明書他緩い接続を許す
Client client2 = OciJaxRsClient.getLooseClient();
// OciJaxRsClientFilter 内蔵 + proxy指定
Client client3 = OciJaxRsClient.getClient("http://proxy.xxx.xxx:80");
// OciJaxRsClientFilter 内蔵 + proxy指定 + SSL自己署名証明書他緩い接続を許す
Client client4 = OciJaxRsClient.getLooseClient("http://proxy.xxx.xxx:80");

// OciJaxRsClientFilter 無し
Client client5 = JaxRsClient.getClient();
// OciJaxRsClientFilter 無し + SSL自己署名証明書他緩い接続を許す
Client client6 = JaxRsClient.getLooseClient();
// OciJaxRsClientFilter 無し + proxy指定
Client client7 = JaxRsClient.getClient("http://proxy.xxx.xxx:80");
// OciJaxRsClientFilter 無し + proxy指定 + SSL自己署名証明書他緩い接続を許す
Client client8 = JaxRsClient.getLooseClient("http://proxy.xxx.xxx:80");
```

---
*Copyright © 2019, Oracle and/or its affiliates. All rights reserved.*

[request-signing]: https://docs.cloud.oracle.com/iaas/Content/API/Concepts/signingrequests.htm#Java
