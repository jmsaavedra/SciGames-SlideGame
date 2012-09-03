
/* all led functions */

#define  LED1_RED       13 //pin assignments
#define  LED1_GREEN     11
#define  LED1_BLUE      12

int currBlue = 10; //vals for fading
int currRed = 10;
int currGreen = 10;

int fadeAdder = 1; //adder for fading, controls fade speed

int onVal = 50; //brightness of LEDs when 'on'

void initLeds(){
  pinMode(LED1_RED, OUTPUT);
  pinMode(LED1_GREEN, OUTPUT);
  pinMode(LED1_BLUE, OUTPUT);
  ledsOff();
}

void greenOn(){
  analogWrite(LED1_GREEN, onVal);
  analogWrite(LED1_RED, 0);
  analogWrite(LED1_BLUE, 0);
}
void blueOn(){
  analogWrite(LED1_BLUE, onVal);
  analogWrite(LED1_RED, 0);
  analogWrite(LED1_GREEN, 0);
}

void redOn(){
  analogWrite(LED1_RED, onVal);
  analogWrite(LED1_GREEN, 0);
  analogWrite(LED1_BLUE, 0);
}

void blueFader(){

  if(currBlue > onVal || currBlue < 1){
    fadeAdder *= -1;
  }
  currBlue += fadeAdder;
  analogWrite(LED1_RED, 0);
  analogWrite(LED1_GREEN, 0);
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
  analogWrite(LED1_RED, 0);
  analogWrite(LED1_GREEN, 0);
  analogWrite(LED1_BLUE, 0);
}

void allLeds(boolean state){
  if (state){
    analogWrite(LED1_RED, 40);
    analogWrite(LED1_GREEN, 50);
    analogWrite(LED1_BLUE, 50);
  }
  else {
    analogWrite(LED1_RED, 0);
    analogWrite(LED1_GREEN, 0);
    analogWrite(LED1_BLUE, 0);
  }
}



