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
	// Easy way to not have to mess with floats/doubles
	private short servingTenths;


	public Food(FoodType name){
		this.name = name;
		servingTenths = 0;
	}
	
	
	public FoodType getName(){
		return name;
	}
	
	
	public void incServing(PortionSize portion){
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
	
	public void decServing(PortionSize portion){
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
	

	public short getServingTenths() {
		return servingTenths;
	}

	public void setServingTenths(short servingTenths) {
		this.servingTenths = servingTenths;
	}
	
	
}
