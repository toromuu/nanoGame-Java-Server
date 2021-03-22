package es.um.redes.nanoGames.server.roomManager;

import java.util.Set;

import es.um.redes.nanoGames.server.NGPlayerInfo;

/**
 * Representa la información actual sobre el estado del juego
 * la cual se corresponde con los jugadores en la sala, por tanto 
 * este cambiara cuando se produzcan cambios sobre estos, como 
 * la salida-entrada un jugador o cambios en la puntuacion
 */


public class NGRoomStatus {
	public short statusNumber;
	public Set<NGPlayerInfo> players;

	//Status initialization
	NGRoomStatus() {
		statusNumber = 0;
		players = null;
		
	}
	
	// constructor
	public NGRoomStatus(short currentStatus, Set<NGPlayerInfo> players) {
		statusNumber = currentStatus;
		this.players = players;
	
	}
	
	//metodos get
	public short getStatusNumber() {
		return statusNumber;
	}

	public Set<NGPlayerInfo> getPlayers() {
		return players;
	}
}
