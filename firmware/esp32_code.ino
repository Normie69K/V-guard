#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <TinyGPS++.h>
#include <HardwareSerial.h>

// Required by Firebase ESP Client library
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// ─── 1. CONFIGURATION ────────────────────────────────────────────────────
#define WIFI_SSID "YOUR_WIFI_NAME"          // Replace with your WiFi SSID
#define WIFI_PASSWORD "YOUR_WIFI_PASSWORD"  // Replace with your WiFi Password

// Firebase Project Credentials
#define API_KEY "AIzaSyBRl2h6plN-ppWbF-RWq7OtQEXNN-QW6yQ"
#define DATABASE_URL "https://v-guard-e57f8-default-rtdb.firebaseio.com"

// ─── 2. HARDWARE PINS ────────────────────────────────────────────────────
#define CRASH_SENSOR_PIN 4  // Connect to SW-420 Vibration Sensor
#define GPS_RX_PIN 16       // Connect to TX of NEO-6M GPS
#define GPS_TX_PIN 17       // Connect to RX of NEO-6M GPS

// ─── 3. GLOBAL OBJECTS ───────────────────────────────────────────────────
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org");

TinyGPSPlus gps;
HardwareSerial GPS_Serial(2); // Use UART2 for GPS

String espMacAddress;
unsigned long sendDataPrevMillis = 0;
bool accidentDetected = false;

void setup() {
    Serial.begin(115200);
    GPS_Serial.begin(9600, SERIAL_8N1, GPS_RX_PIN, GPS_TX_PIN);
    pinMode(CRASH_SENSOR_PIN, INPUT_PULLUP);

    // ─── CONNECT WIFI ───
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("Connecting to Wi-Fi");
    while (WiFi.status() != WL_CONNECTED) {
        Serial.print(".");
        delay(300);
    }
    Serial.println("\n✅ Wi-Fi Connected!");

    // ─── GET DEVICE ID ───
    // This MAC address acts as the unique identifier for the Android app
    espMacAddress = WiFi.macAddress();
    Serial.println("========================================");
    Serial.print("🚀 YOUR DEVICE ID: ");
    Serial.println(espMacAddress);
    Serial.println("========================================");

    timeClient.begin();

    // ─── INIT FIREBASE ───
    config.api_key = API_KEY;
    config.database_url = DATABASE_URL;

    // Anonymous authentication must be enabled in Firebase Console
    if (Firebase.signUp(&config, &auth, "", "")) {
        Serial.println("✅ Firebase Auth Successful");
    } else {
        Serial.printf("❌ Firebase Auth Error: %s\n", config.signer.signupError.message.c_str());
    }

    config.token_status_callback = tokenStatusCallback;
    Firebase.begin(&config, &auth);
    Firebase.reconnectWiFi(true);
}

void loop() {
    timeClient.update();

    // Read GPS Serial data constantly
    while (GPS_Serial.available() > 0) {
        gps.encode(GPS_Serial.read());
    }

    // ─── CRASH DETECTION LOGIC ───
    // Triggered when the sensor pulls the pin LOW
    if (digitalRead(CRASH_SENSOR_PIN) == LOW && !accidentDetected) {
        Serial.println("⚠️ CRASH DETECTED! Updating Firebase...");
        accidentDetected = true;

        String path = "devices/" + espMacAddress + "/status/is_accident";
        Firebase.RTDB.setBool(&fbdo, path.c_str(), true); // Instant update for safety
        delay(1000); // Debounce
    }

    // ─── PERIODIC DATA SYNC (Every 5 seconds) ───
    if (Firebase.ready() && (millis() - sendDataPrevMillis > 5000 || sendDataPrevMillis == 0)) {
        sendDataPrevMillis = millis();

        if (gps.location.isValid()) {
            // Convert to milliseconds for Android compatibility
            unsigned long long timestamp = (unsigned long long)timeClient.getEpochTime() * 1000;

            FirebaseJson json;
            json.set("is_accident", accidentDetected);
            json.set("last_seen", timestamp);
            json.set("latitude", gps.location.lat());
            json.set("longitude", gps.location.lng());
            json.set("wifi_signal", WiFi.RSSI());

            String path = "devices/" + espMacAddress + "/status";
            if (Firebase.RTDB.setJSON(&fbdo, path.c_str(), &json)) {
                Serial.println("📡 Telemetry Synced");
            }
        } else {
            Serial.println("⏳ Searching for GPS satellites...");
        }
    }

    // Manual Reset via Serial Monitor for testing
    if (Serial.available()) {
        if (Serial.read() == 'r') {
            accidentDetected = false;
            Firebase.RTDB.setBool(&fbdo, ("devices/" + espMacAddress + "/status/is_accident").c_str(), false);
            Serial.println("🔄 Safety Status Reset");
        }
    }
}