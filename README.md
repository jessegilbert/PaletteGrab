# PaletteGrab
Max patch/Java classes that analyze an input image and derive its color palette.

All code by Jesse Gilbert, 2015.


PaletteGrab uses custom Java classes to analyze an arbitrary input image and generate an 8-color palette that represents its dominant colors.
The analysis/sorting algorithm requires an input matrix in CIE-Lab format.  In order to achieve this, use my colorspace transformation shaders available here:

http://jessegilbert.net/release/shaders/


Several of these shaders are used in the example Max patch, so they must be visible in your Max search path.


The Java sorting algorithm has a variable tolerance setting, which enables the user to select a maximum color "distance" used to group colors together.
For input images with subtle color shading a lower tolerance value may be desired, resulting in a more nuanced output palette.  Higher tolerance
values may be useful to ensure that all colors are represented in the output matrix.  Experimentation may be required to find the appropriate
tolerance value for a given input image.

The input image is downsampled to reduce processing time -  the included Max patch provides a umenu to adjust this as needed.

All feedback welcome!
