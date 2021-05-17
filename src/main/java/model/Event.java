package model;

import java.time.LocalTime;

public class Event implements Comparable <Event> {
	
	
	enum EventType{
			ARRIVAL, //arriva un nuovo paziente e subito entra in triage
			TRIAGE, //è finito triage ed entra in sala di attesa
			TIMEOUT,// o mi sono stufato, o sono morto... è passato un tempo di attesa
			FREE_STUDIO, //studio liberato, posso chiamare qualcuno
			TREATED, //paziente curato
			TICK, //timer per controllare se ci sono studi liberi
	};
	
	private LocalTime time;
	private EventType type;
	private Paziente paziente;
	
	
	public Event(LocalTime time, EventType type, Paziente paziente) {
		super();
		this.time = time;
		this.type = type;
		this.paziente = paziente;
	}
	public LocalTime getTime() {
		return time;
	}
	public void setTime(LocalTime time) {
		this.time = time;
	}
	public EventType getType() {
		return type;
	}
	public void setType(EventType type) {
		this.type = type;
	}
	public Paziente getPaziente() {
		return paziente;
	}
	public void setPaziente(Paziente paziente) {
		this.paziente = paziente;
	}
	public int compareTo(Event other) {
		return this.time.compareTo(other.time);
	}
	@Override
	public String toString() {
		return "Event [time=" + time + ", type=" + type + ", paziente=" + paziente + "]";
	}

}
