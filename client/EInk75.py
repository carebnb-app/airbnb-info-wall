import sys
import os
import logging
import timeit
import traceback

logging.basicConfig(level=logging.DEBUG)

libdir = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'lib')
assetsdir = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'assets')

if os.path.exists(libdir):
  sys.path.append(libdir)

from PIL import Image,ImageDraw,ImageFont
from waveshare_epd import epd7in5_V2

class EInk75:

  def __init__(self):
    logging.info("Initializing EInk75")

    fontPath = assetsdir + '/Font.ttc'
    print(fontPath)
    self.fonts = {
      14: ImageFont.truetype(fontPath, 14),
      18: ImageFont.truetype(fontPath, 18),
      24: ImageFont.truetype(fontPath, 24),
      60: ImageFont.truetype(fontPath, 60),
      120: ImageFont.truetype(fontPath, 120)
    }

    logging.info("Instantiating object")
    self.epd = epd7in5_V2.EPD()
    logging.info("Calling init method")
    self.epd.init()

  def clear(self):
    logging.info("Clearing EInk75")
    self.epd.clear()

  def createCanvas(self, color):
    logging.info("Creating canvas %i x %i", self.epd.width, self.epd.height)
    self.canvasImage = Image.new('1', (self.epd.width, self.epd.height), color)
    self.canvas = ImageDraw.Draw(self.canvasImage)

  def drawLine(self, ini=(0,0), end=(0,0), color=0):
    coords = ini + end
    logging.info("Drawing line coords: %s" % (coords,))
    self.canvas.line(coords, fill=color)

  def drawCircle(self, center=(0,0), diameter=0, color=0):
    radius = diameter/2
    x1 = center[0] - radius
    y1 = center[1] - radius
    x2 = center[0] + radius
    y2 = center[1] + radius
    coords = (x1, y1, x2, y2)
    logging.info("Drawing circle coords: %s", (coords,))
    self.canvas.chord(coords, 0, 360, fill=color)

  def drawSquare(self, pos=(0,0), size=(0,0), color=0):
    x1 = pos[0]
    y1 = pos[1]
    x2 = x1 + size[0]
    y2 = y1 + size[1]
    coords = (x1, y1, x2, y2)
    self.canvas.rectangle(coords, fill=color)

  def write(self, pos=(0,0), text='', size=0, color=0):
    self.canvas.text(pos, text + '  ', font=self.fonts[size], fill=color)

  def displayCanvas(self):
    self.epd.display(self.epd.getbuffer(self.canvasImage))
