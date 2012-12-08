#define SIG1_PIN  A5
#define SIG2_PIN  A4
#define SIG3_PIN  A3
#define SIG4_PIN  A2

#define THRESHOLD  200
#define HYSTERESIS 20
#define TIMEOUT    10000

int i, j;
int sig_pins[] = {SIG1_PIN, SIG2_PIN, SIG3_PIN, SIG4_PIN};
long accums[] = {0, 0, 0, 0};
byte vals[] = {0, 0, 0, 0};
byte last_vals[] = {0, 0, 0, 0};
long times[] = {3, 2, 1, 0};

boolean debug = true;
boolean verbose = false;

void setup() {
  for (i=0; i<(sizeof(sig_pins)/sizeof(int)); i++) {
    digitalWrite(sig_pins[i], LOW);
    pinMode(sig_pins[i], INPUT);
  }
  
  Serial.begin(57600);
  while (!Serial)
    ;    
}

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

int gateState = 1;

void loop() {
  while (Serial.available()) {
    char c = Serial.read();
    if (c == 'd' || c == 'D')
      debug = true;
    if (c == 'r' || c == 'R') {
      Serial.println();
      debug = false;
    }
  }
  
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
  } else {
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
          Serial.print(times[1] - times[0]);
          Serial.print(", ");
          Serial.print(times[2] - times[1]);
          Serial.print(", ");
          Serial.println(times[3] - times[2]);
          gateState = 1;
        }
    }
    
    /*
    for (i=0; i<4; i++) {
      if (last_vals[i] > (THRESHOLD + HYSTERESIS) && vals[i] < (THRESHOLD - HYSTERESIS)) {
        times[i] = millis();
//        Serial.print("time: ");
//        Serial.println(i);
      }
    }
    if (check_times() == true) {
      for (i=0; i<4; i++) {
        Serial.print(times[i]);
        Serial.print(" ");
        times[i] = 0;
      }
      Serial.print("- ");
      Serial.print(times[1] - times[0]);
      Serial.print(", ");
      Serial.print(times[2] - times[1]);
      Serial.print(", ");
      Serial.print(times[3] - times[2]);
      Serial.println();      
    }  
    */  
  }
  
  for (i=0; i<4; i++) {
    last_vals[i] = vals[i];
  }
    

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

// this function will return the number of bytes currently free in RAM
//int memoryTest() {
//  int byteCounter = 0; // initialize a counter
//  byte *byteArray; // create a pointer to a byte array
//  // More on pointers here: http://en.wikipedia.org/wiki/Pointer#C_pointers
//
//  // use the malloc function to repeatedly attempt
//  // allocating a certain number of bytes to memory
//  // More on malloc here: http://en.wikipedia.org/wiki/Malloc
//  while ( (byteArray = (byte*) malloc (byteCounter * sizeof(byte))) != NULL ) {
//    byteCounter++; // if allocation was successful, then up the count for the next try
//    free(byteArray); // free memory after allocating it
//  }
//
//  free(byteArray); // also free memory after the function finishes
//  return byteCounter; // send back the highest number of bytes successfully allocated
//}

