#include <FastLED.h>

#define AUDIO A0
#define LED_PIN 6
#define NUM_LEDS 180
#define BRIGHTNESS  255
#define LED_PER_HEX 30
#define NUM_HEX 6
#define LED_TYPE    WS2812B
#define COLOR_ORDER GRB

CRGB leds[NUM_LEDS];

void setHexSolid(int hex, CRGB color, int brightness, int index);
void setHexPalette(CRGBPalette16 palette, int index);
void setHexBreathe(int hex, CRGB color, uint16_t level);
void setHexAudio(CRGBPalette16 palette, int  index);
void createChasePalette(int r, int g, int b);
void FillLEDsFromPaletteColors(uint8_t colorIndex);

extern const TProgmemPalette16 BluePalette_p PROGMEM;
extern const TProgmemPalette16 PurplePalette_p PROGMEM;
extern const TProgmemPalette16 ISUPalette_p PROGMEM;
CRGBPalette16 Chase_p;

uint8_t state = 0;
int react = 0;
int pre_react = 0;
int counter = 0;
byte msg[5];
byte data;
int breathing = 0;
int mode = 0;
unsigned long dataReceived;
int seed = random8();
boolean newData = false;
const byte numChars = 5;

void setup() {
  Serial.begin(9600);
  Serial1.begin(9600);

  // LED LIGHTING SETUP
  delay( 3000 ); // power-up safety delay
  FastLED.addLeds<LED_TYPE, LED_PIN, COLOR_ORDER>(leds, NUM_LEDS).setCorrection( TypicalLEDStrip );
  FastLED.setBrightness(  BRIGHTNESS );

  FastLED.clear();
  FastLED.show();
  randomSeed(analogRead(1));

  pinMode(AUDIO, INPUT);
}

void loop() {
  static unsigned int startIndex = 0;
  static bool breathing = false;
  counter = counter % 4;
  if (Serial1.available()) {
    dataReceived = millis();
    msg[counter] = Serial1.read();
//    Serial.print(counter);
//    Serial.print(" ");
//    Serial.println(msg[counter]);
    counter++;
  }
  if (counter == 4) {
    mode = msg[3];
  }
  unsigned long t = millis();
  if (t - dataReceived > 300) {
    counter = 0;
    dataReceived = t;
  }
  if (0 <= msg[3] && msg[3] < 6) {
    setHexSolid(msg[3], CRGB(msg[0], msg[1], msg[2]), BRIGHTNESS, 0);
    msg[3] = -1;
  }
  if (mode == 7) { //Off
    //Serial.println("Off");
    FastLED.clear();
  }
  else if (mode == 8) { //Rainbow 1
    //Serial.println("Rainbow 1");
    for (int i = 0; i < NUM_HEX; i++) {
      setHexSolid(i, ColorFromPalette(RainbowColors_p, startIndex + i * 13, BRIGHTNESS, LINEARBLEND), BRIGHTNESS * (msg[1] / 100.0), 0);
    }
  } else if (mode == 9) { //Rainbow 2
    //Serial.println("Rainbow 2");
    if (startIndex % 512 != 0) {
      setHexBreathe(seed % 6, ColorFromPalette(RainbowColors_p, startIndex / 256 + 32, BRIGHTNESS, LINEARBLEND), startIndex % 512);
    } else {
      FastLED.clear();
      seed = random8();
    }
  } else if (mode == 10) { //Rainbow 3
    //Serial.println("Rainbow 3");
    setHexPalette(RainbowColors_p, BRIGHTNESS * (msg[1] / 100.0), startIndex);
  } else if (mode == 11) { //Blue Palette
    //Serial.println("Blue Palette");
    setHexPalette(BluePalette_p, BRIGHTNESS * (msg[1] / 100.0), startIndex);
  } else if (mode == 12) { //Purple Palette
    //Serial.println("Purple Palette");
    setHexPalette(PurplePalette_p, BRIGHTNESS * (msg[1] / 100.0), startIndex);
  } else if (mode == 13) { //Chase
    //Serial.println("Chase White");
    createChasePalette(msg[0], msg[1], msg[2]);
    FillLEDsFromPaletteColors(startIndex / 2);
  } else if (mode == 14) { //Audio
    //Serial.println("Audio");
    setHexAudio(RainbowColors_p, startIndex);
  }
  FastLED.show();
  startIndex ++;
}

