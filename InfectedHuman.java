package malariademo;

import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class InfectedHuman extends Human
{
	private int startingEnergy;
	
	public InfectedHuman(ContinuousSpace<Object> space, Grid<Object> grid, int energy)
	{
		super(space, grid, energy);
		this.startingEnergy = energy;
	}
	
	@ScheduledMethod(start = 1, interval = 2)
	public void step()
	{
		//get the grid location of this Human
		GridPoint pt = grid.getLocation(this);
		
		//use the GridCellNgh class to create GridCells for the surrounding neighborhood.
		GridCellNgh<Mosquito> nghCreator = new GridCellNgh<Mosquito>(grid, pt, Mosquito.class, 1,1);
		List<GridCell<Mosquito>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		GridPoint pointWithLeastMosquitoes = gridCells.get(0).getPoint();
		
		if(energy > 1)
		{
			if(energy > 11)
				this.reproduce();
			
			boolean moved = this.moveTowards(pointWithLeastMosquitoes);
			if(!moved)
				energy++;
		}
		
		else
			this.die();
	}
	
	public boolean moveTowards(GridPoint pt)
	{
		if(!pt.equals(grid.getLocation(this)))
		{
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			space.moveByVector(this,2,angle,0);
			myPoint=space.getLocation(this);
			grid.moveTo(this, (int)myPoint.getX(), (int)myPoint.getY());
			
			energy = energy - 3;
			return true;
		}
		
		else
			return false;
	}
	
	public void die()
	{	
		NdPoint spacePt = space.getLocation(this);
		Context<Object> context = ContextUtils.getContext(this);
		context.remove(this);	
		
		humanPopulation--;
	}
	
	
	public void reproduce()
	{
		GridPoint pt = grid.getLocation(this);
		NdPoint spacePt = space.getLocation(this);
		Context<Object> context = ContextUtils.getContext(this);
		
		double index = 10* Math.random();
		
		if(index >= 8)
		{
			Human human = new HealthyHuman(space, grid, startingEnergy);
			context.add(human);
			space.moveTo(human, spacePt.getX(), spacePt.getY());
			grid.moveTo(human, pt.getX(), pt.getY());
		}
		
		else
		{
			Human human = new InfectedHuman(space, grid, startingEnergy);
			context.add(human);
			space.moveTo(human, spacePt.getX(), spacePt.getY());
			grid.moveTo(human, pt.getX(), pt.getY());
		}
		
		humanPopulation++;
	}
}
