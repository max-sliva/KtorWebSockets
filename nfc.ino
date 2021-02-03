#include <Wire.h>
#include <SPI.h>
#include <Adafruit_PN532.h> // библиотека для работы с RFID/NFC
 
#define PN532_IRQ  9  // пин прерывания
// создаём объект для работы со сканером и передаём ему два параметра
// первый — номер пина прерывания
// второй — число 100, это просто число, чтоб занять параметр
Adafruit_PN532 nfc(PN532_IRQ, 100);
 
void setup(void)
{
  Serial.begin(9600);
  nfc.begin();  // инициализация RFID/NFC сканера
  int versiondata = nfc.getFirmwareVersion(); //получаем данные о сканере
  if (!versiondata) { //если сканер не найден
    Serial.print("Didn't find RFID/NFC reader");
    while(1) { } //бесконечный цикл
  }
  Serial.println("Found RFID/NFC reader");
  nfc.SAMConfig();  // настраиваем модуль
  Serial.println("Waiting for a card ...");
}
 
void loop(void)
{
  uint8_t success; //результат чтения 
  uint8_t uid[8]; // буфер для хранения ID карты
  uint8_t uidLength; // размер буфера карты
  // слушаем новые метки
  success = nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A, uid, &uidLength);
  if (success) {  // если найдена карта
    Serial.println("Found a card");  // выводим в консоль полученные данные
    Serial.print("ID Length: ");
    Serial.print(uidLength, DEC);
    Serial.println(" bytes");
    Serial.print("ID Value: ");
    nfc.PrintHex(uid, uidLength);
    Serial.println("");
    delay(1000);
  }
}