package es.um.redes.nanoGames.server.roomManager;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import es.um.redes.nanoGames.message.NGMessage;
import es.um.redes.nanoGames.server.NGPlayerInfo;


public class NGRoomDescription {
	
	// ATRIBUTOS  DE LAS SALAS
	public static final byte CONECTA_CUATRO = 0;
	public static final byte COUNT_SYMBOLS = 1;
	private short id;
	private byte game;
	private Set<String> players;
	
	
	// CONSTRUCTOR
	public NGRoomDescription(short id, byte game) {
		this.id = id;
		this.game = game;
		players = new HashSet<String>();
	}
	
	public NGRoomDescription(short id, byte game, Set<NGPlayerInfo> players) {
		this.id = id;
		this.game = game;
		this.players = new HashSet<String>();
		for (NGPlayerInfo i : players)
			this.players.add(i.getNick());
	}

	
	
	// CONSTRUCTOR DE COPIA
	public NGRoomDescription(NGRoomDescription description) {
		this.id = description.id;
		this.game = description.game;
		this.players = new HashSet<String>(description.players);
	}


	public short getId() {
		return id;
	}


	public byte getGame() {
		return game;
	}


	public Set<String> getPlayers() {	
		return new HashSet<String>(players);
	}
		
	// FUNCIONALIDAD
	
	
	public byte[] toByteArray(){
		int sum=0;
		// Recorremos para saber los bytes que ocupan todos los nombres
		for (String i : players) 
			sum+=4 +i.getBytes().length;	// Espacio para representar la longitud del nombre (4 byts) + el nombre
		
		ByteBuffer bb = ByteBuffer.allocate(2+1+1+sum);
		bb.putShort(id);
		bb.put(game);
		bb.put( (byte) players.size());
		for (String i : players) {
			bb.putInt(i.getBytes().length);
			bb.put(i.getBytes());
		}
		byte[] men = bb.array();
		return men;
			
	}
	
	
	
	public static NGRoomDescription readFromIS(DataInputStream dis) throws IOException {
		short id = dis.readShort();
		byte game = dis.readByte();
		NGRoomDescription sala = new NGRoomDescription(id, game);
		byte numPlayers = dis.readByte();
		for (int i=0; i<numPlayers; i++) {
			String player = NGMessage.readString(dis);
			sala.players.add(player);
		}
		return sala;
		
	}
	
	@Override
	public String toString() {
		String juegoString = null;
		switch (game) {
		case CONECTA_CUATRO:
			juegoString = "Conecta cuatro";
			break;
				
		case COUNT_SYMBOLS:
			juegoString = "Cuenta simbolos";
			break;

		default:
			break;
		}
		return "Sala "+id+" -->   Juego: "+juegoString+"   |   Jugadores: "+players.toString();
	}
	
}
