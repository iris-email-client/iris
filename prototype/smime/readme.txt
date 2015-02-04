# create a new key
keytool -genkey -alias iris -keystore iris_keystore.pfx -storepass 123456 -validity 365 -keyalg RSA -keysize 1024 -storetype PKCS12 -dname "CN=iris, OU=CIC, O=Universidade de Brasilia, L=Brasilia, ST=DF, C=BR"

# list alias
keytool -list -keystore iris_keystore.pfx -storetype PKCS12 -alias iris

# copy default java cacerts into a new PKCS12 keystore
keytool -importkeystore -srckeystore /opt/java/jre/lib/security/cacerts -destkeystore iris_keystore.pfx -srcstoretype JKS -deststoretype PKCS12 -srcstorepass changeit -deststorepass 123456

# list all entries
keytool -list -keystore iris_keystore.pfx -storetype PKCS12
