

#define  LED1_RED       13
#define  LED1_GREEN     11
#define  LED1_BLUE      12

int currBlue = 10;
int currRed = 10;
int currGreen = 10;

int blueAddr = 1;
int redAddr = 1;
int greenAddr = 1;

void greenOn(){
  analogWrite(LED1_GREEN, 180);
  analogWrite(LED1_RED, 0);
  analogWrite(LED1_BLUE, 0);
}

void blinkGreen(int numBlinks){
  for(int i=0; i<numBlinks; i++){
    greenOn();
    delay(250);
    ledsOff();
    delay(250);
  }

}

void blueOn(){
  analogWrite(LED1_RED, 0);
  analogWrite(LED1_GREEN, 0);
  analogWrite(LED1_BLUE, 180);
}

void redOn(){
  analogWrite(LED1_RED, 180);
  analogWrite(LED1_GREEN, 0);
  analogWrite(LED1_BLUE, 0);
}

void blueFader(){

  if(currBlue > 200 || currBlue < 1){
    blueAddr *= -1;
  }
  currBlue += blueAddr;
  analogWrite(LED1_BLUE, currBlue);
  delay(5);
}

void initLeds(){

  pinMode(LED1_RED, OUTPUT);
  pinMode(LED1_GREEN, OUTPUT);
  pinMode(LED1_BLUE, OUTPUT);

  ledsOff();
}

void ledsOff(){
  analogWrite(LED1_RED, 0);
  analogWrite(LED1_GREEN, 0);
  analogWrite(LED1_BLUE, 0);
}

void allLeds( boolean state ){

  if (state){
    analogWrite(LED1_RED, 100);
    analogWrite(LED1_GREEN, 100);
    analogWrite(LED1_BLUE, 100);
  }
  else {
    analogWrite(LED1_RED, 0);
    analogWrite(LED1_GREEN, 0);
    analogWrite(LED1_BLUE, 0);
  }
}


