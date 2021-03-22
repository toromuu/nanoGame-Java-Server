package es.um.redes.nanoGames.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;import es.um.redes.nanoGames.broker.BrokerClient;
import es.um.redes.nanoGames.message.NGBasicMessage;
import es.um.redes.nanoGames.message.NGMatrixMessage;
import es.um.redes.nanoGames.message.NGMessage;
import es.um.redes.nanoGames.message.NGParamMessage;
import es.um.redes.nanoGames.message.NGPlayersMessage;
import es.um.redes.nanoGames.message.NGRoomsMessage;
import es.um.redes.nanoGames.message.NGShortMessage;
import es.um.redes.nanoGames.message.NGStringMessage;
import es.um.redes.nanoGames.server.roomManager.NGChallenge;
import es.um.redes.nanoGames.server.roomManager.NGRoomManager;
import es.um.redes.nanoGames.server.roomManager.NGRoomStatus;

/**
 * A new thread runs for each connected client
 */
public class NGServerThread extends Thread {
	
	//Possible states of the connected client
	/*private static final byte PRE_TOKEN = 1;
	private static final byte PRE_REGISTRATION = 2;
	private static final byte OFF_ROOM = 3;
	private static final byte IN_ROOM = 4;*/
	
	//Time difference between the token provided by the client and the one obtained from the broker directly
	private static final long TOKEN_THRESHOLD = 1500; //15 seconds
	//Socket to exchange messages with the client
	private Socket socket = null;
	//Global and shared manager between the threads
	private NGServerManager serverManager = null;
	//Input and Output Streams
	private DataInputStream dis;
	private DataOutputStream dos;
	//Utility class to communicate with the Broker
	BrokerClient brokerClient;
	//Current player
	NGPlayerInfo player;
	//Current RoomManager (it depends on the room the user enters)
	NGRoomManager roomManager;
	//TODO Add additional fields

	public NGServerThread(NGServerManager manager, Socket socket, String brokerHostname) throws IOException {
		serverManager = manager;
		this.socket = socket;
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		brokerClient = new BrokerClient(brokerHostname);

	}

	//Main loop
	public void run() {
		try {
			//We obtain the streams from the socket
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			//The first step is to receive and to verify the token
			receiveAndVerifyToken();
			//The second step is to receive and to verify the nick name
			receiveAndVerifyNickname();
			//While the connection is alive...
			while (true) {
				// Procesar mensajes.
				NGMessage mensaje = 
						 NGMessage.readMessageFromSocket(dis);
				byte opcode = mensaje.getOpcode();
				switch (opcode) {
				case NGMessage.OP_GET_ROOM_LIST:
					sendRoomList();
					break;
				case NGMessage.OP_QUIT:
					serverManager.removePlayer(player);
					break;
				case NGMessage.OP_ENTER_ROOM:
					NGShortMessage mensajeSala = (NGShortMessage) mensaje;
					roomManager = serverManager.enterRoom(player, mensajeSala.getShort());
					
					
					if (roomManager != null) {
						NGBasicMessage respuesta = new NGBasicMessage(NGMessage.OP_ACCESS_OK);
						byte [] mensajeBytes = respuesta.toByteArray();
						dos.write(mensajeBytes);
						processRoomMessages();
					}
					else {
						NGBasicMessage respuesta = new NGBasicMessage(NGMessage.OP_ACCESS_ERROR);
						byte [] mensajeBytes = respuesta.toByteArray();
						dos.write(mensajeBytes);
					}
				default:
					break;
				}
					
				
				//TODO Rest of messages according to the automata
			}
		} catch (Exception e) {
			//If an error occurs with the communications the user is removed from all the managers and the connection is closed
			//TODO
		}
		//TODO Close the socket
		try {
			socket.close();
		} catch (IOException e) {
			// La excepción no debería saltar. Si el socket no existe, eso no se ejecuta
		}
	}

