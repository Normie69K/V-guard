#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <TinyGPS++.h>
#include <HardwareSerial.h>
#include <Wire.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>

// Required by Firebase ESP Client
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// ─── 1. WIFI & FIREBASE CONFIG ───────────────────────────
#define WIFI_SSID "YOUR_WIFI_NAME"
#define WIFI_PASSWORD "YOUR_WIFI_PASSWORD"

#define API_KEY "AIzaSyBRl2h6plN-ppWbF-RWq7OtQEXNN-QW6yQ"
#define DATABASE_URL "https://v-guard-e57f8-default-rtdb.firebaseio.com"

// ─── 2. HARDWARE PINS ────────────────────────────────────
#define CRASH_SENSOR_PIN 4  // SW-420 Vibration Sensor

#define GPS_RX_PIN 16       // NEO-6M
#define GPS_TX_PIN 17

#define GSM_RX 26           // SIM800L / GSM Module
#define GSM_TX 27

#define BUZZER_PIN 25

// ─── 3. GLOBAL OBJECTS ───────────────────────────────────
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org");

TinyGPSPlus gps;
HardwareSerial GPS_Serial(2); // UART 2 for GPS
HardwareSerial GSM_Serial(1); // UART 1 for GSM

Adafruit_MPU6050 mpu;

// ─── 4. VARIABLES ────────────────────────────────────────
String espMacAddress;
unsigned long sendDataPrevMillis = 0;    // Timer for 5-sec Live Status
unsigned long sendHistoryPrevMillis = 0; // Timer for 60-sec Trip History
bool accidentDetected = false;

// Hardware SOS fallback number (The Android app also sends to its own list)
String emergencyNumber = "+91XXXXXXXXXX";

// ─── HELPER: GSM SMS FUNCTION ────────────────────────────
void sendSMS(String message) {
    GSM_Serial.println("AT");
    delay(1000);

    GSM_Serial.println("AT+CMGF=1"); // Set SMS to text mode
    delay(1000);

    GSM_Serial.print("AT+CMGS=\"");
    GSM_Serial.print(emergencyNumber);
    GSM_Serial.println("\"");
    delay(1000);

    GSM_Serial.print(message);
    delay(100);

    GSM_Serial.write(26); // ASCII code for CTRL+Z to send the SMS
    delay(5000);
}

// ─── HELPER: UNIFIED ACCIDENT TRIGGER ────────────────────
// This prevents duplicating the buzzer/SMS/Firebase code
void triggerAccident(String source) {
    if (accidentDetected) return; // Prevent double-triggering

    Serial.println("⚠️ ACCIDENT DETECTED BY: " + source);
    accidentDetected = true;

    // 1. Turn on the alarm
    digitalWrite(BUZZER_PIN, HIGH);

    // 2. Alert the Android App via Firebase instantly
    String path = "devices/" + espMacAddress + "/status/is_accident";
    Firebase.RTDB.setBool(&fbdo, path.c_str(), true);

    // 3. Send Hardware SMS (Hardware fallback)
    if (gps.location.isValid()) {
        // Fixed the Google Maps link structure so it is clickable
        String message = "🚨 V-GUARD ACCIDENT ALERT\nLocation:\nhttp://maps.google.com/maps?q=" +
                         String(gps.location.lat(), 6) + "," +
                         String(gps.location.lng(), 6);
        sendSMS(message);
    } else {
        sendSMS("🚨 V-GUARD ACCIDENT ALERT\nLocation unknown. GPS signal lost.");
    }
}

