package es.um.redes.nanoGames.client.application;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import es.um.redes.nanoGames.broker.BrokerClient;
import es.um.redes.nanoGames.client.comm.NGGameClient;
import es.um.redes.nanoGames.client.shell.NGCommands;
import es.um.redes.nanoGames.client.shell.NGShell;
import es.um.redes.nanoGames.message.NGMatrixMessage;
import es.um.redes.nanoGames.message.NGMessage;
import es.um.redes.nanoGames.message.NGPlayersMessage;
import es.um.redes.nanoGames.message.NGStringMessage;
import es.um.redes.nanoGames.server.NGPlayerInfo;
import es.um.redes.nanoGames.server.roomManager.NGRoomDescription;

public class NGController {
	//Number of attempts to get a token
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;
	
	//Different states of the client (according to the automata)
	/*private static final byte PRE_TOKEN = 1;
	private static final byte PRE_REGISTRATION = 2;
	private static final byte OFF_ROOM = 3;
	private static final byte IN_ROOM = 4;*/
	
	//TODO Add additional states if necessary
	//The client for the broker
	private BrokerClient brokerClient;
	//The client for the game server
	private NGGameClient ngClient;
	//The shell for user commands from the standard input
	private NGShell shell;
	//Last command provided by the user
	private byte currentCommand;
	//Nickname of the user
	private String nickname;
	//Current room of the user (if any)
	private String room;
	//Current answer of the user (if any)
	private String answer;
	//Rules of the game
	//private String rules = "";
	//Current status of the game
	//private String gameStatus = "";
	//Token obtained from the broker
	private long token = 0;
	//Server hosting the games
	private String serverHostname;
	private boolean hayChallenge = false;
	private boolean nickIntroducido = false;

	public NGController(String brokerHostname, String serverHostname) {
		try {
			brokerClient = new BrokerClient(brokerHostname);
		} catch (Exception e) {
			System.out.println("Error al crear NG controller, nombre del broker invalido");
		}
		shell = new NGShell();
		this.serverHostname = serverHostname;
	}

	public byte getCurrentCommand() {
		return this.currentCommand;
	}

	public void setCurrentCommand(byte command) {
		currentCommand = command;
	}

	public void setCurrentCommandArguments(String[] args) {
		//According to the command we register the related parameters
		//We also check if the command is valid in the current state
		switch (currentCommand) {
		case NGCommands.COM_NICK:
			nickname = args[0];
			break;
		case NGCommands.COM_ENTER:
			room = args[0];
			break;
		case NGCommands.COM_ANSWER:
			answer = args[0];
			break;
		default:
		}
	}

	//Process commands provided by the users when they are not in a room 
	public void processCommand() {
		switch (currentCommand) {
		case NGCommands.COM_TOKEN:
			getTokenAndDeliver();
			break;
		case NGCommands.COM_NICK:
			if (!nickIntroducido) {
				registerNickName();
			}
			else
				System.out.println("Ya has introducido el nick");
			
			break;
		case NGCommands.COM_ROOMLIST:
			getAndShowRooms();
			break;
		case NGCommands.COM_ENTER:
			enterTheGame();
			break;
		case NGCommands.COM_QUIT:
			ngClient.disconnect();			
			brokerClient.close();
			break;
		default:
		}
	}
	
	private void getAndShowRooms() {
		try {
			List<NGRoomDescription> lista = ngClient.getRoomList();
			for (NGRoomDescription i : lista) {
				System.out.println(i.toString());
			}
		} catch (IOException e) {
			System.out.println("Error en la lectura de la lista de salas");
		}
	}

	private void registerNickName() {
		try {
			if (ngClient.registerNickname(nickname)){ 
				nickIntroducido=true;
				System.out.println("* Your nickname was succesfully registered");	
			}
			else {
				nickIntroducido=false;
				System.out.println("* The nickname already exists.");
			}
		} catch (IOException e) {
			System.out.println("* Check your connection, the game server is not available.");
			
		}
	}

	private void enterTheGame() {
		//The users request to enter in the room
		short roomNum;
		try {
			roomNum = Short.parseShort(room);
		}
		catch (NumberFormatException e) {
			roomNum=-1;
		}
		try {
			if (ngClient.requestEnterRoom(roomNum)) {
				//If success, we change the state in order to accept new commands
				System.out.println(ngClient.receiveRules());
				Set<NGPlayerInfo> players = ngClient.receiveStatus();
				System.out.println("[ LEADERBOARD ]");
				for (NGPlayerInfo i : players)
					System.out.println(i);
				
				do {
					//We will only accept commands related to a room
					readGameCommandFromShell();
					processGameCommand();
				} while (currentCommand != NGCommands.COM_EXIT);
			}
			else
				System.out.println("* La sala especificada no existe o esta llena.");
		} catch (IOException e) {
			System.out.println("* Check your connection, the game server is not available.");
		}
	}

