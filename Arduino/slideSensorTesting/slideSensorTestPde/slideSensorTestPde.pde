import processing.serial.*;

Serial port;
PFont font, smallFont;
String displayString = "not connected...";

int vals[][];
int pos[] = {0, 0, 0, 0};
boolean stop = false;
boolean stopWhenFull = false;

void setup() {
  size(1200, 700);
  frameRate(30);
  
  vals = new int[4][width];
  
  font = loadFont("DialogInput-48.vlw");
  smallFont = loadFont("DialogInput-48.vlw");
  textFont(smallFont);
  
  String names[] = Serial.list();
  for (int i=0; i<names.length; i++) {
    System.out.println(i + ": " + names[i]);
  }

  String portName = names[0];
  displayString = "attempting to open " + portName; 
  if (port != null) 
    displayString = "opened " + portName;
  
  port = new Serial(this, Serial.list()[0], 57600);  
  port.clear();
}

void draw() {
  while (port != null && port.available() > 7) {
    byte[] buf = new byte[500];
    int len = port.readBytesUntil(0xFF, buf);
    if (buf != null && stop == false) {
      if (0xFF == (0x00FF & buf[5])) {
        byte crc = (byte)(((0x00FF & buf[0]) ^ (0x00FF & buf[1])) ^ (0x00FF & buf[2]) ^ (0x00FF & buf[3]));
        if (crc == buf[4]) {         
//          int val1 = (buf[1] << 7) + buf[2];
//          int val2 = (buf[3] << 7) + buf[4];
//          int val3 = (buf[5] << 7) + buf[6];
//          int val4 = (buf[7] << 8) + buf[8];
          int val1 = (int)( buf[0] & 0x00FF);
          int val2 = (int)( buf[1] & 0x00FF);
          int val3 = (int)( buf[2] & 0x00FF);
          int val4 = (int)( buf[3] & 0x00FF);
          
          vals[0][pos[0]] = val1;
          pos[0] += 1;
          vals[1][pos[1]] = val2;
          pos[1] += 1;
          vals[2][pos[2]] = val3;
          pos[2] += 1;
          vals[3][pos[3]] = val4;
          pos[3] += 1;
          if (pos[0] >= width-2) {
            if (stopWhenFull == false) {
              pos[0] = 0;
              pos[1] = 0;
              pos[2] = 0;
              pos[3] = 0;
            } else {
              stop = true;
            }
          }
          println(val1 + " " + val2 + " " + val3 + " " + val4);
        } else {
          displayString = "CRC error...";
        }
      }
    }
  }
  background(0);
  for (int i=0; i<vals[0].length-2; i++) {
    stroke(255, 255, 0); 
    line(i, height-vals[0][i], i+1, height-vals[0][i+1]);    
    stroke(0, 255, 255);
    line(i, height-vals[1][i], i+1, height-vals[1][i+1]);    
    stroke(255, 0, 255);
    line(i, height-vals[2][i], i+1, height-vals[2][i+1]);    
    stroke(0, 255, 0);
    line(i, height-vals[3][i], i+1, height-vals[3][i+1]);    
  }

  stroke(255);
  fill(255);  
  text(displayString, 50, 60);  
}

void keyTyped() {
  if (key == 's' || key == 'S') {
    for (int i=0; i<vals[0].length-2; i++) {
      vals[0][i] = 0;
      vals[1][i] = 0;
      vals[2][i] = 0;
      vals[3][i] = 0;
    }
    pos[0] = 0;
    pos[1] = 0;
    pos[2] = 0;
    pos[3] = 0;
    stop = false;
    stopWhenFull = true;
  }
  if (key == 'c' || key == 'C') {
    stop = false;
    stopWhenFull = false;
  }
}
