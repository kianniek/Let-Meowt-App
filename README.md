### **SMART PET DOOR WITH PROXIMITY DETECTION, VISION MODULE, AND WEATHER API**

#### **Description**

This project creates a smart pet door that automatically detects your pet, checks the weather, and unlocks the door when you approach with your phone. The system uses a **Node MCU** (ESP8266 and ESP32), **Grove AI Vision Module** for outside human detection, a **Weather API** for weather checks, and **Bluetooth Low Energy (BLE)** to detect when the pet owner approaches the door. A companion Android/Web app provides real-time notifications and remote control over the system.

#### **Requirements**

1. **Hardware**:
  - **Node MCU** (ESP8266 and ESP32)
  - **Grove AI Vision Module** (or an equivalent camera module like ESP32-CAM)

- **Wi-Fi** for communication
  
- **Smartphone** (Android) for BLE and app control
  
- **Power supply** (5V or battery depending on your setup)

#### **Optional**
  
- **Servo or Motorized Lock** for the door
  
- **RFID/Bluetooth Pet Collar Tag**
  
- **Proximity Sensor** (optional as a backup for the RFID tag)
  

2. **Software/Tools**:
  - **Arduino IDE** (with necessary libraries for Node MCU, BLE, Wi-Fi, and Grove Vision)
  - **Grove AI Vision Library** (or TensorFlow Lite if using a custom vision system)
  - **Android Studio** (for developing the companion Android app)
  - **Firebase** (for real-time notifications and data synchronization)
  - **Weather API** (OpenWeatherMap or similar)
  - **MQTT/HTTP Protocols** for communication between the pet door, cloud, and app

---

### **Steps**

#### **Step 1: Set Up the Node MCU**

1. Install the **Arduino IDE** and set up the **ESP8266/ESP32** board manager.
  - Go to **File > Preferences** and add the ESP8266/ESP32 board URL.
  - Install the board in **Tools > Board > Boards Manager**.
2. Connect the Node MCU to your computer and upload a simple **Wi-Fi connection test sketch** to ensure the module can connect to your Wi-Fi network.
3. Install the following libraries via the Arduino Library Manager:
  - **ESP8266WiFi** or **WiFi** (for ESP32)
  - **PubSubClient** (for MQTT if you use that protocol)
  - **BLEPeripheral** (for BLE proximity detection)
---
#### **Step 2: Configure the Vision Module**

1. Connect the **Grove AI Vision Module** to the Node MCU via the **I2C interface** or use the **ESP32-CAM** for an alternative.
2. Use the provided vision module library (e.g., **Grove AI Vision Library**) and load a sample code to test the pet detection functionality.
  - Use a **pre-trained model** for basic object detection or train your own model to recognize your pet. This can be done using **TensorFlow Lite** and uploaded to the module.
3. Integrate the vision module with the Node MCU. When a pet approaches, the module should send a signal to the Node MCU to trigger the door opening.
---
#### **Step 3: Set Up Proximity Detection with BLE**

1. Set the **Node MCU** as a **BLE beacon** or **peripheral** that listens for signals from the smartphone.
  - Program the Node MCU to scan for **BLE signals** from the smartphone (using the **BLEPeripheral** library).
  - When a paired smartphone comes within range, the Node MCU should trigger the door to unlock.
2. On the **Android app**, use **Android’s BLE API** to scan for the Node MCU’s BLE beacon when you approach the door.
  - When the BLE signal is detected, trigger the app to send an unlock command to the Node MCU over Wi-Fi.
---
#### **Step 4: Integrate the Weather API**

1. Sign up for a **Weather API** service like **OpenWeatherMap** and get your API key.
2. Write code for the Node MCU to send **HTTP requests** to the Weather API at regular intervals (e.g., every 15 minutes) to check local weather conditions.
  - If it’s raining, the Node MCU should automatically prevent the door from opening.
3. Parse the **API response** to extract weather data like **rain, temperature, wind**, and implement logic to decide whether to allow the pet to go outside.
---
#### **Step 5: Build the Android App**

1. Use **Android Studio** to develop a basic app that communicates with the pet door via **BLE** and **Wi-Fi**.
  - Add a screen to control the door (lock/unlock).
  - Implement push notifications using **Firebase Cloud Messaging (FCM)** to alert the user when the pet is near the door or when the door is locked due to bad weather.
2. Add a **manual override** button in the app to lock/unlock the door remotely.
3. Display real-time updates about the pet’s activity and weather conditions using the **Weather API** results.
---
#### **Step 6: Assemble the Door Lock System**

1. Connect a **servo motor or door actuator** to the Node MCU to act as the **lock/unlock mechanism**.
  - Write code to open/close the lock based on signals from either the vision module (pet detection) or BLE (owner proximity).
2. Ensure the door securely locks when closed and unlocks only when both the sensor and app conditions are satisfied (e.g., pet detected + good weather).

---

### **Troubleshoot**

- **Wi-Fi Connectivity Issues**:
  
  - Ensure the Node MCU is within range of the Wi-Fi router and that the credentials are correct. Use a basic **Wi-Fi test sketch** to troubleshoot connection problems.
  - Use **network diagnostic tools** to monitor signal strength and connectivity drops.
- **BLE Detection Failures**:
  
  - Check if **Bluetooth is enabled** on your smartphone.
  - If the Node MCU doesn’t detect the phone, make sure that **BLE pairing** is correctly set up and that the range is within 5-10 meters.
  - Test with other BLE devices to see if the Node MCU can detect them.
- **Vision Module Accuracy**:
  
  - If the vision module fails to detect the pet correctly, adjust the **camera angle** and lighting conditions.
  - If using a machine learning model, retrain or fine-tune the model to reduce false positives/negatives.
- **Weather API Not Responding**:
  
  - Ensure you’ve correctly implemented the **API key** and are within the **API request limits** (check the free plan limits for your chosen service).
  - Test the **HTTP request** separately using tools like **Postman** to see if the API is working.
