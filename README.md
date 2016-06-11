# user-service
Login via facebook and google &amp; mysql

This project contains Oauth2 based authentication.

user can be authenticated by 
1. Social Login [Facebook & Google] :> can be extended for other Social login as well.
2. Username Password based authentication.
3. Third party [Token] based authentication. 

Used redis as session manager to provide multiple instances of this servicec[Distributed environment].

For getting user details :
http://localhost:8081/uaa/userDetails?access_token=eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE0OTcxOTg3OTQsInVzZXJfbmFtZSI6IjI1OTQ4MSIsImF1dGhvcml0aWVzIjpbIlJPTEVfQVVUSEVOVElDQVRFRF9VU0VSIl0sImp0aSI6ImMxOTkwYTU4LTI3OGQtNGY3ZS1iZjgyLTk4ZTc4MTkwMjU5OSIsImNsaWVudF9pZCI6ImFjbWUiLCJzY29wZSI6WyJvcGVuaWQiXX0.lzs0QtpL5IqBZ9ioiqrFzYiXTK7t6kiPtqUPKtgpCyzrpvsDFK0qqKTjKuMfiW-mcSpzRJPOlEzXiJIq71-U0Y5PgH4aKt5XIGde9BNRBV6deApsaOleNR-VX8_SvU9XW7WVEVV_uldy05Kx0WR8gjuY1nCX_ysQyumJWwBr76PuG21BtrVUdI1TphGVxDGgzbP616izxDibC5AiDoeVZmAqDl3ILXTDiU7DmSlBlk-fleSHWi609tQoz_VWIfEIij3YVJe1sErwvprQ1iIyF7nyb6kNLmdZnuYSqcUwFQZDF-PYhz91lr9-uWazQZJWGwY9tzfZXFeiW79wC_FpOQ
You have to first login and then provide token which you got after login.

SignUp :
http://localhost:8081/uaa/signup
=====
{
    "emailId": "namo@gmail.com",
   "password":"ddfdf",
    "ssoProvider": "app",
    "firstName": "Narendra",
    "lastName": "Modi",
    "country": "India",
    "gender": "male",
    "phoneNumber": "9999999999",
    "dob":"1991-11-17"
}

MD5 encryption is used to save password in database.

Social Login : 
String userName = "exsocialfacebooklogin~fb or google token"; eg: exsocialfacebooklogin~CAAA for Web : exsocialfacebooklogin~CAA_WEB
String password = "fb or google token";
String appended = userName + ":" + password;
String bytesEncoded = new String(Base64.encodeBase64(appended.getBytes()));
loginServiceSocialGetRequest.setHeader("Authorization",
				 "Basic " + bytesEncoded);
				 
Third Part Login : send this token in header :>> "Login-Token"
For Normal Login : 
        String userName = "namo@gmail.com";
				String password = "secret";
				String appended = userName + ":" + password;
				String bytesEncoded = new String(Base64.encodeBase64(appended.getBytes()));
				loginServiceSocialGetRequest.setHeader("Authorization",
				 "Basic " + bytesEncoded);
				 
				 


Configuration files will be present on spring cloud. I have used Netflix OSS stack.
spring.application.name=user-service
spring.datasource.url = jdbc:mysql://localhost:3306/cms
spring.datasource.driver-class-name = com.mysql.jdbc.Driver
spring.datasource.username = root
spring.datasource.password = root
spring.datasource.testWhileIdle = true
spring.datasource.validationQuery = SELECT 1
spring.datasource.name=DS
spring.jpa.show-sql = true
spring.jpa.hibernate.ddl-auto = update
spring.jpa.hibernate.naming-strategy = org.hibernate.cfg.ImprovedNamingStrategy
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.MySQL5Dialect
server.contextPath=/uaa
facebook.appID=
facebook.appSecret=
google.appID=
google.appSecret=
google.redirect.uri=
google.redirect.uri.web=
url.getUserInformation.headername=Login-Token
url.static.code=false
spring.groovy.template.check-template-location=false
url.social.token.name=X-SOCIAL-TOKEN
url.social.token.type=X-SOCIAL-TYPE
thread.pool.core.size=5
thread.pool.max.size=10
facebook.image.size=LARGE
redis.sentinel.uri=ip1~26379,ip2~26379,ip3~26379
redis.env=statging
redis.host-name=localhost
redis.port=6379
