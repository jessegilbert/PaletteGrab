
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

import com.cycling74.jitter.JitterMatrix;
import com.cycling74.max.Atom;
import com.cycling74.max.MaxObject;


public class PaletteGrab extends MaxObject
{
	private static boolean DEBUG = true;
	private static int NUM_COLORS = 8;

	private ArrayList<JColor> colors;
	
	private static final int RGB = 0;
	private static final int LAB = 1;
	
	private static int MODE = LAB;
	
	
	
	public PaletteGrab()
	{
		colors = new ArrayList<JColor>();
		declareIO( 1, 2 );
		createInfoOutlet( false );
	}

	
	public void tol( float val )
	{
		JColor.TOLERANCE = val;
	}
	
	
	// input image is processed to reveal dominant colors, with two results:
	// outlet 0: 8-color matrix with LAB values
	// outlet 1: histogram showing the relative weights of each color
	public void jit_matrix( String ptr )
	{
		if( DEBUG ) printLine( "===========================================================" );
		
		
		// clear the internal ArrayList of colors
		colors.clear();
		
		// make a copy of the matrix in memory
		JitterMatrix mx = new JitterMatrix( ptr );
		
		// method1 is a lazy attractor algorithm
		method1( mx );
		
		// final sort and then print them out
		Collections.sort( colors );

		
		int index = 0;
		
		
		// we are going to output a matrix for conversion from LAB to RGB via shaders
		JitterMatrix outMx = null;
		JitterMatrix histoMx = null;
		
		ListIterator<JColor> it = colors.listIterator();
		while (it.hasNext() )
		{			
			if( index < NUM_COLORS )
			{
				JColor jc = (JColor)it.next();
				printLine( index + ": " + jc.toString() + " " + jc.count() );

				if( MODE == RGB )
				{
					if( outMx == null ) 
						outMx = new JitterMatrix( 4, "char", NUM_COLORS, 1 );
						
					Color c = jc.asColor();
					
					int[] argb = new int[4];

					argb[0] = 255;			// placeholder alpha
					argb[1] = c.getRed();
					argb[2] = c.getGreen();
					argb[3] = c.getBlue();
					
					outMx.setcell1d( index, argb );					
				}
				else if( MODE == LAB )
				{
					if( outMx == null ) 
					{
						outMx = new JitterMatrix( 4, "float32", NUM_COLORS, 1 );
						histoMx = new JitterMatrix( 1, "long", NUM_COLORS, 1 );
					}
					
					LabColor lc = jc.asLabColor();
					
					float[] Labx = new float[4];
					
					Labx[1] = lc.getL();
					Labx[2] = lc.getA();
					Labx[3] = lc.getB();
					Labx[0] = 0f;
					
					outMx.setcell1d( index, Labx );
					histoMx.setcell1d( index, new int[] {jc.count()} );
				}
				else
				{
					printLine("ERROR: MODE is set to illegal value:" + MODE );
					break;
				}
			}
			else
				break;
			
			index++;
		}
		
		outlet( 0, "jit_matrix", outMx.getName() );		
		outlet( 1, "jit_matrix", histoMx.getName() );
	}


	private void method1( JitterMatrix mx )
	{
		// get the matrix dim
		int[] dim = mx.getDim();
		
		// for every cell
		for( int i = 0; i < dim[0]; i++ )
		{
			for( int j = 0; j < dim[1]; j++ )
			{
				// read a cell from the input matrix
				Atom[] cell = mx.getcell2d( i, j );
				
				// translate to a Java Color object
				LabColor cellColor = atomsToColor(cell);
				
				// flag for finding matches
				boolean foundMatch = false;
				
				if( cellColor != null )
				{
					// for every JColor already stored
					ListIterator<JColor> it = colors.listIterator();
					while (it.hasNext() )
					{
						JColor c = (JColor)it.next();
						
						// evaluate according to tolerances
						if( c.nearby( cellColor ) )
						{
							c.add( cellColor );
							foundMatch = true;
							break;
						}
					}
					
					// if we found no matches, make a new JColor
					if( !foundMatch )
					{
						colors.add( new JColor( cellColor ) );
					}
					
					// sort by count --  larger JColor groups get first comparisons
					Collections.sort( colors );
				}
			}
		}
	}

	
	public void mode( int x )
	{
		MODE = x;
	}
	
	
	public void distMode( int x )
	{
		LabColor.DIST_MODE = x;
	}
	
	
	// cause debug information to be printed to the Max window
	public void debug( int flag )
	{
		DEBUG = (flag != 0);
	}
	
	
	public void notifyDeleted()
	{

	}
	

	private static LabColor atomsToColor( Atom[] a )
	{
		if( a.length == 4 )
		{
			return new LabColor( a[1].getFloat(), a[2].getFloat(), a[3].getFloat() );
		}
		else
		{
			if( DEBUG ) printLine( "ERROR in atomsToColor(): incoming Atom array is length " + a.length + ", expected 4" );
			return null;
		}
	}
	
	
	public static void printLine( String x )
	{
		//System.out.println( x );
		post( x );
	}
}
