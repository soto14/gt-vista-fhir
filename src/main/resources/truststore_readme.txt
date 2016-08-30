About
-------
The truststore contains the key needed to establish an SSL connection to the VistA Ex API server.

Password
---------
gtvistaex

Command to create truststore
----------------------------
Assumes java keytool is on the classpath
>keytool -import -file vistaExCert.cer -alias vistaExCert -keystore gtVistaExTrustStore