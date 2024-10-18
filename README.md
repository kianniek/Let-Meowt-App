# Connecting an ESP32 to an Android App via BLE: Let Meowt Door opening solution when the user comes close to the door

## Table of Contents
1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Setting Up the Arduino IDE](#setting-up-the-arduino-ide)
4. [Programming the ESP32](#programming-the-esp32)
5. [Setting Up the Android App](#setting-up-the-android-app)
6. [Running the System](#running-the-system)
7. [Troubleshooting](#troubleshooting)

---

### **Manual for IoT Solution: Connecting a BLE ESP32 to an Android App**

---

#### **Introduction**

This detailed guide will walk you through the process of building an IoT system using an ESP32 microcontroller with Bluetooth Low Energy (BLE) capabilities, and an Android app to control it. This system can be used to perform tasks like opening or locking a door remotely. This manual is designed for users with basic coding knowledge, and it not only provides step-by-step instructions but also explains why each step is necessary, ensuring a deeper understanding of the project.

---

### **Step 1: Setting Up the Arduino IDE for ESP32**

Before we can start programming the ESP32, we need to ensure that the development environment is correctly set up. Here’s how to configure the Arduino IDE to work with ESP32:

#### **1.1 Install Arduino IDE**

First, if you haven't installed the Arduino IDE, follow these steps:

1. Go to the [official Arduino website](https://www.arduino.cc/en/software) and download the latest version of the Arduino IDE for your operating system.
2. Install the IDE by following the instructions for your specific operating system.

#### **1.2 Set Up ESP32 Board Support**

The ESP32 is not supported by Arduino by default, so we need to add support for it.

1. Open the Arduino IDE.
2. In the top menu, go to **File** > **Preferences**. This will open the preferences window.
3. In the field labeled **Additional Board Manager URLs**, add the following URL:  
   [https://dl.espressif.com/dl/package_esp32_index.json](http://arduino.esp8266.com/stable/package_esp8266com_index.json)
   
   This URL tells the Arduino IDE where to find the package needed to program ESP32 devices.

   ![Preferences Window showing where to add ESP32 URL](link)

4. After adding the URL, click **OK** to close the window.

#### **1.3 Install the ESP32 Board Package**

Next, we need to download and install the actual board files for ESP32.

1. Go to **Tools** > **Board** > **Boards Manager**.  
   This opens the board manager, where you can search and install new boards.
   
   ![Boards Manager in Arduino IDE](link)

2. In the Boards Manager window, type "ESP32" into the search bar.
3. Select the "ESP32 by Espressif Systems" entry and click **Install**.

   This process will download the necessary files to support ESP32 in the Arduino IDE.

#### **1.4 Select Your ESP32 Board**

Once the ESP32 package is installed, you’ll need to select the correct board for your project.

1. Go to **Tools** > **Board**.
2. Scroll through the list until you find **ESP32 Dev Module** (or your specific ESP32 model). Select it.

> **Why This Step?**  
> This ensures that the Arduino IDE knows how to compile and upload code for your ESP32. Without selecting the correct board, the code would not upload or function properly.

---

### **Step 2: Understanding and writing the ESP32 Code**

The code running on the ESP32 is critical for establishing the BLE communication between the microcontroller and the Android app. This section breaks down the important parts of the **Server.ino** code and explains what each function does, and why it's necessary.

#### **2.0 using the exiting BLE server code**

To not have to write everything manually we are going to be using an existing sketch and modifiying it to make it work with our android app we are yet to make

1. Go to **File** > **Examples** > **BLE** > **Server** this will load the "Server.ino" example file with all the code for our ESP32 to work as an BLE server and be detectable for other devices

#### **2.1 Setting Up the BLE Server (Server.ino)**

The ESP32 acts as a BLE server, allowing the Android app to connect to it and send commands. Here’s how the BLE server is initialized in the **setup()** function:

```cpp
void setup() {
   Serial.begin(115200); // Initialize Serial communication
  Serial.println("Starting BLE work!");

  BLEDevice::init("Let_Meowt_Door"); // Name of our device
  BLEServer *pServer = BLEDevice::createServer(); // Create a BLE server object
  pServer->setCallbacks(new MyServerCallbacks()); // Set connection callbacks
  BLEService *pService = pServer->createService(SERVICE_UUID); // Creates a refrence to the service 

  // More initialization code follows...
}
```

- **BLEDevice::init("ESP32_Door_Lock")**: This function initializes the BLE functionality on the ESP32 and sets the BLE device name as "Let_Meowt_Door". This name will appear in the list of available BLE devices on the Android app. It is important to note that this name should not be left empty. most systems discard any devices with no name so you won't be able to find it.

- **BLEServer**: This object represents the BLE server on the ESP32. The Android app will connect to this server to send and receive data.

> **Why This Step?**  
> The BLE server is the backbone of communication between the ESP32 and the Android app. Without this server running, the app cannot detect or communicate with the ESP32. Setting the BLE name ensures that the app can identify which device to connect to.

#### **2.2 Creating BLE Services and Characteristics**

BLE communication uses services and characteristics. A service is a collection of data (or characteristics) that the client (Android app) can interact with.

```cpp
  // Create a characteristic and set its properties
    BLECharacteristic *pCharacteristic =
      pService->createCharacteristic(CHARACTERISTIC_UUID,
                                    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);

  pCharacteristic->setCallbacks(new MyCharacteristicCallbacks());  // Set write callbacks
  pService->start();
```

- **BLEService**: This object defines a service that groups related characteristics. In this example, `SERVICE_UUID` uniquely identifies the service, which could be something like controlling a door lock.

- **BLECharacteristic**: Characteristics are the actual pieces of data that are exchanged between the server (ESP32) and client (Android app). In this case, we define a characteristic with the ability to be both read and written to by the client.

> **Why This Step?**  
> Defining services and characteristics allows the app to read from and send commands to the ESP32. For example, a characteristic could represent the state of the door (locked or unlocked), and the Android app could write to this characteristic to lock or unlock the door.

#### **2.3 Handling Door Commands**

In the code, specific functions are defined to handle the user commands for opening or locking the door. These commands are sent from the Android app to the ESP32.

```cpp
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
```

- **openDoor()**: This function is called when the app sends a command to unlock or open the door. Depending on your setup, this could involve controlling a motor or releasing a latch.
  
- **lockDoor()**: This function is called when the app sends a command to lock the door. Again, this would control the hardware responsible for securing the door.

> **Why This Step?**  
> These functions handle the core functionality of your IoT solution—controlling the physical door. The app sends a signal, and these functions trigger the relevant hardware to lock or unlock the door.

#### **2.4 Summary**

Your code should now look something like this

```cpp
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
```

---

### **Step 3: Building the Android App**

On the Android side, the app is responsible for finding BLE devices, connecting to the ESP32, and sending commands like "Open Door" or "Lock Door." Let’s explore the main parts of the Android app in detail.

#### **3.1 Main Activity (MainActivity.kt)**

The **MainActivity** is the central hub of your app. It’s responsible for scanning BLE devices and initiating connections. Here’s a breakdown of key components in this file:

```kotlin
fun scanForDevices() {
    // Start scanning for BLE devices
    bluetoothAdapter?.startLeScan(leScanCallback)
}
```

- **startLeScan()**: This function initiates a BLE scan. It tells the app to search for nearby BLE devices, such as the ESP32 running the BLE server we set up earlier.

> **Why This Step?**  
> Scanning is the first step in discovering the ESP32. The Android app needs to find the BLE server before it can communicate with it. This scan will display available devices to the user.

#### **3.2 Handling Scan Results**

Once the scan is complete, the app displays the available devices for the user to select. The scan results are handled by a callback function.

```kotlin
val leScanCallback = BluetoothAdapter.LeScanCallback { device, _, _ ->
    // Add discovered device to a list
    if (device.name != null) {
        devicesList.add(device)
        deviceAdapter.notifyDataSetChanged()
    }
}
```

- **leScanCallback**: This callback is triggered whenever a BLE device is found. If the device has a name (like "ESP32_Door_Lock"), it gets added to a list that is shown to the user.

> **Why This Step?**  
> The user needs to see a list of available BLE devices to choose the correct one. The callback ensures that every discovered device is added to this list, which is then shown on the app's interface.

---

#### **3.3 Managing the User Interface (activity_main.xml)**

The user interface (UI) of the Android app is defined in **activity_main.xml**. This file contains all the buttons and lists that the user interacts with.

```xml
<Button
    android:id="@+id/btnScan"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Scan" />
```

- **btnScan**: This button starts the scanning process when pressed. When the user clicks this, the app searches for nearby BLE devices, like the ESP32.

- **RecyclerView (rvDevices)**: The RecyclerView is used to display a list of available BLE devices found during the scan.

```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/rvDevices"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_weight="1" />
```

> **Why This Step?

**  
> The UI is essential for user interaction. Buttons like "Scan," "Connect," "Open Door," and "Lock Door" allow the user to control the ESP32. The RecyclerView lists available BLE devices for easy selection.

---

### **Step 4: Connecting the Android App to ESP32**

After the user selects the ESP32 from the list of available devices, the app establishes a connection to the ESP32’s BLE server.

```kotlin
fun connectToDevice(device: BluetoothDevice) {
    val gatt = device.connectGatt(this, false, gattCallback)
}
```

- **connectGatt()**: This function initiates the connection to the selected BLE device. It establishes the communication link between the app and the ESP32 BLE server.

---

### **Step 5: Sending Commands to the ESP32**

Once connected, the app can send commands to the ESP32 to either open or lock the door.

```kotlin
fun sendCommand(command: String) {
    // Write the command to the BLE characteristic
    characteristic.setValue(command.toByteArray())
    bluetoothGatt.writeCharacteristic(characteristic)
}
```

- **setValue()**: This function sets the command (like "open" or "lock") as the value of the BLE characteristic.
- **writeCharacteristic()**: This sends the value to the ESP32, where it is processed to perform the action (e.g., opening the door).

> **Why This Step?**  
> Sending commands is the main purpose of the IoT system. The app translates user actions (pressing "Open Door" or "Lock Door") into BLE commands, which the ESP32 interprets and executes.

---

### **Step 6: Testing and Troubleshooting**

Now that the system is set up, it’s time to test it and troubleshoot any potential issues.

#### **6.1 Upload Code to ESP32**

1. Open **Server.ino** in the Arduino IDE.
2. Make sure the correct board is selected (**Tools** > **Board** > **ESP32 Dev Module**).
3. Connect the ESP32 to your computer via USB.
4. Press the **Upload** button to upload the code to the ESP32.

#### **6.2 Install the Android App**

1. Build the Android app in Android Studio and install it on your Android device.
2. Open the app and press the **Scan** button to search for nearby BLE devices.
3. Select the ESP32 from the list of available devices.
4. Press **Connect** and then try the **Open Door** and **Lock Door** buttons to test the functionality.

---

### **Conclusion**

By following this detailed guide, you should now have a fully functioning IoT solution that allows an Android app to control an ESP32 over BLE. We’ve covered every step in detail, ensuring you understand not just the **how**, but the **why** behind each action. Whether you are a beginner or have some experience with coding, this journey has taken you from setting up the development environment to establishing BLE communication and testing the final system.

