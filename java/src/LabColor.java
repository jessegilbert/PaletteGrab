import java.awt.Color;


public class LabColor
{
	private float L;
	private float a;
	private float b;
	
	
	// constants for color comparison
	private static double WHT_L = 1.;
	private static double WHT_C = 1.045;
	private static double WHT_H = 1.015;

	// Observer= 2¡, Illuminant= D65
	private static float ref_X = 95.047f;
	private static float ref_Y = 100.0f;
	private static float ref_Z = 108.883f;
	
	// comparison modes
	private static final int CIE94 = 0;
	private static final int CIEDE2000 = 1;
	
	// CIE94 seems good enough
	public static int DIST_MODE = CIE94;
	
	
	public LabColor( float L, float a, float b )
	{
		this.L = L;
		this.a = a;
		this.b = b;
	}
	
	public LabColor( float[] Lab ) throws Exception
	{
		if( Lab.length == 3 )
		{
			this.L = Lab[0];
			this.a = Lab[1];
			this.b = Lab[2];
		}
		else
			throw new Exception("in LabColor(): initializing array of length " + Lab.length + ", should be 3" );
	}

	public float getL()
	{
		return L;
	}

	public float getA()
	{
		return a;
	}

	public float getB()
	{
		return b;
	}
	
	
	//  Lab to Java RGB color
	public static Color asColor( LabColor c )
	{
		float[] xyz = LABtoXYZ( c.getL(), c.getA(), c.getB() );
		int[] rgb = XYZtoRGB( xyz[0], xyz[1], xyz[2] );
		
		return new Color( rgb[0], rgb[1], rgb[2] );
	}
	
	
	private static float XYZrectify( float val )
	{
		double result;

		if( Math.pow(val,3.) > 0.008856 ) 
			result =  Math.pow(val,3);
		else
			result = ( val - 16f / 116f ) / 7.787;

		return (float)result;
	}
	
	
	//  LAB to XYZ color space
	private static float[] LABtoXYZ( float L, float a, float b )
	{
		// initial calculations
		float var_Y = ( L + 16f ) / 116f;
		float var_X = a / 500f + var_Y;
		float var_Z = var_Y - b / 200f;

		// rectify all
		var_Y = XYZrectify( var_Y );
		var_X = XYZrectify( var_X );
		var_Z = XYZrectify( var_Z );

		// multiply
		float X = ref_X * var_X;
		float Y = ref_Y * var_Y;
		float Z = ref_Z * var_Z;
		
		return new float[] { X, Y, Z};
	}

	
	private static float RGBrectify( float val )
	{
		double result;

		if( val > 0.0031308 )
			result =  1.055 * ( Math.pow( val, 1./2.4 ) ) - 0.055;
		else
			result = 12.92 * val;
		
		return (float)result;
	}

	
	//  XYZ to RGB color space
	private static int[] XYZtoRGB( float X, float Y, float Z )
	{
		// individual variables for XYZ channels
		float var_X = X / 100f;
		float var_Y = Y / 100f;
		float var_Z = Z / 100f;

		// derive raw values
		float var_R = var_X *  3.2406f + var_Y * -1.5372f + var_Z * -0.4986f;
		float var_G = var_X * -0.9689f + var_Y *  1.8758f + var_Z *  0.0415f;
		float var_B = var_X *  0.0557f + var_Y * -0.2040f + var_Z *  1.0570f;

		// rectify all
		var_R = RGBrectify( var_R );	
		var_G = RGBrectify( var_G );	
		var_B = RGBrectify( var_B );	

		int r = (int)(var_R * 255f);
		int g = (int)(var_G * 255f);
		int b = (int)(var_B * 255f);
		
		return new int[] { r, g, b };
	}

	
	// compute color distance between two input colors
	public static double colorDist( LabColor c1, LabColor c2 )
	{
		switch( DIST_MODE )
		{
			case CIE94:
				return CIE94( c1, c2 );
			case CIEDE2000:
				return CIEDE2000( c1, c2 );
			default:
				return 0;
		}
	}
	
	
	// compute color distance using CIE 2000 color standard revision
	private static double CIEDE2000( LabColor c1, LabColor c2 )
	{
		
		// DELTA E 2000 from http://www.easyrgb.com/index.php?X=DELT

		double xC1 = Math.sqrt( Math.pow(c1.getA(),2) + Math.pow(c1.getB(),2) );				
		double xC2 = Math.sqrt( Math.pow(c2.getA(),2) + Math.pow(c2.getB(),2) );
		
		double xCX = ( xC1 + xC2 ) / 2;
		double xGX = 0.5 * ( 1 - Math.sqrt( Math.pow(xCX,7) / ( Math.pow(xCX,7) + Math.pow(25,7) ) ) );
				
		double xNN = ( 1 + xGX ) * c1.getA();
		xC1 = Math.sqrt( Math.pow(xNN,2) + Math.pow(c1.getB(),2) );
		
		double xH1 = CieLab2Hue( xNN, c1.getB() );
				
		xNN = (1 + xGX ) * c2.getA();
		
		xC1 = Math.sqrt( Math.pow(xNN,2) + Math.pow(c2.getB(),2) );
		
		double xH2 = CieLab2Hue( xNN, c2.getB() );
		double xDL = c2.getL() - c1.getL();		
		double xDC = xC2 - xC1;
		
		
		double xDH;
		
		if( (xC1 * xC2) == 0 )
		{
			xDH = 0;
		}
		else
		{
			xNN = roundh( xH2 - xH1, 12 );
			if( Math.abs( xNN ) <= 180 )
			{
				xDH = xH2 - xH1;
			}
			else
			{
				if( xNN > 180) 	xDH = xH2 - xH1 - 360;
				else			xDH = xH2 - xH1 + 360;
			}
		}

		xDH = 2 * Math.sqrt( xC1 * xC2 ) * Math.sin( Math.toRadians(xDH/2) );
		
		double xLX = ( c1.getL() + c2.getL() ) / 2;		
		double xCY = ( xC1 + xC2 ) / 2;
		
		double xHX;		
		if( (xC1 * xC2) == 0 )
		{
			xHX = xH1 + xH2;
		}		
		else
		{
			xNN = Math.abs( roundh( xH1 - xH2, 12 ) );
			if( xNN > 180 )
			{
				if( (xH2 + xH1) < 360 ) xHX = xH1 + xH2 + 360;
				else					xHX = xH1 + xH2 - 360;
			}
			else
			{
				xHX = xH2 + xH2;
			}
			xHX /= 2;
		}
		
		double xTX = 1 - 0.17 	* Math.cos( Math.toRadians( xHX - 30 ) ) + 0.24
								* Math.cos( Math.toRadians( 2 * xHX ) ) + 0.32
								* Math.cos( Math.toRadians( 3 * xHX+ 6 ) ) - 0.20
								* Math.cos( Math.toRadians( 4 * xHX - 63 ) );
		
		double xPH = 30 * Math.exp( - Math.pow( (xHX-275) / 25, 2 ) );				//  TODO my interpretation, not sure if it's right		
		double xRC = 2 * Math.sqrt( Math.pow(xCY,7) / ( Math.pow(xCY,7) + Math.pow(25,7) ) );

		double xSL = 1 + ( ( 0.015 * ( ( xLX - 50 ) * ( xLX - 50 ) ) )
				/ Math.sqrt( 20 + ( ( xLX - 50 ) * ( xLX - 50 ) ) ) );
		
		double xSC = 1 + 0.045 * xCY;		
		double xSH = 1 + 0.015 * xCY * xTX;		
		double xRT = - Math.sin( Math.toRadians( 2 * xPH ) ) * xRC;
		
		xDL = xDL / ( WHT_L * xSL );
		xDC = xDC / ( WHT_C * xSC );
		xDH = xDH / ( WHT_H * xSH );
		
		return Math.sqrt( Math.pow(xDL,2) + Math.pow(xDC,2) + Math.pow(xDH,2) + xRT * xDC * xDH );	
	}
		
	
	private static double roundh( double d, int place )
	{
		double mask = Math.pow(10,place); 
		d *= mask;

		double remainder = d - Math.floor( d );
		
		if( remainder < 0.5 )
		{
			d = Math.floor( d );
			d /= mask;
		}
		else	// >= 0.5
		{
			d = Math.ceil( d );
			d /= mask;
		}
	
		return d;
	}
	

