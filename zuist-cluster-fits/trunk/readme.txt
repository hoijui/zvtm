

Requirement of the server for run python script "src/main/resources/scripts/wcs/daemon_wcsCoordinates.py":

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


I you need remove the queues and exchanges

rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl start_app


In the file "src/main/resources/scripts/wcs/daemon_wcsCoordinates.py" containe the configuration about the server rabbitMQ. Change this data with the values of your server.
When init the app (for example, "./single_master.sh -hostmq 127.0.0.1 -fits ~/zuist_scenes/astro/vvv/scene.xml"), set the parameter -hostmq with ip address of server.


For create the tiles of a fits images, use the script "src/main/resource/scripts/tile/imageTiler.sh" with the parameters:

python imageTiler.py <src_image_path> <target_dir> [options]

when options is for fits images case:

	-fits					processing fits image with astropy library
	-ts=N					tile size (N in pixels)
	-f						force tile generation
	-idprefix=p				custom prefix for all region and objects IDs
	-dx=x					x offset for all regions and objects
	-dy=y					y offset for all regions and objects
	-dl=l					level offset for all regions and objects
	-scale=s				s scale factor w.r.t default size for PDF input
	-layer=name				name layer from the zuist
	-minvalue				value minimum on fits images (this is necessary for global scale method. You can calculated with script "minmaxfit.py")
	-maxvalue				value maximum on fits images (this is necessary for global scale method. You can calculated with script "minmaxfit.py")
	-onlyxml 				create only xml from the tiles
	-shrink 				default use natural neighbor. change set to shrink
	-notnewfile 			if exist file, not create new file
	-withbackground			Without the image of the level 0
	-maxneighborhood=N		Maximum neighborhood. The best solution tested with N = 2

Example:
./imageTiler.py ~/coadd_gal_AIT.fits ./coadd_AIT_neighbor -f -astropy -ts=500 -dx=-100000 -dy=-100000 -tileprefix=tile- -idprefix=coadd_AIT -layer=SceneKsSpace -minvalue=-10385.527 -maxvalue=50703.176 -maxneighborhood=2
