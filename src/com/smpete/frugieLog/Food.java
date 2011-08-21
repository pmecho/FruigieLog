package com.smpete.frugieLog;

public class Food{

	public static enum PortionSize{
		FULL,
		HALF
	};
	
	public static enum FoodType{
		FRUIT,
		VEGGIE
	};

	private FoodType name;
	private PortionSize portion;
	// Easy way to not have to mess with floats/doubles
	private short servingTenths;


	public Food(FoodType name){
		this.name = name;
		portion =  PortionSize.FULL;
		servingTenths = 0;
	}
	
	
	public FoodType getName(){
		return name;
	}
	
	
	public void incServing(){
		switch(portion){
		case FULL:
			servingTenths += 10;
			break;
		case HALF:
			servingTenths += 5;
			break;
		default:
				
		}
		
		if(servingTenths < 0)
			servingTenths = 0;
	}
	
	public void decServing(){
		switch(portion){
		case FULL:
			servingTenths -= 10;
			break;
		case HALF:
			servingTenths -= 5;
			break;
		default:
				
		}
		if(servingTenths < 0)
			servingTenths = 0;
	}
	

	public void switchPortion(){
		switch(portion){
		case FULL:
			portion = PortionSize.HALF;
			break;
		case HALF:
			portion = PortionSize.FULL;
			break;
		default:
		}
	}
	
	public void setPortion(PortionSize portion) {
		this.portion = portion;
	}

	public PortionSize getPortion() {
		return portion;
	}

	public short getServingTenths() {
		return servingTenths;
	}

	public void setServingTenths(short servingTenths) {
		this.servingTenths = servingTenths;
	}
	
	
}
