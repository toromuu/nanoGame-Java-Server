package es.um.redes.nanoGames.message;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import es.um.redes.nanoGames.server.NGPlayerInfo;

public class NGPlayersMessage extends NGMessage {

	// ATRIBUTOS
	private Set<NGPlayerInfo> players;
		
	// CONTRUCTOR
	public NGPlayersMessage (byte code, Set<NGPlayerInfo> players) {
		opcode = code;
		this.players=players;
	}
	
	// FUNCIONALIDAD	
	@Override
	public byte[] toByteArray() {
		int totalSize = 1+4; // Opcode (1 byte) + numero de jugadores (Int, 4 bytes)
		for (NGPlayerInfo i : players)
			totalSize+=i.toByteArray().length; // longitud del nombre (4 bytes) + nombre (nombre.length() bytes) + score (1 byte)
	
		ByteBuffer bb = ByteBuffer.allocate(totalSize);
		bb.put(opcode); //Inserta un campo de 1 byte (opcode es byte)
		bb.putInt(players.size());
		for (NGPlayerInfo i : players){
			bb.put(i.toByteArray());
		}
		byte[] men = bb.array();    
		return men;
	}

	public static NGPlayersMessage readFromIS(byte code, DataInputStream dis) throws IOException {
		
		Set<NGPlayerInfo> conjunto = new HashSet<NGPlayerInfo>();
		int nJugadores=dis.readInt();
		for (int i = 0; i<nJugadores; i++)
			conjunto.add(NGPlayerInfo.readFromIS(dis));
		return new NGPlayersMessage(code, conjunto);
		
	}

	public Set<NGPlayerInfo> getPlayers() {
		return players;
	}
	
	
}
