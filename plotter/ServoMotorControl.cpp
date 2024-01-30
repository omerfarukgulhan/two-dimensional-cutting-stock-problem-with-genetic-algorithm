#include "ServoMotorControl.h"
#include <Servo.h>


Servo myServo;

void ServoMotorInit()
{
  myServo.attach(SERVO_PIN);
  PenDown();
}

void PenUp()
{
  myServo.write(PEN_UP_SERVO_VALUE);
}

void PenDown()
{
  myServo.write(PEN_DOWN_SERVO_VALUE);
}