	// function returns CIE-H¡ value
	private static double CieLab2Hue( double var_a, float var_b )
	{
		double var_bias = 0;
		if( var_a >= 0 &&	var_b == 0 ) return 0;
		if( var_a <  0 &&	var_b == 0 ) return 180;
		if( var_a == 0 &&	var_b >  0 ) return 90;
		if( var_a == 0 &&	var_b <  0 ) return 270;
		if( var_a >  0 && 	var_b >  0 ) var_bias = 0;
		if( var_a <  0                 ) var_bias = 180;
		if( var_a >  0 && 	var_b <  0 ) var_bias = 360;
	
		return Math.toDegrees( Math.atan(var_b/var_a) ) + var_bias;
	}

	
	// compute color distance using CIE 1994 color standard revision
	private static double CIE94( LabColor c1, LabColor c2 )
	{
		// DELTA 1994 from http://www.easyrgb.com/index.php?X=DELT
		
		double xC1 = Math.sqrt( Math.pow(c1.getA(),2) + Math.pow(c1.getB(),2) );
		double xC2 = Math.sqrt( Math.pow(c2.getA(),2) + Math.pow(c2.getB(),2) );
		double xDL = c2.getL() - c1.getL();
		double xDC = xC2 - xC1;
		
		double xDE = Math.sqrt( 
				( c1.getL() - c2.getL() ) * ( c1.getL() - c2.getL() ) +
				( c1.getA() - c2.getA() ) * ( c1.getA() - c2.getA() ) +
				( c1.getB() - c2.getB() ) * ( c1.getB() - c2.getB() )
		);

		double xDH = 0;
		if( Math.sqrt(xDE) > ( Math.sqrt(Math.abs(xDL)) + Math.sqrt(Math.abs(xDC)) ) )
		{
			xDH = Math.sqrt( (xDE*xDE) - (xDL*xDL) - (xDC*xDC) );
		}
		
		double xSC = 1 + ( 0.034 * xC1 );
		double xSH = 1 + ( 0.015 * xC1 );
		
		xDL /= WHT_L;
		xDC /= WHT_C * xSC;
		xDH /= WHT_H * xSH;
		
		double delta = Math.sqrt( Math.pow(xDL,2) + Math.pow(xDC,2) + Math.pow(xDH,2) );
		return delta;
	}
	
	
	public boolean equals( Object o )
	{
		boolean result = false;

		if( o instanceof LabColor )
		{
			LabColor lc = (LabColor)o;

			if( lc.getL() == this.getL() && lc.getA() == this.getA() && lc.getB() == this.getB() )
				result = true;
		}
		
		return result;
	}
	
	
	public static double colorDist( JColor c1, JColor c2 )
	{
		return colorDist( c1.asLabColor(), c2.asLabColor() );
	}
	
	
	public static double colorDist( JColor c1, LabColor c2 )
	{
		return colorDist( c1.asLabColor(), c2 );
	}
	
	public static double colorDist( LabColor c1, JColor c2 )
	{
		return colorDist( c1, c2.asLabColor() );
	}
	
}
