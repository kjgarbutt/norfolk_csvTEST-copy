package sim.app.geo.norfolk_csvTEST;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.planargraph.DirectedEdgeStar;
import com.vividsolutions.jts.planargraph.Node;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.geo.GeomPlanarGraphDirectedEdge;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;
import sim.util.geo.PointMoveTo;

/**
 * A simple agent that moves around randomly, has some attributes like 
 * rate of movement and 'age' that are assigned at random.
 * 
 * @author KJGarbutt
 *
 */
public class LimitedActionsAgent implements Steppable {
    private static final long serialVersionUID = -1113018274619047013L;
    
    //////////Parameters ///////////////////////////////////
    private MasonGeometry location; // point that denotes agent's position
    private LengthIndexedLine segment = null;
    double startIndex = 0.0; // start position of current line
    double endIndex = 0.0; // end position of current line
    double currentIndex = 0.0; // current location along line
    PointMoveTo pointMoveTo = new PointMoveTo();
    
    //////////Attributes ///////////////////////////////////
    private double basemoveRate = 50.0; // The base speed of the agent.
    // How much to move the agent by in each step(); may become negative if
    // agent is moving from the end to the start of current line.
    private double moveRate = basemoveRate;
    // Used by agent to walk along line segment; assigned in setNewRoute()

    static private GeometryFactory fact = new GeometryFactory();

    /**
	 * Constructor: specifies parameters for Agents
	 * 
	 */
    public LimitedActionsAgent(NorfolkCSVTEST state)	{
        location = new MasonGeometry(fact.createPoint(new Coordinate(10, 10))); // magic numbers
        location.isMovable = true;
        // Find the first line segment and set our position over the start coordinate.
        int posStart = state.random.nextInt(state.roads.getGeometries().numObjs);
        MasonGeometry mg = (MasonGeometry) state.roads.getGeometries().objs[posStart];
        setNewRoute((LineString) mg.getGeometry(), true);
        
        // Not everyone walks at the same speed so mix it up
       //basemoveRate *= Math.abs(state.random.nextGaussian());
       //location.addDoubleAttribute("MOVE RATE", basemoveRate);
    }
    
    /**
     * @return geometry representing agent location
     */
    public MasonGeometry getGeometry()	{
        return location;
    }
   
    // true if the agent has arrived at the target intersection
    private boolean arrived()    {
        // If we have a negative move rate the agent is moving from the end to
        // the start, else the agent is moving in the opposite direction.
        if ((moveRate > 0 && currentIndex >= endIndex)
            || (moveRate < 0 && currentIndex <= startIndex))
        {
            return true;
        }

        return false;
    }

    /**
     * @return string indicating whether we are "FACULTY" or a "STUDENT"
     */
    public String getType()	{
        return location.getStringAttribute("TYPE");
    }

    // randomly selects an adjacent route to traverse
    private void findNewPath(NorfolkCSVTEST geoTest)    {
        // find all the adjacent junctions
        Node currentJunction = geoTest.network.findNode(location.getGeometry().getCoordinate());

        if (currentJunction != null)	{
            DirectedEdgeStar directedEdgeStar = currentJunction.getOutEdges();
            Object[] edges = directedEdgeStar.getEdges().toArray();

            if (edges.length > 0)	{
                // pick one randomly
                int i = geoTest.random.nextInt(edges.length);
                GeomPlanarGraphDirectedEdge directedEdge = (GeomPlanarGraphDirectedEdge) edges[i];
                GeomPlanarGraphEdge edge = (GeomPlanarGraphEdge) directedEdge.getEdge();

                // and start moving along it
                LineString newRoute = edge.getLine();
                Point startPoint = newRoute.getStartPoint();
                Point endPoint = newRoute.getEndPoint();

                if (startPoint.equals(location.geometry))	{
                    setNewRoute(newRoute, true);
                } else	{
                    if (endPoint.equals(location.geometry))
                    {
                        setNewRoute(newRoute, false);
                    } else	{
                        System.err.println("Where am I?");
                    }
                }
            }
        }
    }

    /**
     * have the agent move along new route
     * @param line defining new route
     * @param start true if agent at start of line else agent placed at end
     */
    private void setNewRoute(LineString line, boolean start)    {
        segment = new LengthIndexedLine(line);
        startIndex = segment.getStartIndex();
        endIndex = segment.getEndIndex();

        Coordinate startCoord = null;

        if (start)	{
            startCoord = segment.extractPoint(startIndex);
            currentIndex = startIndex;
            moveRate = basemoveRate; // ensure we move forward along segment
        } else	{
            startCoord = segment.extractPoint(endIndex);
            currentIndex = endIndex;
            moveRate = -basemoveRate; // ensure we move backward along segment
        }

        moveTo(startCoord);
    }

    // move the agent to the given coordinates
    public void moveTo(Coordinate c)    {
        pointMoveTo.setCoordinate(c);
        location.getGeometry().apply(pointMoveTo);
        getGeometry().geometry.geometryChanged();
    }

    public void step(SimState state)    {
    	NorfolkCSVTEST campState = (NorfolkCSVTEST) state;
        move(campState);
        // campState.agents.setGeometryLocation(getGeometry(), pointMoveTo);
    }

    /**
     * moves the agent along the grid
     * @param geoTest handle on the base SimState
     * The agent will randomly select an adjacent junction and then move along
     * the line segment to it. Then it will repeat.
     */
    private void move(NorfolkCSVTEST geoTest)    {
        // if we're not at a junction move along the current segment
        if (!arrived())	{
            moveAlongPath();
        } else	{
            findNewPath(geoTest);
        }
    }

    // move agent along current line segment 
    private void moveAlongPath()    {
        currentIndex += moveRate;

        // Truncate movement to end of line segment
        if (moveRate < 0)	{ // moving from endIndex to startIndex
            if (currentIndex < startIndex)
            {
                currentIndex = startIndex;
            }
        } else	{ // moving from startIndex to endIndex
            if (currentIndex > endIndex)
            {
                currentIndex = endIndex;
            }
        }

        Coordinate currentPos = segment.extractPoint(currentIndex);

        moveTo(currentPos);
    }
}