// ─── SETUP ───────────────────────────────────────────────
void setup() {
    Serial.begin(115200);

    pinMode(CRASH_SENSOR_PIN, INPUT_PULLUP);
    pinMode(BUZZER_PIN, OUTPUT);

    GPS_Serial.begin(9600, SERIAL_8N1, GPS_RX_PIN, GPS_TX_PIN);
    GSM_Serial.begin(9600, SERIAL_8N1, GSM_RX, GSM_TX);

    Wire.begin();

    // Initialize Accelerometer
    if (!mpu.begin()) {
        Serial.println("❌ MPU6050 NOT FOUND");
    } else {
        Serial.println("✅ MPU6050 Ready");
    }

    // Connect WiFi
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("Connecting WiFi");
    while (WiFi.status() != WL_CONNECTED) {
        Serial.print(".");
        delay(300);
    }
    Serial.println("\n✅ WiFi Connected");

    // Get MAC Address for Firebase
    espMacAddress = WiFi.macAddress();
    Serial.println("================================");
    Serial.print("🚀 DEVICE ID: ");
    Serial.println(espMacAddress);
    Serial.println("================================");

    timeClient.begin();

    // Initialize Firebase
    config.api_key = API_KEY;
    config.database_url = DATABASE_URL;

    if (Firebase.signUp(&config, &auth, "", "")) {
        Serial.println("✅ Firebase Auth Success");
    } else {
        Serial.printf("❌ Firebase Auth Error: %s\n", config.signer.signupError.message.c_str());
    }

    config.token_status_callback = tokenStatusCallback;
    Firebase.begin(&config, &auth);
    Firebase.reconnectWiFi(true);
}

// ─── MAIN LOOP ───────────────────────────────────────────
void loop() {
    timeClient.update();

    // Read incoming GPS data
    while (GPS_Serial.available()) {
        gps.encode(GPS_Serial.read());
    }

    // ─── 1. MPU6050 ACCIDENT DETECTION ───
    sensors_event_t a, g, temp;
    mpu.getEvent(&a, &g, &temp);

    // Calculate total G-force (Basic shake detection)
    float totalAcceleration = abs(a.acceleration.x) + abs(a.acceleration.y) + abs(a.acceleration.z);

    if (totalAcceleration > 25 && !accidentDetected) {
        triggerAccident("MPU6050 (High G-Force)");
        delay(1000); // Debounce
    }

    // ─── 2. SW420 CRASH SENSOR DETECTION ───
    if (digitalRead(CRASH_SENSOR_PIN) == LOW && !accidentDetected) {
        triggerAccident("SW420 (Vibration Impact)");
        delay(1000); // Debounce
    }

    // ─── 3. FIREBASE DATA SYNC ───
    if (Firebase.ready() && gps.location.isValid()) {
        unsigned long long timestamp = (unsigned long long)timeClient.getEpochTime() * 1000;

        // A. LIVE STATUS: Update every 5 seconds
        if (millis() - sendDataPrevMillis > 5000 || sendDataPrevMillis == 0) {
            sendDataPrevMillis = millis();

            FirebaseJson json;
            json.set("is_accident", accidentDetected);
            json.set("last_seen", timestamp);
            json.set("latitude", gps.location.lat());
            json.set("longitude", gps.location.lng());
            json.set("wifi_signal", WiFi.RSSI());

            String path = "devices/" + espMacAddress + "/status";
            if (Firebase.RTDB.setJSON(&fbdo, path.c_str(), &json)) {
                Serial.println("📡 Live Data Synced");
            }
        }

        // B. TRIP HISTORY: Push coordinate every 60 seconds (For the map line)
        if (millis() - sendHistoryPrevMillis > 60000 || sendHistoryPrevMillis == 0) {
            sendHistoryPrevMillis = millis();

            FirebaseJson historyJson;
            historyJson.set("latitude", gps.location.lat());
            historyJson.set("longitude", gps.location.lng());
            historyJson.set("timestamp", timestamp);

            String historyPath = "devices/" + espMacAddress + "/history";

            // pushJSON creates a unique ID for each coordinate so they don't overwrite!
            if (Firebase.RTDB.pushJSON(&fbdo, historyPath.c_str(), &historyJson)) {
                Serial.println("📍 History Point Saved to Firebase");
            }
        }
    }

    // ─── 4. SERIAL RESET COMMAND ───
    // Type 'r' in the Serial Monitor to reset the accident state
    if (Serial.available()) {
        if (Serial.read() == 'r') {
            accidentDetected = false;
            digitalWrite(BUZZER_PIN, LOW);
            Firebase.RTDB.setBool(&fbdo, ("devices/" + espMacAddress + "/status/is_accident").c_str(), false);
            Serial.println("🔄 System Reset. Alarm Disabled.");
        }
    }
}