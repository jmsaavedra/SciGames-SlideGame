#define SIG1_PIN  A5
#define SIG2_PIN  A4
#define SIG3_PIN  A3
#define SIG4_PIN  A2

#define THRESHOLD  200
#define HYSTERESIS 20
#define TIMEOUT    10000

boolean debug = false;
boolean verbose = false;
int gateState = 1;

long accums[] = {
  0, 0, 0, 0};
byte vals[] = {
  0, 0, 0, 0};
byte last_vals[] = {
  0, 0, 0, 0};
long times[] = {
  3, 2, 1, 0};

unsigned int timer1 = 0;
unsigned int timer2 = 0;
unsigned int timer3 = 0;

void send_result(byte val1, byte val2, byte val3, byte val4) {
  byte msg[6];  
  msg[0] = val1;
  msg[1] = val2;
  msg[2] = val3;
  msg[3] = val4;
  if (msg[0] == 255)
    msg[0] = 254;
  if (msg[1] == 255)
    msg[1] = 254;
  if (msg[2] == 255)
    msg[2] = 254;
  if (msg[3] == 255)
    msg[3] = 254;

  msg[4] = (msg[0] ^ msg[1] ^ msg[2] ^ msg[3]);
  if (msg[4] == 255)
    return;
  msg[5] = 255;

  for (int i=0; i<sizeof(msg); i++) {
    Serial.write(msg[i]);
  }
}

void updateSlide(){
  for (i=0; i<4; i++) {
    accums[i] = 0;
    for (j=0; j<8; j++) {
      accums[i] += analogRead(sig_pins[i]);
      delayMicroseconds(5);
    } 

    vals[i] = (byte)((accums[i] / 8) >> 2);
  }
  if (debug == true) {
    send_result(vals[0], vals[1], vals[2], vals[3]);
    delay(1);       
  } 
  else {
    switch (gateState) {
    case 1:
      if (vals[0] > THRESHOLD && vals[0] < last_vals[0] && (millis() - times[0] > 80)) {
        times[0] = millis();
        if (verbose) {
          Serial.print("(");
          Serial.print(vals[0]);
          Serial.print(") ");
        }
      }
      if (vals[1] > THRESHOLD && vals[1] < last_vals[1]) {
        times[1] = millis();
        if (verbose) {
          Serial.print("(");
          Serial.print(vals[1]);
          Serial.print(") ");          
          Serial.print(times[0]);
          Serial.print(" ");
          Serial.print(times[1]);
          Serial.print(" ");
        }
        gateState = 2;
      }          
      break;
    case 2:
      if (vals[2] > THRESHOLD && vals[2] < last_vals[2] && (millis() - times[2] > 80)) {          
        times[2] = millis();
        if (verbose) {
          Serial.print("(");
          Serial.print(vals[2]);
          Serial.print(") ");
        }
      }  
      if (vals[3] > THRESHOLD && vals[3] < last_vals[3]) {
        times[3] = millis();
        if (verbose) {
          Serial.print("(");
          Serial.print(vals[3]);
          Serial.print(") ");
          Serial.print(times[2]);
          Serial.print(" ");
          Serial.print(times[3]);
          Serial.print(" - ");
        }
        timer1 = times[1] - times[0];
        timer2 = times[2] - times[1];
        timer3 = times[3] - times[2];
        Serial.print(timer1);
        Serial.print(", ");
        Serial.print(timer2);
        Serial.print(", ");
        Serial.println(timer3);
        ledsOff();
        SLIDE_GO = false;
        sendSlideTimes(timer1, timer2, timer3);
        gateState = 1;
      }
    }
  }
  for (i=0; i<4; i++) {
    last_vals[i] = vals[i];
  }   
}

void sendSlideTimes(int one, int two, int three) {
  byte msg[7]; //pack all data into 6 bytes, 7th is for CRC
  
  msg[0] = (byte)((one >> 8) & 0x00FF); // 256*this number
  msg[1] = (byte)(one & 0x00FF);        //leftover
  msg[2] = (byte)((two >> 8) & 0x00FF);
  msg[3] = (byte)(two & 0x00FF);
  msg[4] = (byte)((three >> 8) & 0x00FF);
  msg[5] = (byte)(three & 0x00FF);
  msg[6] = msg[0] ^ msg[1] ^ msg[2] ^ msg[3] ^ msg[4] ^ msg[5]; //CRC
  //CRC info: http://en.wikipedia.org/wiki/Cyclic_redundancy_check

  for (int i=0; i<(sizeof(msg) / sizeof(byte)); i++) {
    Serial.println(msg[i]);
  }
  acc.write(msg, 7); //send all bytes as one package
}  

boolean check_times() {
  long current_time = millis();
  if (current_time - times[0] < TIMEOUT &&
    current_time - times[1] < TIMEOUT &&
    current_time - times[2] < TIMEOUT &&
    current_time - times[3] < TIMEOUT &&
    times[3] > times[2] &&
    times[2] > times[1] &&
    times[1] > times[0])
    return true;
  else 
    return false;
}






