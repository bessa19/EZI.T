#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

BLEServer *pServer = NULL;
BLEService *pService = NULL;
BLECharacteristic *pCharacteristic = NULL;

void setup() {
  Serial.begin(115200);
  Serial.println("Starting BLE work!");

  BLEDevice::init("ESP32_GATE_CONTROL");
  pServer = BLEDevice::createServer();
  pService = pServer->createService(BLEUUID((uint16_t)0x180F)); // Generic Service

  pCharacteristic = pService -> createCharacteristic(
    BLEUUID((uint16_t)0x2A19), // Generic Characteristic
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE
  );

  pCharacteristic -> setValue("Hello from ESP32!");
  pService -> start();
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->start();
}

void loop() {
  delay(2000);
}
