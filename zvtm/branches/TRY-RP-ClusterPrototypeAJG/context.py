from fr.inria.zvtm.engine import *
from fr.inria.zvtm.glyphs import *
from fr.inria.zvtm.clustering import *

from org.jgroups import JChannel;
from org.jgroups import Message;

from java.awt import Color;

from java.io import File;

def patchVSGlyphAdd():
	""" Sends a network message whenever a glyph is added to 
	a VirtualSpace"""
	oldMethod = VirtualSpace.addGlyph
	
	def newAddGlyph(self, glyph):
		oldMethod(self, glyph)
		delta = glyph.getCreateDelta()
		channel.send(Message(None,None,delta))
	
	VirtualSpace.addGlyph = newAddGlyph

#it should be possible to factor patchGlyphMove and
#patchGlyphMoveTo using higher order functions (unfortunately
#I'm not used to those)
def patchGlyphMove():
	""" Sends a network message whenever a glyph is moved (relative)
	"""
	oldMethod = Glyph.move

	def newGlyphMove(self, dx, dy):
		oldMethod(self, dx, dy)
		loc = self.getLocation()
		delta = GlyphPosDelta(self.getObjId(),
				loc.x, loc.y)
		channel.send(Message(None,None,delta))

	Glyph.move = newGlyphMove

def patchGlyphMoveTo():
	""" Sends a network message whenever a glyph is moved (absolute)
	"""
	oldMethod = Glyph.moveTo

	def newGlyphMoveTo(self, x, y):
		oldMethod(self, x, y)
		loc = self.getLocation()
		delta = GlyphPosDelta(self.getObjId(),
				loc.x, loc.y)
		channel.send(Message(None,None,delta))

	Glyph.moveTo = newGlyphMoveTo

def patchGlyphColorChange():
	""" Sends a network message whenever a glyph's color is changed. """
	oldMethod = Glyph.setColor

	def newGlyphSetColor(self, color):
			oldMethod(self, color)
			delta = GlyphColorDelta(self.getObjId(),
					self.getColor())
			channel.send(Message(None,None,delta))

	Glyph.setColor = newGlyphSetColor

def patchGlyphStrokeWidthChange():
	""" Sends a network message whenever a glyph's stroke width is changed
	"""
	oldMethod = Glyph.setStrokeWidth

	def newGlyphStrokeWidthChange(self, strokeWidth):
		oldMethod(self, strokeWidth)
		delta = GlyphStrokeWidthDelta(self.getObjId(),
				self.getStrokeWidth())
		channel.send(Message(None,None,delta))

	Glyph.setStrokeWidth = newGlyphStrokeWidthChange

def doMonkeyPatch():
	""" Patches Glyph-derived class as well as VirtualSpace so that
	network messages are sent whenever a glyph is added or removed
	into a virtual space, or a glyph property is changed.
	"""
	patchVSGlyphAdd()
	#todo patchVSGlyphRemove
	#todo patchVSGlyphRemoveAll
	patchGlyphMove()
	patchGlyphMoveTo()
	patchGlyphColorChange()
	patchGlyphStrokeWidthChange()

def init():
	doMonkeyPatch()

if __name__ == "__main__":	
	spaceName = "testSpace"
	vsm = VirtualSpaceManager.INSTANCE;
	vs = vsm.addVirtualSpace(spaceName);
	channel = JChannel(File("chan_conf.xml"))
	channel.connect(spaceName)
	init()
	print('Welcome to the interactive ZVTM shell (jython based)')
	print('VirtualSpaceManager singleton is named vsm')
	print('Default VirtualSpace is named vs')

