import java.awt.Color;
import java.util.ArrayList;
import java.util.ListIterator;

// Class JColor wraps an ArrayList of LabColor objects that have been determined to be close enough to count as a single color.
// The size of the ArrayList is used as a measure of how prominent a given color is in the input image, for weighting purposes.
public class JColor implements Comparable<JColor>
{	
//	public static float H_TOLERANCE = 0.02f;
//	public static float S_TOLERANCE = 0.02f;
//	public static float B_TOLERANCE = 0.02f;

	public static double TOLERANCE = 15.;

	
	// internal storage for LabColors
	private ArrayList<LabColor> list;

	// average LabColor value
	private LabColor color;

	
	
	public JColor( LabColor c )
	{
		list = new ArrayList<LabColor>();
		add( c );
	}	
	
	
	public void add( LabColor c )
	{
		// store LabColor value
		list.add( c );
		
		// calculate average value for all stored LabColor objects
		calc();
	}
	
	
	
	// create an average LabColor value that repreents mean of all LabColor objects stored in internal ArrayList
	private void calc()
	{
		float LL = 0;
		float AA = 0;
		float BB = 0;

		ListIterator<LabColor> li = list.listIterator();
		while( li.hasNext() )
		{
			LabColor c = (LabColor) li.next();

			LL += c.getL();
			AA += c.getA();
			BB += c.getB();
		}
		
		// calculate avg values
		float elems = (float)list.size();
		float L = LL / elems;
		float a = AA / elems;
		float b = BB / elems;

		color = new LabColor( L, a, b );
	}
	
	

	// critical method that determines if the color distance between two input colors falls within a given tolerance range
	public boolean nearby( LabColor c )
	{		
		return LabColor.colorDist( color, c ) < TOLERANCE;
	}
	
	
	
	// utility method so that JColor ArrayList can be sorted
	public int compareTo( JColor o )
	{
		if( this.count() > o.count() )
		{
			return -1;
		}
		else if ( this.count() == o.count() )
		{
			return 0;
		}
		else
		{
			return 1;
		}
	}

	
	public int count()
	{
		return list.size();
	}
	
	
	public Color asColor()
	{
		return LabColor.asColor( color );
	}
	
	
	public LabColor asLabColor()
	{
		return color;
	}
	
	
	public String toString()
	{
		return "JColor( " + color.getL() + "," + color.getA() + "," + color.getB() + " )";
	}
		
}
