from fr.inria.zvtm.engine import *
from fr.inria.zvtm.glyphs import *
from fr.inria.zvtm.clustering import *

from org.jgroups import JChannel;
from org.jgroups import Message;

from java.awt import Color;

from java.io import File;

if __name__ == "__main__":	
	spaceName = "testSpace"
	vsm = VirtualSpaceManager.INSTANCE;
	vs = vsm.addVirtualSpace(spaceName);
	print('Welcome to the interactive ZVTM shell (jython based)')
	print('VirtualSpaceManager singleton is named vsm')
	print('Default VirtualSpace is named vs')

