package sim.app.geo.norfolk_csvTEST;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import sim.app.geo.campusworld_norfolk.CampusworldNorfolk;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileExporter;
import sim.io.geo.ShapeFileImporter;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.MasonGeometry;
import au.com.bytecode.opencsv.CSVReader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;

/**
 * A simple model of Norfolk's political boundary (LSOA) and Road Network
 * with a bunch of agents moving around randomly.
 * 
 * @author KJGarbutt
 *
 */
public class NorfolkCSVTEST extends SimState	{
	/////////////// Model Parameters ///////////////////////////////////
    private static final long serialVersionUID = -4554882816749973618L;

    public static final int WIDTH = 500; 
    public static final int HEIGHT = 500; 
    
    /////////////// Objects //////////////////////////////////////////////
	//public int numAgents = 500;
	//public int numNGOAgents = 500;
	//public int numElderlyAgents = 500;
	//public int numLimActAgents = 500;

	/////////////// Containers ///////////////////////////////////////
    public GeomVectorField roads = new GeomVectorField(WIDTH, HEIGHT);
    //public GeomVectorField floodedroads = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField lsoa = new GeomVectorField(WIDTH,HEIGHT);
    public GeomVectorField flood = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField agents = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField ngoagents = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField elderlyagents = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField limactagents = new GeomVectorField(WIDTH, HEIGHT);

    // Stores the road network connections
    public GeomPlanarGraph network = new GeomPlanarGraph();
    public GeomVectorField junctions = new GeomVectorField(WIDTH, HEIGHT); // nodes for intersections

	///////////////////////////////////////////////////////////////////////////
	/////////////////////////// BEGIN functions ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////
    
