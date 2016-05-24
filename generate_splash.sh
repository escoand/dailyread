#!/bin/bash

INPUT=$1
FLAVOR=$2
OUTPUT=img_splash.jpg

mkdir -p app/src/$FLAVOR/res/mipmap-{m,h,xh,xxh,xxxh}dpi/

convert $INPUT -strip -thumbnail 320x480 app/src/$FLAVOR/res/mipmap-mdpi/$OUTPUT
convert $INPUT -strip -thumbnail 480x800 app/src/$FLAVOR/res/mipmap-hdpi/$OUTPUT
convert $INPUT -strip -thumbnail 720x1280 app/src/$FLAVOR/res/mipmap-xhdpi/$OUTPUT
convert $INPUT -strip -thumbnail 960x1600 app/src/$FLAVOR/res/mipmap-xxhdpi/$OUTPUT
convert $INPUT -strip -thumbnail 1280x1920 app/src/$FLAVOR/res/mipmap-xxxhdpi/$OUTPUT