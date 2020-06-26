#!/usr/bin/python
# -*- coding:utf-8 -*-

import json
import requests
from consts import BLACK,WHITE
from EInk75 import EInk75

# Init
screen = EInk75()
screen.createCanvas(WHITE)

# Request json
jsonRequest = requests.get("https://raw.githubusercontent.com/carebnb-app/airbnb-listing-wall-server/master/samples/welcome.json")
jsonContent = jsonRequest.text
print(jsonContent)

# Parse Json
jsonObj = json.loads(jsonContent)

for obj in jsonObj['objs']:
  if(obj['type'] == 'debug'):
    print(obj['value'])
  if(obj['type'] == 'line'):
    screen.drawLine(ini=(obj['ini']['x'], obj['ini']['y']), end=(obj['end']['x'], obj['end']['y']), color=obj['color'])
  if(obj['type'] == 'circle'):
    screen.drawCircle(center=(obj['center']['x'], obj['center']['y']), diameter=obj['diameter'], color=obj['color'])
  if(obj['type'] == 'text'):
    screen.write(pos=(obj['pos']['x'], obj['pos']['y']), text=obj['text'], size=obj['size'], color=obj['color'])

screen.displayCanvas()
