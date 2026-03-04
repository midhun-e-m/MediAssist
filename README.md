# 🏥 MediAssist – AI Powered Medical Assistant

MediAssist is an AI-powered Android application designed to provide **basic first-aid guidance, emergency assistance tools, and location-based medical support**.  

The application combines **AI, mobile development, and real-world emergency utilities** to create an intelligent healthcare assistant.

---

# 📱 Overview

MediAssist is built using **Kotlin for Android** and integrates several modern technologies including AI models, Firebase services, and location-based hospital discovery.

The application provides:

- AI-based first-aid assistance
- Emergency contact notifications
- Nearby hospital discovery
- Secure user authentication
- Real-time location services

---

# 🚀 Features

## 🤖 AI Medical Assistant
- Integrated **Groq API (Llama 3.3 70B)**
- Provides **basic first-aid guidance**
- Avoids medical diagnosis
- Handles emergency escalation prompts

## 🔐 Authentication
- Firebase **Email/Password authentication**
- Secure login and session management

## 🏥 Nearby Hospitals
- **OpenStreetMap integration (OSMDroid)**
- Location-based hospital listing
- Road-based distance estimation

## 📞 Emergency Utilities
- Emergency contact management
- Sends **emergency SMS alerts to saved contacts**
- Emergency history tracking

## 📍 Location & Tracking
- Real-time device location
- Optional **driver / patient tracking modules**

---

# 🛠 Tech Stack

| Layer | Technology |
|------|------------|
| Language | Kotlin |
| Architecture | MVVM |
| Networking | Retrofit + OkHttp |
| AI Integration | Groq API (Llama 3.3 70B) |
| Maps | OSMDroid (OpenStreetMap) |
| Authentication | Firebase Auth |
| Database | Firebase Firestore |
| Async Operations | Kotlin Coroutines |

---

# 🔐 Security Implementation

To ensure secure API handling:

- API keys stored in **local.properties**
- Accessed via **BuildConfig**
- Secrets excluded using **.gitignore**
- No sensitive data committed to repository

---

# 🏗 Architecture Overview
Android App
│
├── UI Layer
│
├── ViewModel Layer
│
├── Repository Layer
│
└── Network Layer
→ Groq API (AI Model)

Firebase services manage:

- Authentication
- Firestore data storage

Project package structure:
ui/
auth/
driver/
user/
data/
remote/
model/

---

# ⚙️ Setup Instructions

1. Clone the repository
   git clone https://github.com/your-username/mediassist.git

2. Open the project in **Android Studio**

3. Add your **Groq API Key** inside `local.properties`
GROQ_API_KEY=your_api_key_here

4. Sync Gradle

5. Run the application on a device or emulator

---

# 📌 Future Improvements

- Backend proxy server for API key protection
- Offline first-aid knowledge caching
- Push notifications for emergency alerts
- Improved UI/UX
- Role-based dashboards

---

# 🎯 Learning Outcomes

Through this project I learned:

- Secure API key handling in Android
- AI model integration using REST APIs
- Firebase Authentication & Firestore
- OpenStreetMap integration without Google Maps
- Structuring scalable Android applications

---

# 👨‍💻 Developer

**Midhun**  
MCA Student | Backend Developer | Software developer

---

⭐ If you found this project useful, consider giving it a star!
