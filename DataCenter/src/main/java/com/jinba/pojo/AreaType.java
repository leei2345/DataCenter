package com.jinba.pojo;

public enum AreaType {
	
	FirstStemp(0),
	District(1),
	DistrictCounty(2),
	Nomal(3),
	;
	
	public int rank;

	private AreaType (int rank) {
		this.rank = rank;
	}

}