	private void processGameCommand() {
		switch (currentCommand) {
		case NGCommands.COM_RULES:
			rules();
			break;
		case NGCommands.COM_STATUS:
			status();
			break;
		case NGCommands.COM_ANSWER:
			if (hayChallenge) {
				sendAnswer();
				hayChallenge = false;
			}
			else
				System.out.println("No es tu turno");
			break;
		case NGCommands.COM_SOCKET_IN:
			//In this case the user did not provide a command but an incoming message was received from the server
			processGameMessage();
			break;
		case NGCommands.COM_EXIT:
			exitTheGame();
		}		
	}

	private void exitTheGame() {
		short roomNum = Short.parseShort(room);
		try {
			System.out.println("* Exiting room "+roomNum);
			ngClient.sendExitRoom(roomNum);
		} catch (IOException e) {
			System.out.println("* Check your connection, the game server is not available.");
		}
	}
	
	private void rules() {
	try {
		ngClient.getRules();
	} catch (IOException e) {
		System.out.println("Error en la lectura de Reglas");
	}
	}
	
	private void status() {
	try {
		ngClient.getStatus();
	} catch (IOException e) {
		System.out.println("Error en la lectura de Status");
	}}
	
	private void sendAnswer() {
		ngClient.sendAnswerToServer(answer);
	}

	private void processGameMessage() {	
		//This method processes the incoming message received when the shell was waiting for a user command
		NGMessage mensaje = ngClient.recibirInfoServer();
		switch (mensaje.getOpcode()) {
		case NGMessage.OP_MATRIX:
			System.out.println("[ TABLERO DE JUEGO ]");
			NGMatrixMessage mensajeChallenge = (NGMatrixMessage) mensaje;
			char[][] tablero = mensajeChallenge.getMatrix();
			for (int i = 0; i<mensajeChallenge.getFilas(); i++){
				for (int j = 0; j<mensajeChallenge.getColumnas(); j++)
					System.out.print("| "+tablero[i][j]+" ");
				System.out.println("|");
			}
			for (int j = 0; j<mensajeChallenge.getColumnas(); j++)
				System.out.print("----");
			System.out.println("-");
			for (int j = 0; j<mensajeChallenge.getColumnas(); j++)
				System.out.print("| "+(j+1)+" ");
			System.out.println("|");
			System.out.println("* Te toca "+ nickname+". Date prisa!! ");
			hayChallenge=true;	
			break;
			
		case NGMessage.OP_SEND_PLAYERS:
			NGPlayersMessage mensajeStatus = (NGPlayersMessage) mensaje;
			System.out.println("[ LEADERBOARD ]");
			for (NGPlayerInfo i : mensajeStatus.getPlayers())
				System.out.println(i);
			break;
			
		case NGMessage.OP_TIMEOUT:
			hayChallenge=false;
			System.out.println("* El tiempo ha expirado!");
			break;
		
		case NGMessage.OP_SEND_RULES:
			NGStringMessage reglas = (NGStringMessage) mensaje;
			System.out.println(reglas.getString());
			break;	
		}
	}

	//Method to obtain the token from the Broker
	private void getTokenAndDeliver() {
		//There will be a max number of attempts
		int attempts = MAX_NUMBER_OF_ATTEMPTS;
		//We try to obtain a token from the broker
		boolean cond = false;
		int i = 0;
		while (!cond && i<attempts) {
			try {
				token = brokerClient.getToken();
				cond = true;
			} catch (IOException e) {
				System.out.println("* Check your connection, the token server is not available.");
			}
			i++;
		}
		//If we have a token then we will send it to the game server
		if (token != 0) {
			try {
				//We initialize the game client to be used to connect with the name server
				ngClient = new NGGameClient(serverHostname);
				//We send the token in order to verify it
				if (!ngClient.verifyToken(token)) {
					System.out.println("* The token is not valid.");	
					token = 0;
				}
			} catch (IOException e) {
				System.out.println("* Check your connection, the game server is not available.");
				token = 0;
			}
		}
	}
	
	public void readGameCommandFromShell() {
		//We ask for a new game command to the Shell (and parameters if any)
		shell.readGameCommand(ngClient);
		setCurrentCommand(shell.getCommand());
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	public void readGeneralCommandFromShell() {
		//We ask for a general command to the Shell (and parameters if any)
		shell.readGeneralCommand();
		setCurrentCommand(shell.getCommand());
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	public boolean sendToken() {
		//We simulate that the Token is a command provided by the user in order to reuse the existing code
		System.out.println("* Obtaining the token...");
		setCurrentCommand(NGCommands.COM_TOKEN);
		processCommand();
		if (token != 0) {
			System.out.println("* Token is "+ token + "and it was validated by the server.");
			System.out.println("* Introduce your nick with the command: \"nick <your_nick>\".");
		}		
		return (token != 0);
	}

	public boolean shouldQuit() {
		return currentCommand == NGCommands.COM_QUIT;
	}

}
