
#include <Servo.h> 
#include <Stepper.h>

void setup() 
{ 
  // initialize serial communication at 9600 bits per second:
  Serial.begin(9600);
 
} 
 
 
void loop() 
{ 
  
  
  Serial.print("1x6t;"); 
  delay(15000);
  Serial.print("1x8u;");
  delay(15000);
  Serial.print("1x8p;");
  delay(30000);
  Serial.print("1x8t;"); 
  delay(15000);
  Serial.print("4x8p;");
  delay(15000);
  Serial.print("8x8t;");
  delay(15000);
  Serial.print("1x8u;");
  delay(30000);
  Serial.print("9x8u;");
  delay(15000);
  Serial.print("9x8p;");
  delay(15000);
  Serial.print("1x8t;");
  delay(30000);
  Serial.print("8x8p;"); 
  delay(15000);
  Serial.print("4x10t;");
  delay(15000);
  Serial.print("4x11p;"); 
  delay(60000);
} 



