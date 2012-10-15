/*
v04b is for a COMMON ANODE RGB LED

- includes RFID, updated slide sensors, led beacon

*/

#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

boolean RFID_GO = false;
boolean SLIDE_GO = false;
boolean mass_leds = false;
boolean haveInfoToSend = false;
byte infoToSend[5];
int numBytesToSend = 5;
boolean currLedStatus = false; //true==on

/* slide sensor vars */
#define SIG1_PIN  A5
#define SIG2_PIN  A4
#define SIG3_PIN  A3
#define SIG4_PIN  A2
int i, j;
int sig_pins[] = {
  SIG1_PIN, SIG2_PIN, SIG3_PIN, SIG4_PIN};


AndroidAccessory acc("Joe Saavedra",
"SG SlideGame",
"SciGames Slide Game Board",
"1.0",
"http://jos.ph",
"0000000012345678");

void setup() {
  // connect to the serial port
  Serial.begin(9600);  
  Serial.println("SETUP BEGIN...");
  initLeds();

  for (i=0; i<(sizeof(sig_pins)/sizeof(int)); i++) {
    digitalWrite(sig_pins[i], LOW);
    pinMode(sig_pins[i], INPUT);
  }
  delay(500);   
  acc.powerOn();
  
  Serial.println("MEASURING SENSOR OFFSETS - DO NOT TOUCH SENSORS FOR 5 SECONDS");
  measure_slide_sensor_offsets();
  
  Serial.println("...SETUP DONE");
}

void loop () {

  checkForAndroidComm();

  if(RFID_GO){ //received the on RFID page message
    RFID_reader();
  }

  if (SLIDE_GO){
    updateSlide();
  }
}

void checkForAndroidComm(){

  byte msgIn[3];
  if (acc.isConnected()) {
    int len = acc.read(msgIn, sizeof(msgIn), 1);

    //    Serial.println( "len: " + len );

    if (len > 0) {
      Serial.println("-------msg------");
      Serial.print((char)msgIn[0]);
      Serial.println((char)msgIn[1]);

      Serial.println("-------  -------");
      char command = (char)msgIn[1];

      Serial.println("command: ");
      Serial.println(command);
      Serial.println();
      ledsOff();

      switch(command) {
      case 'Y':
        Serial.println("received Y"); //prepare for RFID Scan
        Serial3.begin(9600);
        delay(50); //let serial initialize
        ledsOff();
        RFID_GO = true;
        break;

      case 'S':
        Serial.println("received S"); //prepare for SLIDE sensor data
        //initMass(); // calibrate with NO weight
        redOn();
        SLIDE_GO = true;
        break;

      case 'Z':
        Serial.println("received Z"); //turn LEDs on/off = standby
        currLedStatus = !currLedStatus;
        allLeds(currLedStatus);
        break;
      }
    } 
    else {
      if(haveInfoToSend){
        //redOn(); // turn on light
        acc.write(infoToSend, numBytesToSend);
        delay(1000);
        haveInfoToSend = false;
        ledsOff();
      }
    }
  } 
  else {
    //solid green when not connected to board
    RFID_GO = false;
    haveInfoToSend = false;
    greenOn();
  }
}

void reset(){
  haveInfoToSend = false;
  RFID_GO = false;
  ledsOff();
}







