### resolved with current nightly build 19.01.2016 (top commit) ###

Little test project that includes a netty WebSocket Client and a Akka Streams 2.0.2 WebSocket server.

In contrast to 2.0.1 connections break from time to time because the server sends a RST

see the /results folder for a good looking but broken WebSocket exchange and a tcp dump including two different
observed errors as well as one successfull case


There is a server and a client app. The client writes some data to the server, the server echos tha data back and the client
prints it to the cmd line. However sometimes a RESET happens and the data is not returned to the client or not all data is returned.
