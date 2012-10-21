
/* for use with Addressable LED strip:

https://www.sparkfun.com/products/11272 

*/

int SDI = 6; //Yellow
int CKI = 7; //Green wire

#define STRIP_LENGTH 20 //20 LEDs on this strip

long strip_led[STRIP_LENGTH];

long red =  0x0000FF;
long green = 0x00FF00;
long blue = 0xFF0000;


void ledMeterSetup() {
  pinMode(SDI, OUTPUT);
  pinMode(CKI, OUTPUT);
  levelDefault();

  //Clear out the array
  for(int x = 0 ; x < STRIP_LENGTH ; x++)
    strip_led[x] = 0;

}

/////////////////////////////////////////// DEFAULT

void levelDefault(void){
  long default_color = 32;
  for(int x = 0 ; x < STRIP_LENGTH; x++){
    strip_led[x] = blue;
  }
  post_frame();
}

void setLedMeter(int kinetic, int thermal) {

  for(int x = 0 ; x < kinetic; x++){
    strip_led[x] = green;
  }
  for(int x = thermal ; x < STRIP_LENGTH; x++){
    strip_led[x] = red;
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
    long this_led_color = strip_led[LED_number]; //24 bits of color data

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
  digitalWrite(CKI, LOW);
  delayMicroseconds(500); //Wait for 500us to go into reset
}

