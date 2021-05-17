package model;

import java.time.LocalTime;

public class Paziente implements Comparable <Paziente>{
	
	public enum ColorCode{
		NEW, //in triage
		WHITE, YELLOW, RED, BLACK, //in sala di attesa
		TREATING, //dentro studio medico
		OUT // acasa, abbandonato o curato
	};
	
	private int num;
	private LocalTime arrivalTime;
	private ColorCode color;
	
	public Paziente(int num, LocalTime arrivalTime, ColorCode color) {
		this.num=num;
		this.arrivalTime = arrivalTime;
		this.color = color;
	}
	public LocalTime getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(LocalTime arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public ColorCode getColor() {
		return color;
	}
	public void setColor(ColorCode color) {
		this.color = color;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	@Override
	public String toString() {
		return "Paziente [num=" + num + ", arrivalTime=" + arrivalTime + ", color=" + color + "]";
	}
	@Override
	public int compareTo(Paziente other) {
		if(this.color.equals(other.color))
			return this.arrivalTime.compareTo(other.arrivalTime);
		else if(this.color.equals(Paziente.ColorCode.RED))
			return -1; //passa per primo quello con valore più piccolo -> passa this
		else if(other.color.equals(Paziente.ColorCode.RED))
			return +1; //-> passa other
		else if(this.color.equals(Paziente.ColorCode.YELLOW)) // Y - W
			return -1;
		else	// W - Y
			return +1;
		
	}


	

}
