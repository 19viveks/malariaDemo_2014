package malariademo;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import repast.simphony.random.RandomHelper;
import repast.simphony.context.*;

public class Mosquito
{
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int energy;
	private boolean moved;
	
	public static int mosquitoPopulation = 50;
	
	public Mosquito(ContinuousSpace<Object> space, Grid<Object> grid, int energy)
	{
		super();
		this.space = space;
		this.grid = grid;
		this.energy = energy;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step()
	{
		//get the grid location of this mosquito
		GridPoint pt = grid.getLocation(this);
		
		//use the GridCellNgh class to create GridCells for the surrounding neighborhood
		GridCellNgh<HealthyHuman> nghCreator = new GridCellNgh<HealthyHuman>(grid, pt, HealthyHuman.class, 1, 1);
		List<GridCell<HealthyHuman>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		GridPoint pointWithMostHumans = null;
		int maxCount = -1;
		for(GridCell<HealthyHuman> cell : gridCells)
		{
			if(cell.size() > maxCount)
			{
				pointWithMostHumans = cell.getPoint();
				maxCount = cell.size();
			}
		}
		
		
		if(energy > 1)
		{
			if(energy > 10)
				this.reproduce();
			
			boolean moved = this.moveTowards(pointWithMostHumans);
			if(!moved)
				energy++;
			
			infect();
		}
		
		else
		{
				this.die();
		}
	}
	
	
	public boolean moveTowards(GridPoint pt)
	{
		//only move if we are not already in this grid location
		if(!pt.equals(grid.getLocation(this)))
		{
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			space.moveByVector(this, 1, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int)myPoint.getX(), (int)myPoint.getY());
			
			energy--;
			moved = true;
			return moved;
		}
		
		else
			return false;
	}
	
	
	public void infect()
	{
		GridPoint pt = grid.getLocation(this);
		List<Object> humans = new ArrayList<Object>();
		for(Object obj : grid.getObjectsAt(pt.getX(),pt.getY()))
		{
			if(obj instanceof Human)
				humans.add(obj);
		}
		
		if(humans.size() > 0)
		{
			int index = RandomHelper.nextIntFromTo(0, humans.size() - 1);
			Object obj = humans.get(index);
			NdPoint spacePt = space.getLocation(obj);
			Context<Object> context = ContextUtils.getContext(obj);
			context.remove(obj);
			
			int energy = RandomHelper.nextIntFromTo(4, 10);
			Human human = new InfectedHuman(space, grid, energy);
			
			context.add(human);
			space.moveTo(human, spacePt.getX(), spacePt.getY());
			grid.moveTo(human, pt.getX(), pt.getY());
			
			Network<Object> net = (Network<Object>)context.getProjection("infection network");
			net.addEdge(this, human);
			
			this.energy++;
		} 
	} 
	
	
	public void die()
	{	
		NdPoint spacePt = space.getLocation(this);
		Context<Object> context = ContextUtils.getContext(this);
		context.remove(this);	
		
		mosquitoPopulation--;
	}
	
	
	public void reproduce()
	{
		GridPoint pt = grid.getLocation(this);
		NdPoint spacePt = space.getLocation(this);
		Context<Object> context = ContextUtils.getContext(this);
		
		int energy = RandomHelper.nextIntFromTo(4, 10);
		Mosquito mosquito = new Mosquito(space, grid, energy);
		context.add(mosquito);
		space.moveTo(mosquito, spacePt.getX(), spacePt.getY());
		grid.moveTo(mosquito, pt.getX(), pt.getY());
		
		mosquitoPopulation++;
	}
	
}
