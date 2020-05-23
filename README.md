# remote_sensors
Envia dados dos sensores para um servidor web.

# Configuração
Editar IP do servidor via arquivo:
    RemoteSensor\app\src\main\res\xml\network_security_config.xml
    * chave: `domain`

# Web Dashboard
Acesse https://remotesensorjeffraf.azurewebsites.net


Para usar um servidor local edite `MainActivity.SERVER_URL` e em `app/res/xml/network_security_config.xml` altere o IP em `domain-config/domain`