	private void receiveAndVerifyToken() throws IOException {
		
		boolean tokenVerified = false;
		while (!tokenVerified) {
			NGParamMessage mensaje = (NGParamMessage)
					 NGMessage.readMessageFromSocket(dis);
				//We extract the token from the message
				long tokenCliente = mensaje.getParam();
				//now we obtain a new token from the broker

				long tokenBroker = brokerClient.getToken();
				//We check the token and send an answer to the client
				if (tokenBroker - tokenCliente < TOKEN_THRESHOLD){
					NGBasicMessage aceptar = (NGBasicMessage)
							 NGMessage.makeBasicMessage(NGMessage.OP_TOKEN_OK);
					byte[] aceptarBytes = aceptar.toByteArray();
					dos.write(aceptarBytes);
					tokenVerified = true;
				}
				else {
					NGBasicMessage rechazar = (NGBasicMessage)
							 NGMessage.makeBasicMessage(NGMessage.OP_TOKEN_ERROR);
					byte[] rechazarBytes = rechazar.toByteArray();
					dos.write(rechazarBytes);
				}
				
		}
	}

	//We obtain the nick and we request the server manager to verify if it is duplicated
	//TODO
	private void receiveAndVerifyNickname() throws IOException {
		boolean nickVerified = false;
		//this loop runs until the nick provided is not duplicated
		while (!nickVerified){
			NGMessage mensaje = NGMessage.readMessageFromSocket(dis);
			NGStringMessage mensajeNick = (NGStringMessage) mensaje;
				//We extract the nick from the message
				String nick = mensajeNick.getString();
				player = new NGPlayerInfo(nick);
				//we try to add the player in the server manager
				
				//if success we send to the client the NICK_OK message
				if (serverManager.addPlayer(player)) {
					NGBasicMessage aceptar = (NGBasicMessage)
							 NGMessage.makeBasicMessage(NGMessage.OP_NICK_OK);
					byte[] aceptarBytes = aceptar.toByteArray();
					dos.write(aceptarBytes);
					nickVerified = true;
				}
				//otherwise we send DUPLICATED_NICK
				else {
					NGBasicMessage rechazar = (NGBasicMessage)
							 NGMessage.makeBasicMessage(NGMessage.OP_DUPLICATED_NICK);
					byte[] rechazarBytes = rechazar.toByteArray();
					dos.write(rechazarBytes);
				}	
				
		} 
										
		
	}

	
	//We send to the client the room list
	//TODO
	private void sendRoomList() throws IOException {
		NGRoomsMessage mensaje = new NGRoomsMessage(NGMessage.OP_ROOM_LIST, serverManager.getRoomList());
			//We extract the nick from the message
		byte [] mensajeBytes = mensaje.toByteArray();
		dos.write(mensajeBytes);
	}
	
	


	
	//Method to process messages received when the player is in the room
	//TODO
	private void processRoomMessages() throws IOException {
		// First we send the rules
		NGStringMessage reglas = (NGStringMessage) 
				NGMessage.makeStringMessage(NGMessage.OP_SEND_RULES, roomManager.getRules());
		dos.write(reglas.toByteArray());
		
		NGPlayersMessage status = (NGPlayersMessage) 
				NGMessage.makePlayersMessage(NGMessage.OP_SEND_PLAYERS, roomManager.checkStatus(player).getPlayers());
		dos.write(status.toByteArray());
		// and the initial status
		//Now we check for incoming messages, status updates and new challenges
		boolean exit = false;
		short retoAnterior=(short) (roomManager.getChallengeNumber()-1);
		short estadoAnterior=roomManager.getStatusNumber();
		
		
		while (!exit) {
			// Si hay un nuevo status, se lo mandamos
			NGRoomStatus estado = roomManager.checkStatus(player);
			if (estado != null && estado.getStatusNumber() > estadoAnterior) {
				estadoAnterior = estado.getStatusNumber();
				NGPlayersMessage mensajePuntuaciones = (NGPlayersMessage) 
						NGMessage.makePlayersMessage(NGMessage.OP_SEND_PLAYERS, estado.getPlayers());
				dos.write(mensajePuntuaciones.toByteArray());
			}
			
			
			// So hay reto y es nuevo, se lo enviamos al player
			
			
			NGChallenge reto = roomManager.checkChallenge(player);
			if (reto != null && reto.getChallengeNumber() > retoAnterior) {
				retoAnterior = reto.getChallengeNumber();
		
				exit=processNewChallenge(reto);
				
				/*NGMatrixMessage mensajeMatriz = (NGMatrixMessage)
					NGMessage.makeMatrixMessage(NGMessage.OP_MATRIX, reto.getChallenge());
				dos.write(mensajeMatriz.toByteArray());*/
			}
			
		
			if (dis.available()!=0) {
				// Procesar mensajes del juego.
				NGMessage mensaje = 
						 NGMessage.readMessageFromSocket(dis);
				byte opcode = mensaje.getOpcode();
				switch (opcode) {
				case NGMessage.OP_EXIT_ROOM : {
					NGShortMessage mensajeSalir = (NGShortMessage) mensaje;
					serverManager.leaveRoom(player,mensajeSalir.getShort());
					exit = true;
					break;
				}
				
				/*
				case NGMessage.OP_ANSWER : {
					// Puede ser que le llegue una respuesta en Timeout.
				}
				*/
				
				
				case NGMessage.OP_GET_RULES : {		
					NGStringMessage mensajeReglas = (NGStringMessage) 
							NGMessage.makeStringMessage(NGMessage.OP_SEND_RULES, roomManager.getRules());
					dos.write(mensajeReglas.toByteArray());
					break;
				}
				
				case NGMessage.OP_GET_STATUS : {
					NGPlayersMessage mensajeStatus = (NGPlayersMessage) 
							NGMessage.makePlayersMessage(NGMessage.OP_SEND_PLAYERS, roomManager.checkStatus(player).getPlayers());
					dos.write(mensajeStatus.toByteArray());
					break;
				}
				default:
					break;
				}
				//TODO
			}
		}
	}

