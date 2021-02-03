#include <TFT.h>  // Arduino LCD library
#include <SPI.h>

// pin definition for the Leonardo
 #define cs   7
 #define dc   0
 #define rst  1

TFT TFTscreen = TFT(cs, dc, rst);

void setup() {
  // initialize the serial port
  Serial.begin(9600);
  // initialize the display
  TFTscreen.begin();
  // clear the screen with a pretty color
  TFTscreen.background(255, 255, 255);
  TFTscreen.stroke(250, 180, 10);
  TFTscreen.line(0, 0, TFTscreen.width(), TFTscreen.height());
  TFTscreen.stroke(0, 255, 0);
  TFTscreen.rect(60, 10, 40, 20);
  TFTscreen.stroke(0, 0, 255);
  TFTscreen.fill(0, 0, 255);
  TFTscreen.circle(50,80,25);
}

void loop() {  }