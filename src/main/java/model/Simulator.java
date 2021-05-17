package model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import model.Event.EventType;
import model.Paziente.ColorCode;

public class Simulator {
	
	//coda degli eventi
	PriorityQueue<Event>queue;
	
	//modello del mondo
	private List<Paziente> pazienti;
	private int freeStudios; //numero studi liberi
	private Paziente.ColorCode ultimoColore; 
	private PriorityQueue <Paziente> waitingRoom; //contiene solo i pazienti in lista di attesa (WHITE/YELLOW/RED)
	
	
	//parametri di input
	private int totStudios = 3; //NS
	private int numPazienti = 120; //NP
	
	private Duration T_ARRIVAL = Duration.ofMinutes(5);
	
	private Duration DURATION_TRIAGE = Duration.ofMinutes(5);
	private Duration DURATION_WHITE = Duration.ofMinutes(10);
	private Duration DURATION_YELLOW = Duration.ofMinutes(15);
	private Duration DURATION_RED = Duration.ofMinutes(30);
	
	private Duration TIMEOUT_WHITE= Duration.ofMinutes(60);
	private Duration TIMEOUT_YELLOW = Duration.ofMinutes(30);
	private Duration TIMEOUT_RED= Duration.ofMinutes(30);
	
	private LocalTime startTime = LocalTime.of(8, 00);
	private LocalTime endTime = LocalTime.of(20, 00);
	
	//parametri di output
	private int pazientiTreated;
	private int pazientiAbandoned;
	private int pazientiDead;
	
	
	//inizializzazione del simulatore e creazione degli eventi iniziali
	public void init() {
		
		//ogni volta che chiamo init() riparto da 0 
		
		//inizializzo coda degli eventi 
		this.queue=new PriorityQueue<>();
		
		//e il modello del mondo
		this.pazienti=new ArrayList <>();
		this.freeStudios=this.totStudios; //tutti liberi
		this.waitingRoom= new PriorityQueue<>();
		
		this.ultimoColore= ColorCode.RED;
		
		//e i parametri di input
		this.pazientiAbandoned=0;
		this.pazientiDead=0;
		this.pazientiTreated=0;
		
		//inietta gli eventi di input di tipo ARRIVAL
		LocalTime ora = this.startTime;
		int inseriti = 0;
		//Paziente.ColorCode colore = ColorCode.WHITE;
		
		this.queue.add(new Event(ora, EventType.TICK, null)); //setto il timer
		
		while(ora.isBefore(this.endTime) && inseriti<this.numPazienti) {
			
			Paziente p = new Paziente (inseriti, ora, ColorCode.NEW);
			Event e = new Event(ora, EventType.ARRIVAL, p);
			
			this.pazienti.add(p);
			this.queue.add(e);
			
			inseriti++;
			ora = ora.plus(T_ARRIVAL);
			
			/*
			if(colore.equals(ColorCode.WHITE))
				colore=ColorCode.YELLOW;
			else if(colore.equals(ColorCode.YELLOW))
				colore=ColorCode.RED;
			else
				colore=ColorCode.WHITE;
			*/
		}
		
	}
	
	
	private Paziente.ColorCode prossimoColore () {
		if(ultimoColore.equals(ColorCode.WHITE))
			ultimoColore=ColorCode.YELLOW;
		else if(ultimoColore.equals(ColorCode.YELLOW))
			ultimoColore=ColorCode.RED;
		else
			ultimoColore=ColorCode.WHITE;
		return ultimoColore;
	}
	
	//esecuzione della simulazione
	public void run() {
		while(!this.queue.isEmpty()) {
			
			Event e = this.queue.poll(); //estraggo evento
			System.out.println(e); //verifico l'evento
			processEvent(e); //processo l'evento
		}
		
	}
	
