#ifndef SERVOMOTORCONTROL_H
#define SERVOMOTORCONTROL_H
#include <Arduino.h>

#define   SERVO_PIN             11
#define   PEN_UP_SERVO_VALUE    60
#define   PEN_DOWN_SERVO_VALUE  120

void ServoMotorInit();
void PenUp();
void PenDown();

#endif
