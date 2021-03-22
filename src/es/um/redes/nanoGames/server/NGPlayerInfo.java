package es.um.redes.nanoGames.server;
/**
 * Representa la informacion sobre un jugador
 * en nuestro caso (nick y score: que es el número de partidas ganadas)
 */
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import es.um.redes.nanoGames.message.NGMessage;

public class NGPlayerInfo {


	//Constructor to make copies
	public NGPlayerInfo(NGPlayerInfo p) {
		this.nick = new String(p.nick);
		this.score = p.score;
	}
	
	// Constructor con el nombre
	public NGPlayerInfo(String nick) {
		this.nick = nick;
	}
	
	public NGPlayerInfo(String nick, byte score) {
		this.nick = nick;
		this.score = score;
	}
	
	//Default constructor
	public NGPlayerInfo() {
		
	}
	
	public String getNick(){
		return nick;
	}
	
	public byte[] toByteArray(){
		int playerLong = nick.getBytes().length;
		ByteBuffer bb = ByteBuffer.allocate(4+nick.getBytes().length+1);
		bb.putInt(playerLong);
		bb.put(nick.getBytes());
		bb.put(score);
		byte[] men = bb.array();
		return men;
	}
	
	public void incrementarScore(){
		score++;
	}
	
	public static NGPlayerInfo readFromIS(DataInputStream dis) throws IOException {
		String nick = NGMessage.readString(dis);
		byte score = dis.readByte();
		return new NGPlayerInfo(nick, score);
	}
	
	//TODO Include additional fields if required
	public String nick; //Nickname of the user
	public byte score;  //Current score of the user
	public short challengeNumber;
	public short statusNumber;
	
	public byte getScore() {
		return score;
	}

	public void setScore(byte score) {
		this.score = score;
	}

	@Override
	public String toString(){
		return "[Player : " + nick + "  \t|  Score : " + score+"]";
	}

	
}

