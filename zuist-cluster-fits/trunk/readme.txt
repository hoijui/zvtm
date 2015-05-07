

Requirement of the server for run python script "daemon_wcsCoordinates.py":

- python 2.7

- numpy 1.9.2
sudo pip install numpy

- astropy 1.0.2
sudo pip install astropy

- pika 0.9.14
sudo pip install pika

- rabbitmq 3.1.5
https://www.rabbitmq.com/


Download and install for your operative system
After you need add listener port 5672 (default) on server
	sudo service iptables -A INPUT -p tcp -m tcp --dport 5672 -j ACCEPT
Only in case if is necessary (When the server ports is closed)

Configure RabbitMQ

Add a user and permissions

rabbitmqctl add_user testuser testpassword
rabbitmqctl set_user_tags testuser administrator


Make a virtual host and Set Permissions

rabbitmqctl add_vhost Some_Virtual_Host
rabbitmqctl set_permissions -p Some_Virtual_Host guest ".*" ".*" ".*"

rabbitmq contain for default
user: guest
user tags: administrator
password: guest
vhost: /


Create file "/etc/rabbitmq/rabbitmq.config" with the following content

[
    {rabbit, [{tcp_listeners, [5672]}]}
]

Create file "/etc/rabbitmq/rabbitmq-env.conf" with the following content

NODENAME=rabbit@localhost
NODE_IP_ADDRESS=127.0.0.1
CONFIG_FILE=/etc/rabbitmq/rabbitmq.config

When localhost is the hostname and 127.0.0.1 is the server address of rabbitmq
