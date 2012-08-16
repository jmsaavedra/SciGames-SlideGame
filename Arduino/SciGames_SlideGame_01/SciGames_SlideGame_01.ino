#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

boolean RFID_GO = false;
boolean MASS_GO = false;
boolean mass_leds = false;
boolean haveInfoToSend = false;
byte infoToSend[5];
int numBytesToSend = 5;
boolean currLedStatus = false; //true==on

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
  Serial3.begin(9600);   
  initLeds();
  delay(500);
  acc.powerOn();

  //RFID_GO = true;
  Serial.println("...SETUP DONE");
}

void loop () {

  checkForAndroidComm();

  if(RFID_GO){ //received the on RFID page message
    RFID_reader();
  }

  if (!RFID_GO){
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
      case 'X':
        Serial.println("received X"); //prepare for RFID Scan
        RFID_GO = true;
        break;

//      case 'B':
//        Serial.println("received B"); //prepare for Scale read
//        //initMass(); // calibrate with NO weight
//        mass_leds = true;
//        break;
//
//      case 'C':
//        Serial.println("received C"); //get my mass!
//        mass_leds = false;
//        RFID_GO = false;
//        MASS_GO = true;
//        break;
//        
      case 'D':
        Serial.println("received D"); //turn LEDs off, standby
        redOn();
        break;
//        
//      case 'Z':
//        Serial.println("received Z"); //turn LEDs off, standby
//        currLedStatus = !currLedStatus;
//        allLeds(currLedStatus);
//        break;
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
  } else {
    //redOn();
    greenOn();
  }
}

void reset(){
  haveInfoToSend = false;
  RFID_GO = false;
  ledsOff();
}





