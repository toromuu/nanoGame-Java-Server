package es.um.redes.nanoGames.server.roomManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;


import es.um.redes.nanoGames.server.NGPlayerInfo;
public class NGCountSymbolsManager extends NGRoomManager {
	
	private char[][] tablero;
	private int solucionOs = 0;
	private int contadorRespuestas;
	private List<String> haRespondido;
	
	
	
	public NGCountSymbolsManager(short id, int filas, int columnas) {
		this.id = id;
		this.registrationName = (byte) 1;
		this.players = new HashSet<NGPlayerInfo>();
		rules = new File("CountSymbols");
		tablero = new char[filas][columnas];
		Tablero();
		challengeNumber=0;
		statusNumber=0;
		gameTimeout= 7000;
		haRespondido = new ArrayList<String>();
	}
	
	public NGCountSymbolsManager(short id) {
		this(id, 4, 4);	
	}

	public int getAltura() {
		return tablero.length;
	}
	
	public int getAnchura() {
		return tablero[0].length;
	}
	
	@Override
	public synchronized boolean registerPlayer(NGPlayerInfo p) {
		if (players.size() < 5){ 
			players.add(p);
			statusNumber++;
			if (challengeNumber==0 && players.size()>=2) 
				challengeNumber++;
				Tablero();
			contadorRespuestas++;
			return true;
		}
		System.out.println("La sala esta llena");
		return false;
	}

	@Override
	public synchronized NGRoomStatus checkStatus(NGPlayerInfo p) {
			return new NGRoomStatus(statusNumber, players );
		
	}

	@Override
	public synchronized NGChallenge checkChallenge(NGPlayerInfo p) {
		if (players.size()>=2) // Si hya jugadores suficientes
			
			return new NGChallenge(challengeNumber, tablero);
		return null;
	}

	@Override
	public synchronized NGRoomStatus noAnswer(NGPlayerInfo p) {
		contadorRespuestas--;
		haRespondido.add(p.getNick());
		gameTimeout= 7000;
		if (contadorRespuestas==0) {
			challengeNumber++;
			Tablero();
			statusNumber++;
			contadorRespuestas=playersInRoom();
			haRespondido = new ArrayList<String>();
			
		}	
		return new NGRoomStatus(statusNumber, players);
	}

	@Override
	public synchronized NGRoomStatus answer(NGPlayerInfo p, String answer) {
		// Contesta otro jugador
		contadorRespuestas--;
		haRespondido.add(p.getNick());
		
		int y;
		try {
		y = Integer.parseInt(answer);
		}
		catch (NumberFormatException e) {
			y=-1;
		}		
		//Comprobar si se ha ganado
		
		if (respuestaCorrecta(y)) {
			for (NGPlayerInfo i : players)
				if (i.getNick().equals(p.getNick()))
					i.incrementarScore();
		}
		
		if (contadorRespuestas==0) {
			challengeNumber++;
			statusNumber++;
			Tablero();
			contadorRespuestas=playersInRoom();
			haRespondido = new ArrayList<String>();
			
		}	
		gameTimeout=7000;
		return new NGRoomStatus(statusNumber, players);	
	}


	@Override
	public synchronized void removePlayer(NGPlayerInfo p) {
		for (NGPlayerInfo i : players)
			if (i.getNick().equals(p.getNick()))
				i.score=0;
		if (haRespondido.contains(p.getNick())) 
			haRespondido.remove(p.getNick());
		else
			contadorRespuestas--;
		players.remove(p);
		statusNumber++;
	}

	@Override
	public NGRoomManager duplicate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getRegistrationName() {
		return registrationName;
	}

	@Override
	public synchronized NGRoomDescription getDescription() {
		return new NGRoomDescription(id, registrationName, players);
	}

	@Override
	public synchronized int playersInRoom() {
		return players.size();
	}

	
	//FUNCIONES AUXILIARES DE LA LOGICA DEL JUEGO

	
	protected boolean respuestaCorrecta(int y) {
		return (y == solucionOs);
	}
	
	
	protected void Tablero() {
		int numero;
		solucionOs=0;
		for (int i = 0; i< getAltura(); i++) {
			for(int j = 0; j< getAnchura(); j++) {
				Random generadorAleatorios = new Random();
				numero= 1+generadorAleatorios.nextInt(7);
			if (numero==2) {tablero[i][j]='o'; solucionOs++;}
			else {tablero[i][j]='c';}
			
			}
		}
	}


	
}
