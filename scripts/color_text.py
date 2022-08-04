# Imports
import os
import sys
from typing import Optional

import PIL
import pypdn
from PIL.Image import Image
from numpy import asarray, ndarray

# Constants
PDN_PATH = os.path.join("..", "images", "text", "text.pdn")
PNG_PATH_OUTPUT = os.path.join("..", "images", "text", "text_script_output.png")

# Code
print("Loading PDN file...\nF> '{}'...".format(os.path.abspath(PDN_PATH)))
layeredImage = pypdn.read(PDN_PATH)
print("")

print("Analysing layers...")
palette_layer: Optional[pypdn.Layer] = None
input_layers: list[pypdn.Layer] = list()

layer: pypdn.Layer
for layer in layeredImage.layers:
    if layer.name.startswith("Script - Color Palette"):
        print("C> {}".format(layer.name))
        palette_layer = layer
    elif layer.name.startswith("Text"):
        print("I> {}".format(layer.name))
        input_layers.append(layer)
    else:
        print("*> {}".format(layer.name))
print("")

print("Checking if the required layers were found...")
if palette_layer is None:
    print("!> The palette layer is missing !")
    sys.exit(1)
if len(input_layers) <= 0:
    print("!> No input layers were found !")
    sys.exit(2)
print("!> Everything seems to be good !")
print("")

print("Grabbing the flattened layers...")
layer: pypdn.Layer
for layer in layeredImage.layers:
    layer.visible = False
palette_layer.visible = True
colors_image: ndarray = layeredImage.flatten(asByte=True)
palette_layer.visible = False
for input_layer in input_layers:
    input_layer.visible = True
input_image: ndarray = layeredImage.flatten(asByte=True)
for input_layer in input_layers:
    input_layer.visible = False
print("!> Done !")
print("")

print("Grabbing the colors...")
colors_data = asarray(colors_image)
color_light = tuple(colors_data[0][16])
color_middle = tuple(colors_data[0][32])
color_dark = tuple(colors_data[0][48])
del colors_data
print("!> Done !")
print("")

print("Preparing the output image...")
image_output = PIL.Image.new(mode="RGBA", size=(layeredImage.width, layeredImage.height), color=(0, 0, 0, 0))
print("!> Done !")
print("")

print("Coloring the flattened input image...")
input_data = asarray(input_image)
for y in range(layeredImage.height):
    if y == 0 or y == layeredImage.height - 1:
        continue
    
    for x in range(layeredImage.width):
        if x == 0 or x == layeredImage.width - 1:
            continue
        
        if input_image[y][x].tolist()[3] == 0:
            continue
        
        is_free_up: bool = input_image[y - 1][x].tolist()[3] == 0
        is_free_down: bool = input_image[y + 1][x].tolist()[3] == 0
        is_free_left: bool = input_image[y][x - 1].tolist()[3] == 0
        is_free_right: bool = input_image[y][x + 1].tolist()[3] == 0
        
        pixel_border_map = (
                               (0b1000 if is_free_up else 0b0000) | (0b100 if is_free_down else 0b0000) |
                               (0b10 if is_free_left else 0b0000) | (0b1 if is_free_right else 0b0000)
        ) & 0b1111
        
        pixel_color = color_middle

        match pixel_border_map:
            case 0b0000:
                pass
            case 0b0001:
                pixel_color = color_dark
            case 0b0010:
                pixel_color = color_light
            case 0b0011:
                pixel_color = color_light
            
            case 0b0100:
                pixel_color = color_dark
            case 0b0101:
                pixel_color = color_dark
            case 0b0110:
                # pixel_color = color_light
                pixel_color = color_dark
            case 0b0111:
                # pixel_color = color_light
                pixel_color = color_dark
            
            case _:
                # Covers any case where b3 is 1 -> 0b1xxx
                pixel_color = color_light
        
        image_output.putpixel((x, y), pixel_color)
del input_data
print("!> Done !")
print("")

print("Saving the output image...")
image_output.save(PNG_PATH_OUTPUT)
print("!> Done !")
print("")