	private void processEvent(Event e) {
		
		Paziente p = e.getPaziente();
		LocalTime ora = e.getTime();
		
		
		switch(e.getType()) {
		case ARRIVAL:
			this.queue.add(new Event(ora.plus(DURATION_TRIAGE), EventType.TRIAGE, p));
			break;
			
		case TRIAGE:
			p.setColor(prossimoColore());
			if(p.getColor().equals(Paziente.ColorCode.WHITE)) {
				this.queue.add(new Event(ora.plus(TIMEOUT_WHITE), EventType.TIMEOUT, p));
				this.waitingRoom.add(p);
			}
			else if(p.getColor().equals(Paziente.ColorCode.YELLOW)) {
				this.queue.add(new Event(ora.plus(TIMEOUT_YELLOW), EventType.TIMEOUT, p));
				this.waitingRoom.add(p);
			}
			else if(p.getColor().equals(Paziente.ColorCode.RED)) {
				this.queue.add(new Event(ora.plus(TIMEOUT_RED), EventType.TIMEOUT, p));
				this.waitingRoom.add(p);
			}
			break;
			
		case FREE_STUDIO:
			if(this.freeStudios==0)
				return; //se non ci sono studi liberi esco
			
			//se non è 0 allora ce n'è uno libero quindi lo incremento
			//quale paziente ha diritto di entrare?
			Paziente primo= this.waitingRoom.poll(); //estrae il primo
			if(primo!=null) {
				//ammetti il paziente nello studio
				if(primo.getColor().equals(ColorCode.WHITE))
					this.queue.add(new Event(ora.plus(DURATION_WHITE), EventType.TREATED, primo));
				if(primo.getColor().equals(ColorCode.YELLOW))
					this.queue.add(new Event(ora.plus(DURATION_YELLOW), EventType.TREATED, primo));
				if(primo.getColor().equals(ColorCode.YELLOW))
					this.queue.add(new Event(ora.plus(DURATION_YELLOW), EventType.TREATED, primo));
				
				primo.setColor(ColorCode.TREATING);
				this.freeStudios--;
			}
			
			break;
			//controlliamo il caso in cui c'è almeno uno studio libero e almenno un paziente in attesa
			//in pratica ogni tot controlliamo se c'è posto per qualcuno appena arrivato, in condizione di NON pieno - carico
			//cioè senza il tempo di attesa -> vedi caso TICK
			
		case TIMEOUT:
			Paziente.ColorCode colore = p.getColor();
			switch(colore) {
			case WHITE:
				this.waitingRoom.remove(p);
				p.setColor(ColorCode.OUT);
				this.pazientiAbandoned++;
				break;
				
			case YELLOW:
				this.waitingRoom.remove(p);
				p.setColor(ColorCode.RED);
				this.queue.add(new Event(ora.plus(TIMEOUT_RED), EventType.TIMEOUT, p));
				this.waitingRoom.add(p); //gli ho cambiato colore, quinsi sta in posto diverso
				break;
				
			case RED:
				this.waitingRoom.remove(p);
				p.setColor(ColorCode.BLACK);
				this.pazientiDead++;
				break;
				
			default:
				//System.out.println("ERRORE: TIMEOUT CON COLORE "+colore);
			}
			break;
			
		case TREATED:
			this.pazientiTreated++;
			p.setColor(ColorCode.OUT);
			this.freeStudios++;
			this.queue.add(new Event(ora, EventType.FREE_STUDIO, null));
			break;
			
		case TICK:
			if(this.freeStudios>0 && !this.waitingRoom.isEmpty())
				this.queue.add(new Event(ora, EventType.FREE_STUDIO, null));
			if(ora.isBefore(endTime))
				this.queue.add(new Event(ora.plus(Duration.ofMinutes(5)), EventType.TICK, null)); //TICK si autorigenera da solo
			break;
			
		}
		
	}

	//set per i parametri in ingresso
	
	public void setTotStudios(int totStudios) {
		this.totStudios = totStudios;
	}

	public void setNumPazienti(int numPazienti) {
		this.numPazienti = numPazienti;
	}

	public void setT_ARRIVAL(Duration t_ARRIVAL) {
		T_ARRIVAL = t_ARRIVAL;
	}

	public void setDURATION_TRIAGE(Duration dURATION_TRIAGE) {
		DURATION_TRIAGE = dURATION_TRIAGE;
	}

	public void setDURATION_WHITE(Duration dURATION_WHITE) {
		DURATION_WHITE = dURATION_WHITE;
	}

	public void setDURATION_YELLOW(Duration dURATION_YELLOW) {
		DURATION_YELLOW = dURATION_YELLOW;
	}

	public void setDURATION_RED(Duration dURATION_RED) {
		DURATION_RED = dURATION_RED;
	}

	public void setTIMEOUT_WHITE(Duration tIMEOUT_WHITE) {
		TIMEOUT_WHITE = tIMEOUT_WHITE;
	}

	public void setTIMEOUT_YELLOW(Duration tIMEOUT_YELLOW) {
		TIMEOUT_YELLOW = tIMEOUT_YELLOW;
	}

	public void setTIMEOUT_RED(Duration tIMEOUT_RED) {
		TIMEOUT_RED = tIMEOUT_RED;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}
	
	//get per i parametri in uscita

	public int getPazientiTreated() {
		return pazientiTreated;
	}

	public int getPazientiAbandoned() {
		return pazientiAbandoned;
	}

	public int getPazientiDead() {
		return pazientiDead;
	}
	
	
}
