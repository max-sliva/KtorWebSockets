#include <Keypad.h>

const byte numRows = 4; //кол-во строк
const byte numCols = 4; //кол-во столбцов

//матрица, определяющая расположение эл-ов на клавиатуре
char keymap[numRows][numCols] =
{
  {'1', '2', '3', 'A'},
  {'4', '5', '6', 'B'},
  {'7', '8', '9', 'C'},
  {'*', '0', '#', 'D'}
};

//Code that shows the the keypad connections to the arduino terminals
//определяем подключение пинов для строк и столбцов в виде одномерных массивов
byte rowPins[numRows] = {9, 8, 7, 6}; 
byte colPins[numCols] = {5, 4, 3, 2}; 

//инициализируем клавиатуру
Keypad myKeypad = Keypad(makeKeymap(keymap), rowPins, colPins, numRows, numCols);

void setup()
{
  Serial.begin(9600);
}

void loop()
{
  char keypressed = myKeypad.getKey();
  if (keypressed != NO_KEY)
  {
    Serial.println(keypressed);
  }
}