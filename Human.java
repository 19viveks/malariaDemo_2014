package malariademo;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public abstract class Human
{
	static int humanPopulation = 100;
	
	ContinuousSpace<Object> space;
	Grid<Object> grid;
	int energy;
	
	public Human(ContinuousSpace<Object> space, Grid<Object> grid, int energy)
	{
		this.space = space;
		this.grid = grid;
		this.energy = energy;
	}
	
}