	private AtomicBoolean timeout_triggered = new AtomicBoolean();
	//Private class to implement a very simple timer
		private class Timeout extends TimerTask{
			@Override
			public void run(){
				timeout_triggered.set(true);
			}
		}
		
		
		private boolean processNewChallenge(NGChallenge challenge) throws IOException {
		//We send the challenge to the client
		
			NGMatrixMessage mensajeMatriz = (NGMatrixMessage)
			NGMessage.makeMatrixMessage(NGMessage.OP_MATRIX, challenge.getChallenge());
			dos.write(mensajeMatriz.toByteArray());
		
			//Now we set the timeout
			Timer timer = null;
			timeout_triggered.set(false);
			timer = new Timer();
			timer.schedule( new Timeout(), roomManager.getTimeout(), roomManager.getTimeout());
			boolean answerProvided = false;
			//Loop until an answer is provided or the timeout expires
			while (!timeout_triggered.get() && !answerProvided) {
				if (dis.available() > 0) {
					//The client sent a message
					
					NGMessage mensaje = NGMessage.readMessageFromSocket(dis);
					byte opcode = mensaje.getOpcode();
					switch (opcode) {
					case NGMessage.OP_EXIT_ROOM : {
						NGShortMessage mensajeShort = (NGShortMessage) mensaje;		
						serverManager.leaveRoom(player, mensajeShort.getShort());
						timer.cancel();
						return true;
					}
					case NGMessage.OP_GET_RULES : {		
						NGStringMessage mensajeReglas = (NGStringMessage) 
								NGMessage.makeStringMessage(NGMessage.OP_SEND_RULES, roomManager.getRules());
						dos.write(mensajeReglas.toByteArray());
						break;
					}
					
					case NGMessage.OP_GET_STATUS : {
						
						NGPlayersMessage mensajeStatus = (NGPlayersMessage) 
								NGMessage.makePlayersMessage(NGMessage.OP_SEND_PLAYERS, roomManager.checkStatus(player).getPlayers());
						dos.write(mensajeStatus.toByteArray());
						break;
					}
					
					case NGMessage.OP_ANSWER : {
						answerProvided = true;
						NGStringMessage mensajeAnswer = (NGStringMessage) mensaje;
						roomManager.answer(player, mensajeAnswer.getString());
						timer.cancel();
						break;
					}
					
					default:
						break;
					}		
					
				} 
				else 
					try {
					//To avoid a CPU-consuming busy wait
					Thread.sleep(50);
					} catch (InterruptedException e) {
					//Ignore
					}
			}	
			
			if (!answerProvided) {
				//The timeout expired
				timer.cancel();
				roomManager.noAnswer(player);
				NGBasicMessage timeout = (NGBasicMessage)
						 NGMessage.makeBasicMessage(NGMessage.OP_TIMEOUT);
				dos.write(timeout.toByteArray());
			}
			return false;
		}
	
}
