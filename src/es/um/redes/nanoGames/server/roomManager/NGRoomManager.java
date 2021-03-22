package es.um.redes.nanoGames.server.roomManager;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

import es.um.redes.nanoGames.server.NGPlayerInfo;

public abstract class NGRoomManager {
	short challengeNumber;			//numero de challenge
	short statusNumber;				// numero de status
	File rules;						// fichero con las reglas del juego
	short id;						// id de sala 
	byte registrationName;			// juego de la sala
	Set<NGPlayerInfo> players;		// jugadores de la sala
	int gameTimeout; 				//In milliseconds
	
	
	//the only requirement to add a player is that only MAX_PLAYERS are accepted
	public abstract boolean registerPlayer(NGPlayerInfo p); //Tiene consecuencias sobre el status
	
	//Rules are returned
	public synchronized String getRules() { 
		try {
			return leerFichero(rules);
		} catch (IOException e) {
			System.out.println("Se requiere un fichero en formato '(Nombre del juego)Rules' en el directorio");
			return null;
		}
	}
	
	public synchronized short getStatusNumber() {
		return statusNumber;
	}
	public synchronized short getChallengeNumber() {
		return challengeNumber;
	}
	
	public short getId() {
		return id;
	}
	
	
	//The current status is returned
	public abstract NGRoomStatus checkStatus(NGPlayerInfo p);
	//Check for a new challenge. We can make use of that checking in order to build a new one if the conditions are satisfied 
	public abstract NGChallenge checkChallenge(NGPlayerInfo p);
	//The player provided no answer and we process that situation
	public abstract NGRoomStatus noAnswer(NGPlayerInfo p);
	//The answer provided by the player has to be processed
	public abstract NGRoomStatus answer(NGPlayerInfo p, String answer);
	//The player is removed (maybe the status has to be updated)
	public abstract void removePlayer(NGPlayerInfo p);
	//Creates a copy of the room manager
	public abstract NGRoomManager duplicate();
	//Returns the name of the game
	public abstract byte getRegistrationName();
	//Returns the description of the room
	public abstract NGRoomDescription getDescription();
	//Returns the current number of players in the room
	
	public int playersInRoom() {
		return players.size();
	}
	
	public static NGRoomManager copy(NGRoomManager toCopy) {
		return toCopy.duplicate();
	}
	
	public  synchronized int getTimeout() {
		return gameTimeout;
	}
	
	
	
	
	//FUNCION AUXILIAR PARA LA LECTURA DE FICHEROS
		public static String leerFichero(File fichero) throws IOException {

			Scanner input = new Scanner(fichero, "utf-8"); //Importante el parametro utf-8,para que la codificacion 
														  //de los ficheros de entrada no produzcan errores.
			StringBuffer texto = new StringBuffer();

			while (input.hasNextLine()) {
				texto.append(input.nextLine());
				texto.append(System.getProperty("line.separator"));
			}
			input.close();

			return (texto.toString());
		}
}
