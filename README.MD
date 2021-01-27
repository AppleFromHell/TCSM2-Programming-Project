#Collecto Client Server
##Introduction
This is project contains both the client and the server for the collecto game client.
The game can be played over a network.

For this project Java version 11 is used.

##Execution
To start game execute the client.jar and the server.jar file
 
Assuming you have Java installed, these files can be executed without parameters by using intellij or eclipse to run the Client.java and Server.java class

They can also be executed with parameters.

These parameters can either be:
- (server only) port
- (client only) IP + port (seperated by a space)
- (client only) gui
- debug

Examples are:

Client arguments:`192.0.0.155 8855` to start a client which connects to a server at '192.0.0.155:8855'

Server arguments:`8855 debug` to start a server with debug mode at '8855'

If no parameters are given the client and the server will prompt the user for an ip and or a port

If IP + port are given, this step is skipped.

If gui is added, the view will be started as a GUI

If debug is added, extra information will be displayed in the console like the in- and outgoing messages as well as some of the states of the client

##Once the client is started
If you hadn't specified a port and an IP the client will prompt you for those.
Next you'll be asked to input a username. If this is a valid username you get into the main menu

In the main menu you can:
- enter the queue `q`
- request a list of logged in users `l`
- chat `c`
- whisper `whisper`
- select a player type `player`
- exit `x`
- ask for help `h`

###Playing as an AI
To let an AI play for you, simply select an ai after using the `player` command

###Starting a game
To start a game, simply enter the queue by typing for example `q`
Once another client has entered the queue your game will start

If no AI player was selectd you can make a move like for example `m 3` to move row '3'

If an AI player was selected simply press `m` to let them put a move

##Once the server is started
There is not much to do. You can always exit by using `x`