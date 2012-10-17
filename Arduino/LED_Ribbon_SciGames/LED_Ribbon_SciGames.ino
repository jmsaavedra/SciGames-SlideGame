
int SDI = 2; //Red wire (not the red 5V wire!)
int CKI = 3; //Green wire
int ledPin = 13; //On board LED

#define STRIP_LENGTH 32 //32 LEDs on this strip



long strip_colors[STRIP_LENGTH];

void setup() {
  pinMode(SDI, OUTPUT);
  pinMode(CKI, OUTPUT);
  pinMode(ledPin, OUTPUT);
  
  //Clear out the array
  for(int x = 0 ; x < STRIP_LENGTH ; x++)
    strip_colors[x] = 0;
  
  randomSeed(analogRead(0));
  
  //Serial.begin(9600);
  //Serial.println("Hello!");
}

void loop() {
  //Pre-fill the color array with known values
  strip_colors[0] = 0xFF0000; //Bright Red
  strip_colors[1] = 0x00FF00; //Bright Green
  strip_colors[2] = 0x0000FF; //Bright Blue
  strip_colors[3] = 0x010000; //Faint red
  strip_colors[4] = 0x800000; //1/2 red (0x80 = 128 out of 256)
  post_frame(); //Push the current color frame to the strip
 
  while(1){ //Do nothing
    addNew();
    post_frame(); //Push the current color frame to the strip

    digitalWrite(ledPin, HIGH);   // set the LED on
    delay(250);                  // wait for a second
    digitalWrite(ledPin, LOW);    // set the LED off
    delay(250);                  // wait for a second
  }
}



//Resets the colors in the array
void addNew(void) {
  int x;
  
  //First, shuffle all the current colors down one spot on the strip
  for(x = (STRIP_LENGTH) ; x > 0 ; x--)
    strip_colors[x] = strip_colors[x - 1];
    
//  //Now form a new RGB color
  long default_color = 0;
  for(x = 0 ; x < STRIP_LENGTH; x++){
     default_color = 0xFF3366; //Give Default Blue
  }


  
}




/////////////////////////////////////////// 50/50 = 16/ 16
void levelOne(void) {

  long fifty_fifty_color_a = 0;
  long fifty_fifty_color_b = 16;
  
    for(x = 0 ; x < 16; x++){
      fifty_fifty_color_a = 0x00FF33; //Give Default Green
  }
    for(y = 16 ; y < 32; y++){
      fifty_fifty_color_b = 0x0000CC; //Give Default Red
  }
}


  
///////////////////////////////////////////  
/////////////////////////////////////////// 60/40 = 19 / 13
void levelTwo(void) {
  long sixty_forty_color_a = 19;
  long sixty_forty_color_b = 13;
  
    for(z = 0 ; z < 19; z++){
     sixty_forty_color_a = 0x00FF33; //Give Default Green
  }
  
  for(a = 16 ; a < 32; a++){
     sixty_forty_color_b = 0x0000CC; //Give Default Red
  }
  
}
  
///////////////////////////////////////////  
/////////////////////////////////////////// 70/30 = 22 / 10
void levelThree(void) {

  long seventy_thirty_color_a = 22;
  long seventy_thirty_color_b = 10;
  
    for(b = 0 ; b < 22; b++){
     seventy_thirty_color_a = 0x00FF33; //Give Default Green
  }
  
  for(c = 16 ; c < 32; c++){
     seventy_thirty_color_b = 0x0000CC; //Give Default Red
  }
}
  
///////////////////////////////////////////  
/////////////////////////////////////////// 40/60 = 12 / 20
void levelFour(void) {

  long forty_sixty_color_a = 12;
  long forty_sixty_color_b = 20;
      
    for(d = 0 ; d < 12; d++){
    forty_sixty_color_a = 0x00FF33; //Give Default Green
  }
  
  for(e = 16 ; e < 32; e++){
     forty_sixty_color_b = 0x0000CC; //Give Default Red
  }
  
}

///////////////////////////////////////////  
/////////////////////////////////////////// 30/70 = 9 / 23
void levelFive(void) {
  
  long thirty_seventy_color_a = 9;
  long thirty_seventy_color_b = 23;
  
    for(f = 0 ; f < 9; f++){
     thirty_seventy_color_a = 0x00FF33; //Give Default Green
  }
  
  for(e = 16 ; e < 32; e++){
     thirty_seventy_color_b = 0x0000CC; //Give Default Red
  }

}


//Takes the current strip color array and pushes it out
void post_frame (void) {
  //Each LED requires 24 bits of data
  //MSB: R7, R6, R5..., G7, G6..., B7, B6... B0 
  //Once the 24 bits have been delivered, the IC immediately relays these bits to its neighbor
  //Pulling the clock low for 500us or more causes the IC to post the data.

  for(int LED_number = 0 ; LED_number < STRIP_LENGTH ; LED_number++) {
    long this_led_color = strip_colors[LED_number]; //24 bits of color data

    for(byte color_bit = 23 ; color_bit != 255 ; color_bit--) {
      //Feed color bit 23 first (red data MSB)
      
      digitalWrite(CKI, LOW); //Only change data when clock is low
      
      long mask = 1L << color_bit;
      //The 1'L' forces the 1 to start as a 32 bit number, otherwise it defaults to 16-bit.
      
      if(this_led_color & mask) 
        digitalWrite(SDI, HIGH);
      else
        digitalWrite(SDI, LOW);
  
      digitalWrite(CKI, HIGH); //Data is latched when clock goes high
    }
  }

  //Pull clock low to put strip into reset/post mode
//  digitalWrite(CKI, LOW);
//  delayMicroseconds(500); //Wait for 500us to go into reset
}