    /**
	 * Default constructor function
	 * @param seed
	 */
    public NorfolkCSVTEST(long seed)	{
        super(seed);

		//////////////////////////////////////////////
		///////////// READING IN DATA ////////////////
		//////////////////////////////////////////////
        try	{
        	System.out.println("Reading LSOA layer...");
            URL lsoaGeometry = NorfolkCSVTEST.class.getResource("data/NorfolkLSOA.shp");
            ShapeFileImporter.read(lsoaGeometry, lsoa);
            Envelope MBR = lsoa.getMBR();
            MBR.expandToInclude(lsoa.getMBR());
            
            System.out.println("Reading Flood Zone layer");
            URL floodGeometry = CampusworldNorfolk.class.getResource("data/flood_zone_3_010k_NORFOLK_ONLY.shp");
            ShapeFileImporter.read(floodGeometry, flood);
            MBR.expandToInclude(flood.getMBR());
            
        	System.out.println("Reading Road Network layer...");
            URL roadGeometry = NorfolkCSVTEST.class.getResource("data/NorfolkITN.shp");
            ShapeFileImporter.read(roadGeometry, roads);
            MBR.expandToInclude(roads.getMBR());
            
            //System.out.println("Reading Flooded Road Network layer...");
            //URL floodedroadGeometry = Norfolk.class.getResource("data/norfolk_NO_FLOODED_ROAD.shp");
            //ShapeFileImporter.read(floodedroadGeometry, floodedroads);
            //MBR.expandToInclude(floodedroads.getMBR());
            
            System.out.println("Done reading data!");
            System.out.println();

            // Now synchronize the MBR for all GeomFields to ensure they cover the same area
            lsoa.setMBR(MBR);
            flood.setMBR(MBR);
            //floodedroads.setMBR(MBR);
            roads.setMBR(MBR);

            System.out.println("Creating road network...");
            network.createFromGeomField(roads);
            //network.createFromGeomField(floodedroads);

            addIntersectionNodes(network.nodeIterator(), junctions);

        } catch (FileNotFoundException ex)	{
            Logger.getLogger(NorfolkCSVTEST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	//////////////////////////////////////////////
	////////////////// AGENTS ////////////////////
	//////////////////////////////////////////////
	
	// set up the agents in the simulation
    //public int getNumAgents() { return numAgents; } 
    //public void setNumAgents(int n) { if (n > 0) numAgents = n; } 

    void addAgents()	{
    	String filename = "/Users/KJGarbutt/Desktop/areas_roads_merge1.csv";
		CSVReader reader;
		System.out.println();
		System.out.println("Adding Main agents...");
    	//for (int i = 0; i < numAgents; i++)
		try {
			reader = new CSVReader(new FileReader(filename), ',', '"', 1);
			String [] nextLine;
		    while ((nextLine = reader.readNext()) != null)	{ 
				String [] bits = nextLine;
				//String[] bits = nextLine.length(",");
		    	Integer pop = Integer.parseInt(bits[12]);
		    	for (int i = 0; i < pop; i++)	{
				
				MainAgent a = new MainAgent(this);
				System.out.println("Main Agent: " +nextLine[0]);
				agents.addGeometry(a.getGeometry());
					// Put it at location x, y
				schedule.scheduleRepeating(a);
					// Schedule it to repeat in the simulation
		    	}
		    }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    void addNGOAgents()	{
    	String filename2 = "/Users/KJGarbutt/Desktop/areas_roads_merge1.csv";
		CSVReader reader2;
		System.out.println();
		System.out.println("Adding NGO agents...");
    	//for (int i = 0; i < numAgents; i++)
		try {
			reader2 = new CSVReader(new FileReader(filename2), ',', '"', 1);
			String [] nextLine;
		    while ((nextLine = reader2.readNext()) != null)	{ 
				String [] bits = nextLine;
				//String[] bits = nextLine.length(",");
		    	Integer pop = Integer.parseInt(bits[12]);
		    	for (int i = 0; i < pop; i++)	{
				
				NGOAgent b = new NGOAgent(this);
				System.out.println("NGO Agent: " +nextLine[0]);
				agents.addGeometry(b.getGeometry());
					// Put it at location x, y
				schedule.scheduleRepeating(b);
					// Schedule it to repeat in the simulation
		    	}
		    }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    void addElderlyAgents()	{
    	String filename3 = "/Users/KJGarbutt/Desktop/areas_roads_merge1.csv";
		CSVReader reader3;
		System.out.println();
		System.out.println("Adding Elderly agents...");
    	//for (int i = 0; i < numAgents; i++)
		try {
			reader3 = new CSVReader(new FileReader(filename3), ',', '"', 1);
			String [] nextLine;
		    while ((nextLine = reader3.readNext()) != null)	{ 
				String [] bits = nextLine;
				//String[] bits = nextLine.length(",");
		    	Integer pop = Integer.parseInt(bits[12]);
		    	for (int i = 0; i < pop; i++)	{
				
				ElderlyAgent c = new ElderlyAgent(this);
				System.out.println("Elderly Agent: " +nextLine[0]);
				agents.addGeometry(c.getGeometry());
					// Put it at location x, y
				schedule.scheduleRepeating(c);
					// Schedule it to repeat in the simulation
		    	}
		    }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    void addLimActAgents()	{
    	String filename4 = "/Users/KJGarbutt/Desktop/areas_roads_merge1.csv";
		CSVReader reader4;
		System.out.println();
		System.out.println("Adding Limited Actions agents...");
    	//for (int i = 0; i < numAgents; i++)
		try {
			reader4 = new CSVReader(new FileReader(filename4), ',', '"', 1);
			String [] nextLine;
		    while ((nextLine = reader4.readNext()) != null)	{ 
				String [] bits = nextLine;
				//String[] bits = nextLine.length(",");
		    	Integer pop = Integer.parseInt(bits[12]);
		    	for (int i = 0; i < pop; i++)	{
				
				LimitedActionsAgent d = new LimitedActionsAgent(this);
				System.out.println("Limited Actions Agent: " +nextLine[0]);
				agents.addGeometry(d.getGeometry());
					// Put it at location x, y
				schedule.scheduleRepeating(d);
					// Schedule it to repeat in the simulation
		    	}
		    }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
		
		
    	/*
        for (int i = 0; i < numNGOAgents; i++)
        {
	        NGOAgent b = new NGOAgent(this);
	        ngoagents.addGeometry(b.getGeometry());
	        schedule.scheduleRepeating(b);
        }
        for (int i = 0; i < numElderlyAgents; i++)
        {
	        ElderlyAgent c = new ElderlyAgent(this);
	        elderlyagents.addGeometry(c.getGeometry());
	        schedule.scheduleRepeating(c);
        }
        for (int i = 0; i < numLimActAgents; i++)
        {
	        LimitedActionsAgent d = new LimitedActionsAgent(this);
	        limactagents.addGeometry(d.getGeometry());
	        schedule.scheduleRepeating(d);
        }
        */
    

    /**
	 * Finish the simulation and clean up
	 */
    public void finish()	{
    	super.finish();
    	System.out.println("Simulation ended by user.");
        /*
    	System.out.println("Attempting to export agent data...");
        try	{
        	ShapeFileExporter.write("agents", agents);
        } catch (Exception e)	{
        	System.out.println("Export failed.");
        	e.printStackTrace();
        }
        */
    }
    
    /**
	 * Set up the simulation
	 */
    @Override
    public void start()	{
        super.start();

		//////////////////////////////////////////////
		////////////////// CLEANUP ///////////////////
		//////////////////////////////////////////////
		
        agents.clear(); // clear any existing agents from previous runs
        addAgents();
        addNGOAgents();
        addElderlyAgents();
        addLimActAgents();
        System.out.println();
        System.out.println("Starting simulation...");
        
        // standardize the MBRs for each of the agent classes so that the visualization lines up
        agents.setMBR(roads.getMBR());
        ngoagents.setMBR(roads.getMBR());
        elderlyagents.setMBR(roads.getMBR());
        limactagents.setMBR(roads.getMBR());

        // Ensure that the spatial index is made aware of the new agent
        // positions. Scheduled to guaranteed to run after all agents moved.
        schedule.scheduleRepeating( agents.scheduleSpatialIndexUpdater(), Integer.MAX_VALUE, 1.0);
    }

    /** adds nodes corresponding to road intersections to GeomVectorField
     *
     * @param nodeIterator Points to first node
     * @param intersections GeomVectorField containing intersection geometry
     *
     * Nodes will belong to a planar graph populated from LineString network.
     */
    private void addIntersectionNodes(Iterator nodeIterator, GeomVectorField intersections)	{
        GeometryFactory fact = new GeometryFactory();
        Coordinate coord = null;
        Point point = null;
        int counter = 0;

        while (nodeIterator.hasNext())	{
                Node node = (Node) nodeIterator.next();
                coord = node.getCoordinate();
                point = fact.createPoint(coord);

                junctions.addGeometry(new MasonGeometry(point));
                counter++;
        }
    }
    
    /**
	 * To run the model without visualization
	 */
    public static void main(String[] args)	{
        doLoop(NorfolkCSVTEST.class, args);
        System.exit(0);
    }
}