void setHexBreathe(int hex, CRGB color, uint16_t level) {
  if (level >= 256) {
    setHexSolid(hex, color, 511 - level, 0);
    //Serial.println(511 - level);
  } else {
    setHexSolid(hex, color, level, 0);
    //Serial.println(level);
  }
}

void setHexSolid(int hex, CRGB color, int brightness, int index) {
  CRGBPalette16 currentPalette;
  for (int i = 0; i < 16; i++) {
    currentPalette[i] = color;
  }
  for (int i = 0; i < LED_PER_HEX; i++) {
    leds[i + LED_PER_HEX * hex] = ColorFromPalette(currentPalette, index, brightness, LINEARBLEND);
  }
}

void setHexPalette(CRGBPalette16 palette, int brightness, int index) {
  for (int j = 0; j < NUM_HEX; j++) {
    for (int i = 0; i < LED_PER_HEX; i++) {
      leds[i + LED_PER_HEX * j] = ColorFromPalette(palette, i + index, brightness, LINEARBLEND);
      index += 3;
    }
  }
}

void setHexAudio(CRGBPalette16 palette, int startIndex) {

  uint16_t audio = analogRead(AUDIO);
  if (audio > 0) {
    pre_react = ((long)NUM_LEDS * (long)audio * 4) / 511L; // TRANSLATE AUDIO LEVEL TO NUMBER OF LEDs
    if (pre_react > react) // ONLY ADJUST LEVEL OF LED IF LEVEL HIGHER THAN CURRENT LEVEL
      react = pre_react;

    //Serial.print(" -> ");
    //Serial.println(pre_react);
  }
  if(react > 255) {
    react = 255;
  }
  Serial.println(react);
  for (int i = 0; i < NUM_HEX; i++) {
    setHexSolid(i, ColorFromPalette(RainbowColors_p, startIndex + i * 13, BRIGHTNESS, LINEARBLEND), react, 0);
  }
  if (react > 3){
    react -= 3;
  }
}

void FillLEDsFromPaletteColors(uint8_t colorIndex) {
  for ( int i = 0; i < NUM_LEDS; i++) {
    leds[i] = ColorFromPalette(Chase_p, colorIndex, BRIGHTNESS, LINEARBLEND);
    colorIndex ++;
  }
}

const TProgmemPalette16 BluePalette_p PROGMEM =
{
  CRGB::Blue,
  CRGB::Blue,
  CRGB::Blue,
  CRGB::Blue,
  CRGB::Aqua,
  CRGB::Aqua,
  CRGB::Aqua,
  CRGB::Aqua,
  CRGB::DarkBlue,
  CRGB::DarkBlue,
  CRGB::DarkBlue,
  CRGB::DarkBlue,
  CRGB::Cyan,
  CRGB::Cyan,
  CRGB::Cyan,
  CRGB::Cyan
};

const TProgmemPalette16 PurplePalette_p PROGMEM =
{
  CRGB::Magenta,
  CRGB::Magenta,
  CRGB::Magenta,
  CRGB::Magenta,
  CRGB::Purple,
  CRGB::Purple,
  CRGB::Purple,
  CRGB::Purple,
  CRGB::Fuchsia,
  CRGB::Fuchsia,
  CRGB::Fuchsia,
  CRGB::Fuchsia,
  CRGB::Amethyst,
  CRGB::Amethyst,
  CRGB::Amethyst,
  CRGB::Amethyst,
};

const TProgmemPalette16 ISUPalette_p PROGMEM = {

  CRGB::Gold,
  CRGB::Gold,
  CRGB::Gold,
  CRGB::Gold,
  CRGB::Red,
  CRGB::Red,
  CRGB::Red,
  CRGB::Red,
  CRGB::Gold,
  CRGB::Gold,
  CRGB::Gold,
  CRGB::Gold,
  CRGB::Red,
  CRGB::Red,
  CRGB::Red,
  CRGB::Red,
};

void createChasePalette(int r, int g, int b) {
  Chase_p[0] = CRGB(r, g, b);
  for (int i = 1; i < 16; i++) {
    Chase_p[i] = CRGB::Black;
  }
}
