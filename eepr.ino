#include <avr/eeprom.h>

int address = 0;
byte value;

void setup() {
  Serial.begin(9600);
  Serial.println("Start:");
  delay(1000);
  readMemory(0, 1023);  
  EEPROM.write(10, 122);
  readMemory(0, 20);  
}

void readMemory(int start, int end){
    for (address = start; address < end; address++){
    value = EEPROM.read(address);
    Serial.print(address);
    Serial.print("\t");
    Serial.print(value, DEC);
    Serial.println();
  }
}
void loop() {
  // put your main code here, to run repeatedly:

}