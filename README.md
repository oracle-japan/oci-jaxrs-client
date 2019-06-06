
# JAX-RS client (ClientRequestFilter) for Oracle Cloud Infrastructure API request signatures

Oracle Cloud Infrastructure の [Request Signatures][request-signatures] （[日本語翻訳][request-signatures-ja]）に対応するための JAX-RS標準に準拠した ClientResponseFilterです。このFilterが送信時に自動的に必要な署名ヘッダを付加しますので、通常のJAX-RS Clientと同様のアプリケーションの書き方でAPIのリクエストができます。  
Oracleが提供しているJavaサンプルコードを元に作成しました。  

## 事前準備

プログラムの動作にあたって、[Oracle Cloue Infrastructure Command Line Interface][CLI]（[日本語翻訳][CLI-ja]） (CLI)がインストールされているか、もしくはCLIの動作に必要な設定がなされている必要があります。すなわち、${user.home}/.oci/config ファイルに以下の設定が正しくなされていることを確認して下さい。

```config
[DEFAULT]
user=ocid1.user.oc1..aaaaaaaa3nh4vgi4c4u5f3yofmwow........
fingerprint=69:e5:7b:aa:72:14:........
key_file=/home/opc/.oci/oci_api_key.pem
tenancy=ocid1.tenancy.oc1..aaaaaaaa4mb7wrcy3ls4u3jsy1........
region=ap-tokyo-1
```

てっとり早いのは、ドキュメンテーションに沿ってCLIをインストールすることです。これでCLIが正常動作していればOKです。  
このプログラムはCLIモジュールに依存していませんが、CLIモジュールが利用する設定情報を利用します。CLIをインストールしなくてもconfigファイルと秘密鍵ファイルをコピーすれば動作します。

## 使用方法

使用方法はソース `src/test/java` 配下にある接続テスト用クラスを参考にして下さい。

※ OCI API Signature付のリクエストを送る例

```java
Client client = ClientBuilder.newClient();
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

## 注意点

この実装はJerseyのJAX-RS Clientを前提に作られています。  
フィルターの実装クラス(com.oracle.jp.se.mw.jaxrs.client.OciJaxRsClientFilter)自体は、特定のJAX-RS Clientの実装には依存しないようにしているので、他のJAX-RS Clientにも適用ができると思います。Jerseyを使用した場合に発生する不具合（デフォルトのままだとリクエスト・ヘッダのTransfer-Encoding: chunked になってしまい、Content-Length ヘッダ が消えてしまう）を回避するために、OciJaxRsClient クラス内でJerseyに依存するconfig設定を行っています。  
余談ですが、これの根本原因は、ベースとなっている[標準（ドラフト）][draft-cavage-http-signatures-08]が、Signatureの生成・検証にContent-Lengthヘッダの存在を前提にしているためです。Transfer-Encoding: chunked でもchunk sizeを数えれば実質同じことができると思うので、HTTP/1.1との相性も考慮して標準の方が歩み寄ってもいいのでは?

## その他

このプログラムは、[OCHaCafe#6 - 人にもマシンにもやさしいAPIのエコシステム][ochacafe] で、ClientRequestFilterの例として作成＆紹介しました。自由に使用していただいて構いませんが、いかなる動作保証もしませんし、使用の結果について一切の責任も負いません。動作に問題がある場合Issuesにインプットしていただければ、鋭意対応する心意気はあります。

<br/> 

---
*Copyright © 2019, Oracle and/or its affiliates. All rights reserved.*

[request-signatures]: https://docs.cloud.oracle.com/iaas/Content/API/Concepts/signingrequests.htm
[request-signatures-ja]:https://docs.oracle.com/cd/E97706_01/Content/API/Concepts/signingrequests.htm
[CLI]: https://docs.cloud.oracle.com/iaas/Content/API/Concepts/cliconcepts.htm
[CLI-ja]: https://docs.oracle.com/cd/E97706_01/Content/API/Concepts/cliconcepts.htm
[draft-cavage-http-signatures-08]: https://tools.ietf.org/html/draft-cavage-http-signatures-08
[ochacafe]: https://ochacafe.connpass.com/event/127773/
