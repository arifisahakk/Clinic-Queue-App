# 🏥 Clinic Queue Management System

A real-time clinic queue management system built with **Kotlin + Jetpack Compose** and **Firebase Realtime Database**. Designed to run across multiple Android devices simultaneously — patient tablet, clerk counter, and waiting room TV display.

---

## 📱 Screenshots

> _Run the app on 3 devices/emulators to see the full system in action._

---

## ✨ Features

- 🔍 **Patient Check-in** — Patient enters IC number to get a queue number
- 📝 **Patient Registration** — New patients can register their details on the spot
- 🎫 **Queue Ticket** — Displays queue number with auto-return after 10 seconds
- 🖥️ **Clerk Counter** — Counter A & B can call the next patient with one tap
- 📺 **Live TV Display** — Waiting room screen updates in real-time
- ☁️ **Firebase Sync** — All devices stay in sync instantly via the cloud

---

## 🏗️ System Architecture

```
┌─────────────────┐     ┌──────────────────────┐     ┌─────────────────┐
│  Patient Tablet │────▶│  Firebase Realtime DB │────▶│  Waiting Room TV│
│  (Check-in)     │     │                       │     │  (Live Display) │
└─────────────────┘     │  patients/            │     └─────────────────┘
                        │  queue/nextNumber     │
┌─────────────────┐     │  queue/waiting        │     
│  Counter A & B  │────▶│  counters/A           │     
│  (Clerk Screen) │     │  counters/B           │     
└─────────────────┘     └──────────────────────┘     
```

---

## 📲 Device Roles

| Device | Mode | Description |
|--------|------|-------------|
| Tablet / Phone | 📱 Patient Tablet | Patient enters IC, registers, gets queue number |
| PC / Phone | 🖥️ Counter A or B | Clerk calls next patient in queue |
| TV / Tablet | 📺 Waiting Room TV | Live display of currently serving numbers |

---

## 🗂️ Project Structure

```
app/src/main/java/com/clinic/queue/
│
├── MainActivity.kt        # App entry point + navigation setup + colour palette
├── QueueEntry.kt          # Firebase data model
├── TabletScreen.kt        # Patient check-in screen + Firebase helpers
├── RegisterScreen.kt      # New patient registration screen
├── TicketScreen.kt        # Queue number ticket with countdown
├── CounterScreen.kt       # Clerk counter screen
└── TVScreen.kt            # Waiting room live display
```

---

## 🛠️ Tech Stack

| Technology | Usage |
|------------|-------|
| Kotlin | Primary language |
| Jetpack Compose | UI framework |
| Firebase Realtime Database | Real-time data sync |
| Navigation Compose | Screen navigation |
| Android Studio | IDE |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK API 24+
- Firebase account
- Internet connection on all devices

### Installation

**1. Clone the repository**
```bash
git clone https://github.com/yourusername/ClinicQueueApp.git
cd ClinicQueueApp
```

**2. Set up Firebase**
- Go to [console.firebase.google.com](https://console.firebase.google.com)
- Create a new project
- Add an Android app with package name `com.clinic.queue`
- Download `google-services.json`
- Place it inside the `app/` folder

**3. Enable Firebase Realtime Database**
- In Firebase Console → Realtime Database → Create Database
- Start in **Test Mode**

**4. Import sample database**
- In Firebase Console → Realtime Database → ⋮ Menu → Import JSON
- Upload the `firebase_sample.json` file from this repo

**5. Build and run**
```bash
# Open in Android Studio and click Run
# OR build APK via:
./gradlew assembleDebug
```

---

## 🗄️ Firebase Database Structure

```json
{
  "counters": {
    "A": { "serving": 0, "status": "free" },
    "B": { "serving": 0, "status": "free" }
  },
  "patients": {
    "123456789010": {
      "name": "Ahmad Bin Ali",
      "idNumber": "123456-78-9010",
      "address": "No 12, Jalan Merdeka, Melaka"
    }
  },
  "queue": {
    "nextNumber": 1000,
    "waiting": {}
  }
}
```

---

## 📋 How to Use

### Patient Flow
1. Open app on tablet → tap **📱 Patient Tablet**
2. Enter IC number (e.g. `123456-78-9010`)
3. If registered → receive queue number ticket
4. If not registered → fill in registration form → then get queue number

### Clerk Flow
1. Open app on counter PC → tap **🖥️ Counter A** or **Counter B**
2. When ready → press **▶ NEXT PATIENT**
3. The next queue number is assigned to your counter

### TV Display
1. Open app on waiting room TV → tap **📺 Waiting Room TV**
2. Screen shows live queue numbers for Counter A and Counter B
3. Updates automatically — no refresh needed

---

## 🧪 Sample Patient IDs for Testing

| IC Number | Name |
|-----------|------|
| `123456-78-9010` | Ahmad Bin Ali |
| `234567-89-1011` | Siti Binti Abu |
| `345678-90-1112` | Raj Kumar A/L Muthu |
| `456789-01-1213` | Lim Mei Ling |
| `567890-12-1314` | Nurul Ain Binti Hassan |

---

## ⚙️ Running on Multiple Devices

### Real Devices
1. Enable USB Debugging on each device
2. Connect all devices via USB
3. In Android Studio → select each device → click ▶ Run
4. Each device opens the mode selector — choose the correct mode

### Virtual Devices (Emulator)
1. **Tools → Device Manager → Create Device** (create 3 AVDs)
2. Launch all 3 emulators
3. Run the app on each emulator
4. Select the correct mode on each

> 💡 **Tip:** Running 3 emulators requires at least 16GB RAM. If your PC is slow, use 2 emulators + 1 real device instead.

---

## 🔒 Security Notes

- `google-services.json` is listed in `.gitignore` and **never committed** to the repo
- Firebase rules are currently in **Test Mode** (open read/write)
- For production, update Firebase rules to restrict access:

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/YourFeature`
3. Commit your changes: `git commit -m 'Add YourFeature'`
4. Push to the branch: `git push origin feature/YourFeature`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

Made with ❤️ for clinic queue management.

> Built with Kotlin + Jetpack Compose + Firebase
