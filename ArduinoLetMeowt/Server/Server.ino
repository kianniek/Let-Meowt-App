#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

#define SERVICE_UUID "635477fa-2364-4d09-92b5-0007c47cfef3"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
// https://www.uuidgenerator.net/

bool isDoorOpen;

// Callback class to handle connection events
class MyServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer *pServer) {
    Serial.println("Client Connected");
  }

  void onDisconnect(BLEServer *pServer) {
    Serial.println("Client Disconnected");
    
    // Restart advertising to be discoverable again
    BLEDevice::startAdvertising();
    Serial.println("Advertising restarted, now discoverable.");
  }
};

// Custom characteristic callback class to handle write events
class MyCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) {
    String value = pCharacteristic->getValue();      // Get the value sent by the client
    Serial.printf("Received: %s\n", value.c_str());  // Print the received value to Serial Monitor

    // You can add your logic here to handle the received data
    // For example, you can parse the value and respond accordingly
    if(value == "Open") {
      isDoorOpen = true;
      openDoor();
      Serial.println("Door is now open.");
    }

    if(value == "Close") {
      isDoorOpen = false;
      closeDoor();
      Serial.println("Door is now closed.");
    }

    Serial.println(isDoorOpen);
  }
};

void openDoor(){
  // Add functionality for controlling a servo to open the door
}

void closeDoor(){
  // Add functionality for controlling a servo to close the door
}

void setup() {
  Serial.begin(115200); // Initialize Serial communication
  Serial.println("Starting BLE work!");

  BLEDevice::init("Let Meowt Door"); // Name of our device
  BLEServer *pServer = BLEDevice::createServer(); // Create a BLE server object
  pServer->setCallbacks(new MyServerCallbacks()); // Set connection callbacks
  BLEService *pService = pServer->createService(SERVICE_UUID); // Creates a refrence to the service 

    // Create a characteristic and set its properties
    BLECharacteristic *pCharacteristic =
      pService->createCharacteristic(CHARACTERISTIC_UUID,
                                    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);

  pCharacteristic->setValue("Hello World says Neil");
  pCharacteristic->setCallbacks(new MyCharacteristicCallbacks());  // Set write callbacks
  pService->start();

  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
  Serial.println("Characteristic defined! Now you can read it in your phone!");
}

void loop() {
  delay(2000);  // Just delay for demonstration
}
