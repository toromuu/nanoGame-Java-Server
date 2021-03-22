package es.um.redes.nanoGames.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.um.redes.nanoGames.server.roomManager.NGRoomDescription;
import es.um.redes.nanoGames.server.roomManager.NGRoomManager;

/**
 * This class contains the general status of the whole server (without the logic related to particular games)
 */
class NGServerManager {
	
	//Players registered in this server
	private HashMap<String,NGPlayerInfo> players;
	//Current rooms and their related RoomManagers
	private Map<Short,NGRoomManager> roomManagerList;
	short contadorSalas;
	
	NGServerManager() {
		players = new HashMap<String,NGPlayerInfo>();
		roomManagerList = new HashMap<Short,NGRoomManager>();
		contadorSalas=0;
	}
	
	public void registerRoomManager(NGRoomManager rm) {
		roomManagerList.put(rm.getId(),rm);
		contadorSalas++;
	}
	
	//Returns the set of existing rooms
	public synchronized List<NGRoomDescription> getRoomList() {
		List<NGRoomDescription> roomList = new ArrayList<NGRoomDescription>();
		for (NGRoomManager i : roomManagerList.values()) {
			roomList.add(i.getDescription());
		}
		return roomList;
	}
	
	//Given a room it returns the description
	// No lo usamos
	public synchronized NGRoomDescription getRoomDescription(short room) {
		return roomManagerList.get(room).getDescription();
	}
	
	//False is returned if the nickname is already registered, True otherwise and the player is registered
	public synchronized boolean addPlayer(NGPlayerInfo player) {
		if (players.containsKey(player.getNick())) {
			return false;
		}
		else {
			players.put(player.getNick(), player);
			return true;
		}
	}
	
	//The player is removed from the list
	public synchronized void removePlayer(NGPlayerInfo player) {
			players.remove(player.getNick());
	}
	
	//A player request to enter in a room. If the access is granted the RoomManager is returned, para que el ServerThread se comunique directamente con el RoomManager
	public synchronized NGRoomManager enterRoom(NGPlayerInfo p, short room) {
		if (roomManagerList.containsKey(room)) {
			// Si existe la habitacion, anadimos al jugador
			NGRoomManager roomManager = roomManagerList.get(room);
			if (roomManager.registerPlayer(p)) {
				return roomManager;
			}
			else return null;
		}
		return null;
	}
	
	//A player leaves the room 
	public synchronized void leaveRoom(NGPlayerInfo p, short room) {
		if (roomManagerList.containsKey(room))
			roomManagerList.get(room).removePlayer(p);
	}
}
