int SDI = 6; //Red wire (not the red 5V wire!)
int CKI = 7; //Green wire

#define STRIP_LENGTH 32 //32 LEDs on this strip

long strip_colors[STRIP_LENGTH];

long red =  0x0000FF;
long green = 0x00FF00;
long blue = 0xFF0000;





void ledMeterSetup() {
  pinMode(SDI, OUTPUT);
  pinMode(CKI, OUTPUT);
  levelDefault();
  
  //Clear out the array
  for(int x = 0 ; x < STRIP_LENGTH ; x++)
    strip_colors[x] = 0;
  
}

/////////////////////////////////////////// DEFAULT

void levelDefault(void){
    long default_color = 32;
        for(int x = 0 ; x < 32; x++){
         strip_colors[x] = blue;
        }
    post_frame();

  
}



void setLedMeter(int kinetic, int thermal) {

    for(int x = 0 ; x < kinetic; x++){
      strip_colors[x] = green;
  }
    for(int x = thermal ; x < 32; x++){
      strip_colors[x] = red;
  }
  post_frame();
}



/////////////////////////////////////////// 50/50 = 16/ 16
void levelOne(void) {

  long fifty_fifty_color_a = 0;
  long fifty_fifty_color_b = 16;
  
    for(int x = 0 ; x < 16; x++){
      strip_colors[x] = green;
  }
    for(int x = 16 ; x < 32; x++){
      strip_colors[x] = red;
  }
  post_frame();
}


  
///////////////////////////////////////////  
/////////////////////////////////////////// 60/40 = 19 / 13
void levelTwo(void) {
  long sixty_forty_color_a = 19;
  long sixty_forty_color_b = 13;
  
    for(int x = 0 ; x < 19; x++){
      strip_colors[x] = green;
  }
  
  for(int x = 19 ; x < 32; x++){
      strip_colors[x] = red;
  }
  post_frame();
}


///////////////////////////////////////////  
/////////////////////////////////////////// 40/60 = 12 / 20
void levelThree(void) {

  long forty_sixty_color_a = 12;
  long forty_sixty_color_b = 20;
      
    for(int x = 0 ; x < 12; x++){
      strip_colors[x] = green;
  }
  
  for(int x = 12 ; x < 32; x++){
      strip_colors[x] = red;
  }
  post_frame();
}
  
///////////////////////////////////////////  
/////////////////////////////////////////// 70/30 = 22 / 10
void levelFour(void) {

  long seventy_thirty_color_a = 22;
  long seventy_thirty_color_b = 10;
  
    for(int x = 0 ; x < 22; x++){
      strip_colors[x] = green;
  }
  
  for(int x = 22 ; x < 32; x++){
      strip_colors[x] = red;
  }
  post_frame();
}
  


///////////////////////////////////////////  
/////////////////////////////////////////// 30/70 = 9 / 23
void levelFive(void) {
  
  long thirty_seventy_color_a = 9;
  long thirty_seventy_color_b = 23;
  
    for(int x = 0 ; x < 9; x++){
      strip_colors[x] = green;
  }
  
  for(int x = 9 ; x < 32; x++){
      strip_colors[x] = red;
  }
post_frame();
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
