
/* all led functions */

#define  LED1_RED       13 //pin assignments
#define  LED1_GREEN     11
#define  LED1_BLUE      12

float currBlue = 10; //vals for fading
float currRed = 10;
float currGreen = 10;

float fadeAdder = 0.5; //adder for fading, controls fade speed

int onVal = 150; //brightness of LEDs when 'on'

void initLeds(){
  pinMode(LED1_RED, OUTPUT);
  pinMode(LED1_GREEN, OUTPUT);
  pinMode(LED1_BLUE, OUTPUT);
  ledsOff();
}

void greenOn(){
  analogWrite(LED1_GREEN, onVal);
  analogWrite(LED1_RED, 255);
  analogWrite(LED1_BLUE, 255);
}
void blueOn(){
  analogWrite(LED1_BLUE, onVal);
  analogWrite(LED1_RED, 255);
  analogWrite(LED1_GREEN, 255);
}

void redOn(){
  analogWrite(LED1_RED, onVal);
  analogWrite(LED1_GREEN, 255);
  analogWrite(LED1_BLUE, 255);
}

void blueFader(){

  if(currBlue > onVal || currBlue < 1){
    fadeAdder *= -1;
  }
  currBlue += fadeAdder;
  analogWrite(LED1_RED, 255);
  analogWrite(LED1_GREEN, 255);
  analogWrite(LED1_BLUE, currBlue);
  delay(5);
}

void blinkGreen(int numBlinks){
  for(int i=0; i<numBlinks; i++){
    greenOn();
    delay(250);
    ledsOff();
    delay(250);
  }
}

void ledsOff(){
  analogWrite(LED1_RED, 255);
  analogWrite(LED1_GREEN, 255);
  analogWrite(LED1_BLUE, 255);
}

void allLeds(boolean state){
  if (state){
    analogWrite(LED1_RED, 0);
    analogWrite(LED1_GREEN, 0);
    analogWrite(LED1_BLUE, 0);

  }
  else {
    analogWrite(LED1_RED, 15);
    analogWrite(LED1_GREEN, 25);
    analogWrite(LED1_BLUE, 30);
  }
}




